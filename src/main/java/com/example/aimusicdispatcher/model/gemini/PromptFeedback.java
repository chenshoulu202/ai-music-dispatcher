package com.example.aimusicdispatcher.model.gemini;

import lombok.Data;

import java.util.List;

@Data
public class PromptFeedback {
    private List<SafetyRating> safetyRatings;
}
