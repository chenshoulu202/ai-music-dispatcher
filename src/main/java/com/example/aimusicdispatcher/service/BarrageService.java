package com.example.aimusicdispatcher.service;

import com.example.aimusicdispatcher.dispatcher.BarrageFilterService;
import com.example.aimusicdispatcher.entity.IntroCache;
import com.example.aimusicdispatcher.entity.MusicLibrary;
import com.example.aimusicdispatcher.generator.GeminiService;
import com.example.aimusicdispatcher.generator.TextCleaningService;
import com.example.aimusicdispatcher.generator.TtsService;
import com.example.aimusicdispatcher.model.barrage.BarrageRequest;
import com.example.aimusicdispatcher.model.playlist.PlayTask;
import com.example.aimusicdispatcher.repository.IntroCacheRepository;
import com.example.aimusicdispatcher.repository.MusicLibraryRepository;
import com.example.aimusicdispatcher.scheduler.PlaybackWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class BarrageService {

    private static final Logger log = LoggerFactory.getLogger(BarrageService.class);
    // AI Worker Pool (Fixed, Size=10)
    private final ExecutorService aiWorkerPool = Executors.newFixedThreadPool(10);

    private final BarrageFilterService barrageFilterService;
    private final MusicLibraryRepository musicLibraryRepository;
    private final IntroCacheRepository introCacheRepository;
    private final GeminiService geminiService;
    private final TtsService ttsService;
    private final PlaybackWorker playbackWorker;
    private final TextCleaningService textCleaningService;

    public BarrageService(
            BarrageFilterService barrageFilterService,
            MusicLibraryRepository musicLibraryRepository,
            IntroCacheRepository introCacheRepository,
            GeminiService geminiService,
            TtsService ttsService,
            PlaybackWorker playbackWorker,
            TextCleaningService textCleaningService) {
        this.barrageFilterService = barrageFilterService;
        this.musicLibraryRepository = musicLibraryRepository;
        this.introCacheRepository = introCacheRepository;
        this.geminiService = geminiService;
        this.ttsService = ttsService;
        this.playbackWorker = playbackWorker;
        this.textCleaningService = textCleaningService;
    }

    @jakarta.annotation.PreDestroy
    public void shutdown() {
        log.info("Shutting down AI Worker Pool.");
        aiWorkerPool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!aiWorkerPool.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
                aiWorkerPool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!aiWorkerPool.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
                    log.error("AI Worker Pool did not terminate.");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            aiWorkerPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("AI Worker Pool shut down.");
    }

    public void processBarrage(BarrageRequest barrageRequest) {
        log.info("Processing barrage request: {}", barrageRequest);

        Optional<String> songNameOptional = barrageFilterService.extractSongName(barrageRequest);

        if (songNameOptional.isPresent()) {
            String songName = songNameOptional.get();
            String requester = barrageRequest.getUser() != null ? barrageRequest.getUser() : "匿名用户";

            Optional<MusicLibrary> musicOptional = musicLibraryRepository.findBySongName(songName);

            if (musicOptional.isEmpty()) {
                log.info("Requested song '{}' not found in library.", songName);
                return;
            }

            MusicLibrary music = musicOptional.get();

            // 4. 查 intro_cache 是否有口播音频
            Optional<IntroCache> introCacheOptional = introCacheRepository.findByMusicId(music.getId());
            String introAudioPath = null;

            if (introCacheOptional.isPresent()) {
                introAudioPath = introCacheOptional.get().getAudioPath();
                log.info("Found cached intro audio for song '{}'.", songName);
                enqueuePlayTask(music, introAudioPath, requester);
            } else {
                // 5. 同步生成口播（若缓存缺失），确保播放任务中包含口播路径
                log.info("Cached intro audio missing for song '{}', generating synchronously for playback queue.", songName);
                try {
                    java.util.Map<String, String> result = CompletableFuture.supplyAsync(() -> {
                        log.info("Calling GeminiService to generate intro text for song: {}", songName);
                        // 生成Gemini文案
                        String introText = geminiService.generateIntroText(requester, songName);
                        log.info("GeminiService returned intro text: \'{}\'".concat(introText != null ? introText : "null"), introText);

                        // 清洗文案（移除敏感词和生僻字）
                        String cleanedText = textCleaningService.cleanIntroText(introText);
                        log.info("Cleaned intro text: \'{}\'".concat(cleanedText != null ? cleanedText : "null"), cleanedText);

                        // 计算文案哈希值
                        String introTextHash = textCleaningService.hashIntroText(cleanedText);
                        log.info("Intro text hash: {}", introTextHash);

                        log.info("Calling TtsService to generate audio file for musicId: {}", music.getId());
                        // 生成TTS音频
                        String audioPath = ttsService.generateAudioFile(introText, music.getId());
                        log.info("TtsService returned audio path: \'{}\'".concat(audioPath != null ? audioPath : "null"), audioPath);

                        java.util.Map<String, String> innerResult = new java.util.HashMap<>();
                        innerResult.put("introText", introText);
                        innerResult.put("cleanedText", cleanedText);
                        innerResult.put("introTextHash", introTextHash);
                        innerResult.put("audioPath", audioPath);
                        return innerResult;
                    }, aiWorkerPool).get(); // .get() makes it synchronous

                    String introText = result.get("introText");
                    String introTextHash = result.get("introTextHash");
                    String generatedAudioPath = result.get("audioPath");
                    log.info("BarrageService received generatedAudioPath: \'{}\'".concat(generatedAudioPath != null ? generatedAudioPath : "null"), generatedAudioPath);

                    if (generatedAudioPath != null) {
                        IntroCache newIntroCache = new IntroCache();
                        newIntroCache.setMusicId(music.getId());
                        newIntroCache.setIntroText(introText); // 存储原始文案
                        newIntroCache.setIntroTextHash(introTextHash); // 存储文案哈希值用于快速对比
                        newIntroCache.setAudioPath(generatedAudioPath);
                        newIntroCache.setUpdateTime(LocalDateTime.now());
                        introCacheRepository.save(newIntroCache);
                        log.info("Generated and cached intro audio for song '{}' at '{}' (hash: {}).", songName, generatedAudioPath, introTextHash);
                        enqueuePlayTask(music, generatedAudioPath, requester); // Enqueue with generated path
                    } else {
                        log.warn("TTS generation failed for song '{}', playing song without intro.", songName);
                        enqueuePlayTask(music, null, requester); // Fallback to enqueue without intro if generation fails
                    }

                } catch (Exception ex) {
                    log.error("Failed to generate or cache intro audio for song '{}': {}", songName, ex.getMessage(), ex);
                    enqueuePlayTask(music, null, requester); // Fallback to enqueue without intro if generation fails
                }
            }
        } else {
            log.info("Received chat message from user '{}': '{}' (Permission system disabled, processing anyway)", barrageRequest.getUser(), barrageRequest.getContent());
        }
    }

    private void enqueuePlayTask(MusicLibrary music, String introAudioPath, String requester) {
        PlayTask playTask = PlayTask.builder()
                .musicId(music.getId())
                .songName(music.getSongName())
                .songFilePath(music.getFilePath())
                .introAudioPath(introAudioPath)
                .requester(requester)
                .build();
        playbackWorker.addPlayTask(playTask);
    }
}
