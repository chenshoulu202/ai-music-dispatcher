package com.example.aimusicdispatcher.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tts")
public class TtsProperties {
    private String provider; // TTS提供商（edge-tts等）
    private String voice; // 语音选择（如zh-CN-XiaoxiaoNeural）
    private Double rate; // 语速调节（-1.0到1.0，负数为降速）
    private String outputDir; // 输出目录
    private String audioFormat; // 音频格式（mp3、wav等）
    private Integer sampleRate; // 采样率
}
