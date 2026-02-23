package com.example.aimusicdispatcher.model.barrage;

import lombok.Data;

/**
 * Represents the incoming barrage (弹幕) data from the barrage client.
 */
@Data
public class BarrageRequest {
    private String user;
    private String content;
    private Long timestamp;
    // Potentially other fields like userLevel, roomId, etc.
}
