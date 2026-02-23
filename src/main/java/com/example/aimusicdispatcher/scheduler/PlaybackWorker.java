package com.example.aimusicdispatcher.scheduler;

import com.example.aimusicdispatcher.entity.MusicLibrary;
import com.example.aimusicdispatcher.model.playlist.PlayTask;
import com.example.aimusicdispatcher.repository.MusicLibraryRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class PlaybackWorker implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(PlaybackWorker.class);
    private static final int POLL_TIMEOUT_SECONDS = 15;

    private final BlockingQueue<PlayTask> playQueue = new LinkedBlockingQueue<>();
    private final MusicLibraryRepository musicLibraryRepository;
    // private final IntroCacheRepository introCacheRepository; // Unused for now
    private final AtomicBoolean running = new AtomicBoolean(true);
    private Thread workerThread;

    public PlaybackWorker(MusicLibraryRepository musicLibraryRepository) {
        this.musicLibraryRepository = musicLibraryRepository;
        // this.introCacheRepository = introCacheRepository;
    }

    @PostConstruct
    public void init() {
        workerThread = new Thread(this, "PlaybackWorker");
        workerThread.start();
        log.info("PlaybackWorker thread started.");
    }

    @PreDestroy
    public void destroy() {
        running.set(false);
        if (workerThread != null) {
            workerThread.interrupt();
            try {
                workerThread.join(); // Wait for the thread to finish
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("PlaybackWorker thread interrupted during shutdown.");
            }
        }
        log.info("PlaybackWorker thread stopped.");
    }

    public void addPlayTask(PlayTask task) {
        try {
            playQueue.put(task);
            log.info("Added play task to queue: {}", task.getSongName());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to add play task to queue: {}", task.getSongName(), e);
        }
    }

    @Override
    public void run() {
        while (running.get()) {
            try {
                PlayTask task = playQueue.poll(POLL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                if (task == null) {
                    log.info("No song in queue, triggering cold-fill.");
                    task = selectRandomSong();
                }

                if (task != null) {
                    playAudio(task);
                    updateMusicPlayCount(task.getMusicId());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("PlaybackWorker interrupted, shutting down.");
                running.set(false);
            } catch (Exception e) {
                log.error("Error during playback: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * 从数据库中随机选择一首歌曲进行播放（冷场自动填充）。
     * @return 随机选中的歌曲的PlayTask，如果没有可用歌曲则返回null。
     */
    private PlayTask selectRandomSong() {
        List<MusicLibrary> allSongs = musicLibraryRepository.findAll();
        if (allSongs.isEmpty()) {
            log.warn("No songs found in music library for cold-fill.");
            return null;
        }

        // 从数据库中选取 last_played_at 最久远的歌曲
        Optional<MusicLibrary> leastRecentlyPlayed = musicLibraryRepository.findFirstByOrderByLastPlayedAtAsc();
        if (leastRecentlyPlayed.isEmpty()) {
            log.warn("No songs found in music library for cold-fill or all songs have null lastPlayedAt.");
            return null;
        }

        MusicLibrary randomSong = leastRecentlyPlayed.get();

        // 对于冷场自动播放，不设置introAudioPath，即不播放口播
        return PlayTask.builder()
                .musicId(randomSong.getId())
                .songName(randomSong.getSongName())
                .songFilePath(randomSong.getFilePath())
                .introAudioPath(null) // Explicitly set to null for cold-fill
                .requester("Auto-DJ")
                .build();
    }

    /**
     * 播放音频文件（口播 -> 歌曲）。
     * 使用 Runtime.exec 调用 ffplay 外部进程。
     *
     * @param task 待播放任务
     */
    private void playAudio(PlayTask task) {
        log.info("Starting playback for song: {} (Requested by: {}) ", task.getSongName(), task.getRequester());

        String currentIntroAudioPath = task.getIntroAudioPath(); // This is the path from the PlayTask
        
        // 1. 播放口播音频 (如果存在)
        if (currentIntroAudioPath != null && !currentIntroAudioPath.isEmpty()) {
            File introFile = new File(currentIntroAudioPath);
            if (introFile.exists()) {
                log.info("Playing intro audio: {}", currentIntroAudioPath);
                executeFfplay(currentIntroAudioPath);
                log.info("Finished playing intro audio.");
            } else {
                log.warn("Intro audio file not found at expected path: {}", currentIntroAudioPath);
            }
        } else {
            log.info("No intro audio to play for song '{}'. Skipping intro.", task.getSongName());
        }

        // 2. 播放歌曲
        log.info("Playing main song: {}", task.getSongFilePath());
        if (task.getSongFilePath() != null && !task.getSongFilePath().isEmpty()) {
            File songFile = new File(task.getSongFilePath());
            if (songFile.exists()) {
                log.debug("Playing song: {}", task.getSongFilePath());
                executeFfplay(task.getSongFilePath());
                log.info("Finished playing main song.");
            } else {
                log.error("Song file not found: {}", task.getSongFilePath());
            }
        } else {
            log.error("Song file path is null or empty for song: {}", task.getSongName());
        }

        log.info("Finished playback for song: {}", task.getSongName());
    }

    private void executeFfplay(String filePath) {
        try {
            ProcessBuilder pb = new ProcessBuilder("ffplay", "-nodisp", "-autoexit", filePath);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // 启动一个线程读取并忽略ffplay的输出，防止缓冲区填满导致进程阻塞
            Thread outputGobbler = new Thread(() -> {
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // 可以选择打印日志，或者直接忽略。这里只在debug级别打印，避免日志过多
                        // log.trace("ffplay output: {}", line); 
                    }
                } catch (IOException e) {
                    // 忽略读取流时的错误
                }
            });
            outputGobbler.setDaemon(true);
            outputGobbler.start();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.warn("ffplay exited with non-zero code {} for file: {}", exitCode, filePath);
            }
        } catch (IOException e) {
            log.error("Error executing ffplay for file {}: {}", filePath, e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("ffplay process interrupted for file: {}", filePath);
        }
    }

    @Transactional
    private void updateMusicPlayCount(Long musicId) {
        musicLibraryRepository.findById(musicId).ifPresent(music -> {
            music.setPlayCount(music.getPlayCount() + 1);
            music.setLastPlayedAt(LocalDateTime.now());
            musicLibraryRepository.save(music);
            log.debug("Updated play count for musicId: {}", musicId);
        });
    }

    public BlockingQueue<PlayTask> getPlayQueue() {
        return playQueue;
    }
}
