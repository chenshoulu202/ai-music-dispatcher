package com.example.aimusicdispatcher.model.gemini;

import lombok.Data;

import java.util.List;

@Data
public class Content {
    private String role;
    private List<Part> parts;
}
