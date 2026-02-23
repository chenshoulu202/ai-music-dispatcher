package com.example.aimusicdispatcher.model.dy;

import lombok.Data;

@Data
public class CastUser {
    // user.sec_uid | user.id_str
    private String id;
    // user.nickname
    private String name;
    // user.avatar_thumb.url_list.0
    private String avatar;
    // 性别(猜测) 0 | 1 | 2 => 未知 | 男 | 女
    private Integer gender;
}
