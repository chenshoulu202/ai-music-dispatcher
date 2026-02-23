package com.example.aimusicdispatcher.model.gemini;

import lombok.Data;

import java.util.List;

@Data
public class GeminiResponse {
    private List<Candidate> candidates;
    private PromptFeedback promptFeedback;
}
