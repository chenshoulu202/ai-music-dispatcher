package com.example.aimusicdispatcher.model.dy;

import lombok.Data;
import java.util.List;

/** 最后的整理转发的弹幕消息结构 */
@Data
public class DyMessage {
    // 弹幕 ID
    private String id;
    // 弹幕类型
    private CastMethod method;
    // 用户信息
    private CastUser user;
    // 礼物信息(当类型为礼物弹幕时有值)
    private CastGift gift;
    // 弹幕文本
    private String content;
    // 富文本信息
    private List<CastRtfContent> rtfContent;
    // 房间相关信息
    private LiveRoom room;
    // 礼物排行榜信息
    private List<LiveRankItem> rank;
}
