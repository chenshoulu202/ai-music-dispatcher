package com.example.aimusicdispatcher.dispatcher;

import com.example.aimusicdispatcher.config.PermissionProperties;
import com.example.aimusicdispatcher.model.barrage.BarrageRequest;
import com.example.aimusicdispatcher.model.dy.CastMethod;
import com.example.aimusicdispatcher.model.dy.DyMessage;
import com.example.aimusicdispatcher.service.BarrageService;
import com.example.aimusicdispatcher.service.PermissionService;
import com.example.aimusicdispatcher.util.AnsiColors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * 消息分发器
 * 根据抖音消息的 method 字段，将消息路由到相应的处理逻辑
 */
@Service
public class MessageDispatcher {

    private static final Logger logger = LogManager.getLogger(MessageDispatcher.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final PermissionService permissionService;
    private final BarrageService barrageService;
    private final BarrageFilterService barrageFilterService;
    private final PermissionProperties permissionProperties;

    public MessageDispatcher(PermissionService permissionService, BarrageService barrageService, 
                           BarrageFilterService barrageFilterService, PermissionProperties permissionProperties) {
        this.permissionService = permissionService;
        this.barrageService = barrageService;
        this.barrageFilterService = barrageFilterService;
        this.permissionProperties = permissionProperties;
    }

    /**
     * 分发抖音消息到相应的处理器
     *
     * @param dyMessage 抖音弹幕消息
     */
    public void dispatch(DyMessage dyMessage) {
        if (dyMessage == null) {
            logger.warn("Received null DyMessage");
            return;
        }

        if (dyMessage.getMethod() == null) {
            logger.warn("Message method is null, ignoring.");
            return;
        }

        logger.info("Dispatching message: method={}, userId={}", 
                dyMessage.getMethod(), 
                dyMessage.getUser() != null ? dyMessage.getUser().getId() : "unknown");

        switch (dyMessage.getMethod()) {
            case LIKE:
                handleLikeMessage(dyMessage);
                break;
            case GIFT:
                handleGiftMessage(dyMessage);
                break;
            case CHAT:
                handleChatMessage(dyMessage);
                break;
            default:
                logger.info("Unhandled message type: {}", dyMessage.getMethod());
        }
    }

    /**
     * 处理点赞消息
     * 点赞触发：用户获得权限时长的点歌权限（可配置，默认5分钟）
     *
     * @param message 点赞消息
     */
    private void handleLikeMessage(DyMessage message) {
        // 安全性检查：确保 user 对象存在
        if (message.getUser() == null || message.getUser().getId() == null) {
            logger.warn("Like message received but user information is missing");
            return;
        }

        String userId = message.getUser().getId();
        String userName = message.getUser().getName();
        int likeMinutes = permissionProperties.getLikeMinutes();

        logger.info("Like received from user: {} ({})", userName, userId);

        // 只有启用了权限系统才授予权限
        if (permissionProperties.isEnabled()) {
            permissionService.grant(userId, likeMinutes);
            String logMsg = String.format("%s User: %s (ID: %s) granted %d mins at %s", 
                    AnsiColors.highlightPrivilegeTag(), 
                    userName, 
                    userId, 
                    likeMinutes, 
                    LocalDateTime.now().format(TIME_FORMATTER));
            logger.info(logMsg);
        }
    }

    /**
     * 处理送礼消息
     * 送礼触发：用户获得权限时长的点歌权限（可配置，默认20分钟，权重高于点赞）
     *
     * @param message 送礼消息
     */
    private void handleGiftMessage(DyMessage message) {
        // 安全性检查：确保 user 对象存在
        if (message.getUser() == null || message.getUser().getId() == null) {
            logger.warn("Gift message received but user information is missing");
            return;
        }

        String userId = message.getUser().getId();
        String userName = message.getUser().getName();
        int giftMinutes = permissionProperties.getGiftMinutes();

        logger.info("Gift received from user: {} ({})", userName, userId);

        // 如果有礼物信息，也记录礼物名称和数量
        if (message.getGift() != null) {
            logger.info("Gift details: {} x {}", message.getGift().getName(), message.getGift().getCount());
        }

        // 只有启用了权限系统才授予权限
        if (permissionProperties.isEnabled()) {
            permissionService.grant(userId, giftMinutes);
            String logMsg = String.format("%s User: %s (ID: %s) granted %d mins at %s", 
                    AnsiColors.highlightPrivilegeTag(), 
                    userName, 
                    userId, 
                    giftMinutes, 
                    LocalDateTime.now().format(TIME_FORMATTER));
            logger.info(logMsg);
        }
    }

    /**
     * 处理聊天/弹幕消息
     * 流程：
     * 1. 通过正则提取歌名
     * 2. 检查用户权限（如果启用了权限系统）
     * 3. 权限通过或系统禁用：调用 songService.play()
     * 4. 权限失败：记录拦截日志及彩色高亮
     *
     * @param message 聊天消息
     */
    private void handleChatMessage(DyMessage message) {
        // 安全性检查：确保必要的信息存在
        if (message.getContent() == null || message.getContent().isEmpty()) {
            logger.info("Chat message received but content is empty");
            return;
        }

        // 如果 user 对象为 null，这可能是系统消息，直接返回
        if (message.getUser() == null) {
            logger.info("Chat message received but user information is missing (possibly system message)");
            return;
        }

        String userId = message.getUser().getId();
        String userName = message.getUser().getName();
        String content = message.getContent();

        logger.info("Chat message from user {} ({}): {}", userName, userId, content);

        // Step 1: 通过正则提取歌名
        BarrageRequest barrageRequest = new BarrageRequest();
        barrageRequest.setUser(userName);
        barrageRequest.setContent(content);
        barrageRequest.setTimestamp(System.currentTimeMillis());

        Optional<String> songNameOptional = barrageFilterService.extractSongName(barrageRequest);

        if (songNameOptional.isEmpty()) {
            logger.info("No song request detected in message from user {}", userName);
            return;
        }

        String songName = songNameOptional.get();
        logger.info("Song request detected: {} from user {}", songName, userName);

        // Step 2: 检查用户是否有权限
        if (permissionProperties.isEnabled()) {
            // 启用了权限系统，需要检查权限
            if (userId != null && permissionService.isAuthorized(userId)) {
                logger.info("User {} (ID: {}) has permission to request songs", userName, userId);
                // Step 3: 权限通过，处理点歌请求
                barrageService.processBarrage(barrageRequest);
            } else {
                // Step 4: 权限失败，记录拦截日志（带ANSI颜色高亮）
                String currentTime = LocalDateTime.now().format(TIME_FORMATTER);
                String logMsg = String.format("%s User %s (ID: %s) tried to order \"%s\" without liking or gifting at %s", 
                        AnsiColors.highlightAccessDeniedTag(), 
                        userName, 
                        userId != null ? userId : "unknown", 
                        songName, 
                        currentTime);
                logger.warn(logMsg);
            }
        } else {
            // 权限系统被禁用，直接处理点歌请求
            logger.info("Permission system is disabled, allowing all song requests");
            barrageService.processBarrage(barrageRequest);
        }
    }
}
