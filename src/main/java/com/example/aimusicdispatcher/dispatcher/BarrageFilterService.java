package com.example.aimusicdispatcher.dispatcher;

import com.example.aimusicdispatcher.model.barrage.BarrageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BarrageFilterService {

    private static final Logger log = LoggerFactory.getLogger(BarrageFilterService.class);

    // 正则表达式用于匹配点歌指令
    // (?i) 忽略大小写
    // ^.*? 匹配开头任意字符（非贪婪）
    // (点歌|来首|播放|我想听|play|music) 匹配点歌关键词
    // \s*[:：]*\s* 匹配冒号和空格
    // (?:一首|个)? 可选的“一首”或“个”
    // .*?(.*?).*$ 匹配歌名（非贪婪）
    private static final Pattern SONG_REQUEST_PATTERN = Pattern.compile(
            "(?i)^.*?(点歌|来首|播放|我想听|play|music)\\s*[:：]*\\s*(?:一首|个)?\\s*[\"'《「]?(.*?)[\"'》」!！?？\\s]*$"
    );

    /**
     * 过滤弹幕，识别点歌指令并提取歌曲名称。
     *
     * @param barrageRequest 弹幕请求对象
     * @return 如果是有效的点歌指令，返回歌曲名称的Optional；否则返回Optional.empty()。
     */
    public Optional<String> extractSongName(BarrageRequest barrageRequest) {
        if (barrageRequest == null || barrageRequest.getContent() == null || barrageRequest.getContent().trim().isEmpty()) {
            return Optional.empty();
        }

        String content = barrageRequest.getContent().trim();
        Matcher matcher = SONG_REQUEST_PATTERN.matcher(content);

        if (matcher.matches()) {
            String songName = matcher.group(2).trim();
            // 清洗歌名，去除特殊符号，只保留中文、英文、数字、空格、破折号和单引号
            songName = songName.replaceAll("[^\\p{L}\\p{N}\\s-']", "").trim();
            if (!songName.isEmpty()) {
                log.info("Detected song request from user '{}' for song: '{}'", barrageRequest.getUser(), songName);
                return Optional.of(songName);
            }
        }
        return Optional.empty();
    }
}
