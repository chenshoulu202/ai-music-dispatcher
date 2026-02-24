package com.example.aimusicdispatcher.service;

import com.example.aimusicdispatcher.config.PermissionProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 权限管理服务
 * 用于管理直播间用户的点歌特权
 * 基于 Token 的限时授权机制
 */
@Service
public class PermissionService {

    private static final Logger logger = LogManager.getLogger(PermissionService.class);
    private static final long LOG_DEBOUNCE_MILLIS = 5000; // 5秒内不重复打印授权日志

    private final Cache<String, Long> permissionCache;
    private final Cache<String, Long> lastGrantLogTimeCache; // 用于日志防抖
    private final PermissionProperties permissionProperties;

    public PermissionService(PermissionProperties permissionProperties) {
        this.permissionProperties = permissionProperties;
        // 初始化 Caffeine 权限缓存
        // 最大容量5000个用户，不需要手动清理，通过expireAfterWrite自动过期
        this.permissionCache = Caffeine.newBuilder()
                .maximumSize(5000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .build();
        // 初始化 Caffeine 日志防抖缓存
        this.lastGrantLogTimeCache = Caffeine.newBuilder()
                .expireAfterWrite(LOG_DEBOUNCE_MILLIS, TimeUnit.MILLISECONDS)
                .build();
    }

    /**
     * 检查权限系统是否启用
     */
    public boolean isEnabled() {
        return permissionProperties.isEnabled();
    }

    /**
     * 为用户授权或续期
     *
     * @param userId 抖音用户ID
     * @param minutes 授权分钟数
     */
    public void grant(String userId, int minutes) {
        if (!isEnabled()) {
            logger.debug("[Permission] Permission system is disabled, ignoring grant request");
            return;
        }

        if (userId == null || userId.isEmpty()) {
            logger.warn("[Permission] Invalid userId provided");
            return;
        }

        long currentTime = System.currentTimeMillis();
        long expiryTime = currentTime + (long) minutes * 60 * 1000; // 转换为毫秒

        // 如果用户已在缓存中，则在当前过期时间基础上增加分钟数
        Long existingExpiry = permissionCache.getIfPresent(userId);
        if (existingExpiry != null && existingExpiry > currentTime) {
            // 用户仍在有效期内，在现有基础上增加时间
            expiryTime = existingExpiry + (long) minutes * 60 * 1000;
        }

        permissionCache.put(userId, expiryTime);

        // 日志防抖逻辑
        if (lastGrantLogTimeCache.getIfPresent(userId) == null) {
            logger.info("[Privilege] User: {} granted {} mins", userId, minutes);
            lastGrantLogTimeCache.put(userId, currentTime);
        }
    }

    /**
     * 检查用户是否有效期内（有权限）
     *
     * @param userId 抖音用户ID
     * @return true 表示用户在有效期内，有权限进行点歌操作；false 表示无权限
     */
    public boolean isAuthorized(String userId) {
        // 如果权限系统被禁用，所有用户都是授权的
        if (!isEnabled()) {
            return true;
        }

        if (userId == null || userId.isEmpty()) {
            logger.debug("[Permission] Invalid userId provided for authorization check");
            return false;
        }

        Long expiryTime = permissionCache.getIfPresent(userId);
        if (expiryTime == null) {
            logger.debug("[Permission] User {} not found in cache", userId);
            return false;
        }

        long currentTime = System.currentTimeMillis();
        boolean authorized = expiryTime > currentTime;

        if (!authorized) {
            logger.debug("[Permission] User {} authorization expired at {}", userId, expiryTime);
        }

        return authorized;
    }

    /**
     * 获取用户的剩余有效期（毫秒）
     *
     * @param userId 抖音用户ID
     * @return 剩余时间（毫秒），如果无权限则返回 -1
     */
    public long getRemainingTime(String userId) {
        Long expiryTime = permissionCache.getIfPresent(userId);
        if (expiryTime == null) {
            return -1;
        }

        long remaining = expiryTime - System.currentTimeMillis();
        return remaining > 0 ? remaining : -1;
    }

    /**
     * 撤销用户权限
     *
     * @param userId 抖音用户ID
     */
    public void revoke(String userId) {
        if (userId != null && !userId.isEmpty()) {
            permissionCache.invalidate(userId);
            lastGrantLogTimeCache.invalidate(userId); // 清除日志防抖记录
            logger.info("[Permission] User: {} permission revoked", userId);
        }
    }

    /**
     * 清空所有权限缓存
     */
    public void clearAll() {
        permissionCache.invalidateAll();
        lastGrantLogTimeCache.invalidateAll(); // 清空日志防抖记录
        logger.info("[Permission] All permissions cleared");
    }
}
