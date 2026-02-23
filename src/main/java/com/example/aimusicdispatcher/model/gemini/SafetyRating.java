package com.example.aimusicdispatcher.model.gemini;

import lombok.Data;

@Data
public class SafetyRating {
    private String category;
    private String probability;
}
