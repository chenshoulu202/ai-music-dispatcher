package com.example.aimusicdispatcher.util;

/**
 * ANSI 颜色代码工具类
 * 用于在控制台输出中添加颜色高亮
 */
public class AnsiColors {
    
    // ANSI 重置代码
    public static final String RESET = "\u001B[0m";
    
    // 前景色
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    
    // 亮色前景色
    public static final String BRIGHT_RED = "\u001B[91m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_BLUE = "\u001B[94m";
    public static final String BRIGHT_MAGENTA = "\u001B[95m";
    public static final String BRIGHT_CYAN = "\u001B[96m";
    
    // 背景色
    public static final String BG_RED = "\u001B[41m";
    public static final String BG_GREEN = "\u001B[42m";
    public static final String BG_YELLOW = "\u001B[43m";
    public static final String BG_BLUE = "\u001B[44m";
    
    // 样式
    public static final String BOLD = "\u001B[1m";
    public static final String DIM = "\u001B[2m";
    public static final String ITALIC = "\u001B[3m";
    public static final String UNDERLINE = "\u001B[4m";
    
    /**
     * 为权限授予消息添加亮绿色高亮
     */
    public static String privilegeHighlight(String message) {
        return BRIGHT_GREEN + message + RESET;
    }
    
    /**
     * 为拒绝访问消息添加亮红色高亮
     */
    public static String accessDeniedHighlight(String message) {
        return BRIGHT_RED + message + RESET;
    }
    
    /**
     * 为 [Privilege] 标签添加亮绿色高亮
     */
    public static String highlightPrivilegeTag() {
        return BRIGHT_GREEN + "[Privilege]" + RESET;
    }
    
    /**
     * 为 [AccessDenied] 标签添加亮红色高亮
     */
    public static String highlightAccessDeniedTag() {
        return BRIGHT_RED + "[AccessDenied]" + RESET;
    }
}
