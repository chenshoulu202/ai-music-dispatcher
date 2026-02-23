package com.example.aimusicdispatcher.model.gemini;

import lombok.Data;
import java.util.List;

@Data
public class GenerationConfig {
    private Double temperature;
    private Integer topK;
    private Integer topP;
    private Integer candidateCount;
    private Integer maxOutputTokens;
    private List<String> stopSequences;
}
