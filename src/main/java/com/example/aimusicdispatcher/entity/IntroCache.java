package com.example.aimusicdispatcher.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "intro_cache")
public class IntroCache {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "music_id", nullable = false, unique = true)
    private Long musicId;

    @Column(name = "intro_text", columnDefinition = "TEXT")
    private String introText;

    @Column(name = "intro_text_hash")
    private String introTextHash; // intro_text的哈希值，用于快速对比

    @Column(name = "audio_path")
    private String audioPath;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
