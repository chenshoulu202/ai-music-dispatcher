package com.example.aimusicdispatcher.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "music_library")
public class MusicLibrary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "song_name", nullable = false, unique = true)
    private String songName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "last_played_at")
    private LocalDateTime lastPlayedAt;

    @Column(name = "play_count")
    private Integer playCount = 0;
}
