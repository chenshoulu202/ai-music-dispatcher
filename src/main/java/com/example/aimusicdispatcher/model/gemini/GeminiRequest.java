package com.example.aimusicdispatcher.model.gemini;

import lombok.Data;

import java.util.List;

@Data
public class GeminiRequest {
    private List<Content> contents;
    private GenerationConfig generationConfig;
    private List<SafetySetting> safetySettings;
}
