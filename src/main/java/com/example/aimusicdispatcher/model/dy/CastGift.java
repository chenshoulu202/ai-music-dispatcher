package com.example.aimusicdispatcher.model.dy;

import lombok.Data;

@Data
public class CastGift {
    private String id;
    private String name;
    // 价值抖音币 diamond_count
    private Integer price;
    private Integer type;
    // 描述
    private String desc;
    // 图片
    private String icon;
    // 数量 repeat_count | combo_count
    // Assuming mixed types in TS (number | string), we'll stick to String or Integer based on usage. 
    // Usually repeat_count is a number. Let's use Integer for now, or String if it can be big.
    // The TS definition says `number | string`. Let's use String to be safe or Integer if logical.
    // Let's use String to match `number | string` flexibility or simply Integer if that's what's expected.
    // But repeat_count is usually an integer.
    private String count; 
    // 礼物消息可能重复发送，0 表示第一次，未重复
    private Integer repeatEnd;
}
