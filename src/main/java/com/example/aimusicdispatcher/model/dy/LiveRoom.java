package com.example.aimusicdispatcher.model.dy;

import lombok.Data;

/** 直播间信息 */
@Data
public class LiveRoom {
    /**
     * 在线观众数
     */
    private String audienceCount;
    /**
     * 本场点赞数
     */
    private String likeCount;
    /**
     * 主播粉丝数
     */
    private String followCount;
    /**
     * 累计观看人数
     */
    private String totalUserCount;
    /** 房间状态 */
    private Integer status;
}
