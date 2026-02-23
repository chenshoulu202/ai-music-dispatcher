package com.example.aimusicdispatcher.service;

import com.example.aimusicdispatcher.config.MusicProperties;
import com.example.aimusicdispatcher.entity.MusicLibrary;
import com.example.aimusicdispatcher.repository.MusicLibraryRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MusicSyncService {

    private static final Logger log = LoggerFactory.getLogger(MusicSyncService.class);

    private final MusicLibraryRepository musicLibraryRepository;
    private final MusicProperties musicProperties;

    public MusicSyncService(MusicLibraryRepository musicLibraryRepository, MusicProperties musicProperties) {
        this.musicLibraryRepository = musicLibraryRepository;
        this.musicProperties = musicProperties;
    }

    @PostConstruct
    @Transactional
    public void syncMusicLibrary() {
        log.info("Starting music library synchronization...");
        String musicDirPath = musicProperties.getLocalPath();
        if (musicDirPath == null || musicDirPath.isEmpty()) {
            log.warn("Music local path is not configured. Skipping synchronization.");
            return;
        }

        Path musicDirectory = Paths.get(musicDirPath);
        if (!Files.exists(musicDirectory) || !Files.isDirectory(musicDirectory)) {
            log.error("Music directory does not exist or is not a directory: {}", musicDirPath);
            return;
        }

        Set<String> localFileNames = new HashSet<>();
        int addedCount = 0;
        int removedCount = 0;

        try {
            // 1. Scan local directory for .mp3 and .wav files
            List<Path> files = Files.walk(musicDirectory)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".mp3") || p.toString().endsWith(".wav"))
                    .collect(Collectors.toList());

            for (Path filePath : files) {
                String fileName = filePath.getFileName().toString();
                String songName = fileName.substring(0, fileName.lastIndexOf('.'));
                localFileNames.add(songName);

                // 检查歌曲是否已存在于数据库
                boolean existsInDb = musicLibraryRepository.findBySongName(songName).isPresent();

                if (!existsInDb) {
                    MusicLibrary newSong = new MusicLibrary();
                    newSong.setSongName(songName);
                    newSong.setFilePath(filePath.toAbsolutePath().toString());
                    musicLibraryRepository.save(newSong);
                    addedCount++;
                    log.debug("Added new song: {}", songName);
                }
            }

            // 2. Check for songs in DB that no longer exist locally
            List<MusicLibrary> allDbSongs = musicLibraryRepository.findAll();
            for (MusicLibrary dbSong : allDbSongs) {
                String dbSongName = dbSong.getSongName();
                if (!localFileNames.contains(dbSongName)) {
                    musicLibraryRepository.delete(dbSong);
                    removedCount++;
                    log.debug("Removed non-existent song from DB: {}", dbSongName);
                }
            }

        } catch (IOException e) {
            log.error("Error scanning music directory {}: {}", musicDirPath, e.getMessage());
        }

        log.info("Music library synchronization completed. Added: {} songs, Removed: {} songs.", addedCount, removedCount);
    }
}
