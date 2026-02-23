package com.example.aimusicdispatcher.model.gemini;

import lombok.Data;

import java.util.List;

@Data
public class Candidate {
    private Content content;
    private String finishReason;
    private Integer index;
    private List<SafetyRating> safetyRatings;
}
