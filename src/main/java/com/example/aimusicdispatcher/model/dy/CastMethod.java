package com.example.aimusicdispatcher.model.dy;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 弹幕类型
 */
@Getter
public enum CastMethod {
    CHAT("WebcastChatMessage"),
    GIFT("WebcastGiftMessage"),
    LIKE("WebcastLikeMessage"),
    MEMBER("WebcastMemberMessage"),
    SOCIAL("WebcastSocialMessage"),
    ROOM_USER_SEQ("WebcastRoomUserSeqMessage"),
    CONTROL("WebcastControlMessage"),
    ROOM_RANK("WebcastRoomRankMessage"),
    ROOM_STATS("WebcastRoomStatsMessage"),
    EMOJI_CHAT("WebcastEmojiChatMessage"),
    FANSCLUB("WebcastFansclubMessage"),
    ROOM_DATA_SYNC("WebcastRoomDataSyncMessage"),
    /** 自定义消息 */
    CUSTOM("CustomMessage");

    @JsonValue
    private final String value;

    CastMethod(String value) {
        this.value = value;
    }

    @com.fasterxml.jackson.annotation.JsonCreator
    public static CastMethod fromValue(String value) {
        for (CastMethod method : values()) {
            if (method.value.equals(value)) {
                return method;
            }
        }
        return null;
    }
}
