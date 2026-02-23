package com.example.aimusicdispatcher.model.dy;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 富文本类型
 * 1 - 普通文本
 * 2 - 合并表情
 */
@Getter
public enum CastRtfContentType {
    TEXT(1),
    EMOJI(2);

    @JsonValue
    private final int value;

    CastRtfContentType(int value) {
        this.value = value;
    }

    @com.fasterxml.jackson.annotation.JsonCreator
    public static CastRtfContentType fromValue(int value) {
        for (CastRtfContentType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null;
    }
}
