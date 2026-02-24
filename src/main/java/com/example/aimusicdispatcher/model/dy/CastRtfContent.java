package com.example.aimusicdispatcher.model.dy;

import lombok.Data;

// 富文本
@Data
public class CastRtfContent {
    private CastRtfContentType type;
    private String text;
    private String url;
    private CastUser user; // Added to match frontend structure
}
