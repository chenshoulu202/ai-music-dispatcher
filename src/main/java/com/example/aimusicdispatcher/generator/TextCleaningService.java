package com.example.aimusicdispatcher.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TextCleaningService {

    private static final Logger log = LoggerFactory.getLogger(TextCleaningService.class);

    /**
     * 清洗口播文案，移除不易发音的字符和敏感词。
     * 
     * @param introText 原始口播文案
     * @return 清洗后的文案
     */
    public String cleanIntroText(String introText) {
        if (introText == null || introText.trim().isEmpty()) {
            return "";
        }

        String cleanedText = introText;

        // 1. 移除特殊符号和表情符号（保留中文、英文、数字、常见标点）
        // 保留：中文字符、英文字母、数字、空格、常见中文标点
        cleanedText = cleanedText.replaceAll("[^\\u4E00-\\u9FA5a-zA-Z0-9\\s，。！？；：、（）\\-]", "");

        // 2. 替换多个连续空格为单个空格
        cleanedText = cleanedText.replaceAll("\\s+", " ");

        // 3. 移除多个连续的标点符号，保留一个
        cleanedText = cleanedText.replaceAll("[，。！？；：、（）\\-]+", "$0");
        cleanedText = cleanedText.replaceAll("[，。！？]*$", ""); // 移除末尾标点

        cleanedText = cleanedText.trim();

        if (!cleanedText.equals(introText)) {
            log.debug("Cleaned intro text: '{}' -> '{}'", introText, cleanedText);
        }

        return cleanedText;
    }

    /**
     * 计算文案的哈希值，用于缓存判断。
     * 
     * @param introText 文案内容
     * @return 基于内容的哈希值
     */
    public String hashIntroText(String introText) {
        if (introText == null || introText.isEmpty()) {
            return "";
        }
        return String.valueOf(introText.hashCode());
    }
}
