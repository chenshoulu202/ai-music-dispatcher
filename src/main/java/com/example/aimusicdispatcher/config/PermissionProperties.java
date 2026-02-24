package com.example.aimusicdispatcher.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 应用权限配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.permission")
public class PermissionProperties {
    
    /**
     * 是否启用权限校验功能
     * true: 需要点赞或送礼才能点歌
     * false: 所有用户都可以点歌
     */
    private boolean enabled = true;
    
    /**
     * 点赞授予的权限时长（分钟）
     */
    private int likeMinutes = 5;
    
    /**
     * 送礼授予的权限时长（分钟）
     */
    private int giftMinutes = 20;
}
