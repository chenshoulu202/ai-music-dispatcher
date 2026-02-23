package com.example.aimusicdispatcher.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "gemini")
public class GeminiProperties {
    private String apiKey;
    private String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key={apiKey}";
    private String systemPrompt = "你是一个幽默风趣的直播间DJ。请为点歌人[USER]点的歌曲[SONG]写一段30字内的口播。要自然、亲切，能引起观众共鸣。";
}
