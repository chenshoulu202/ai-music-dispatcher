package com.example.aimusicdispatcher.generator;

import com.example.aimusicdispatcher.config.TtsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class TtsService {

    private static final Logger log = LoggerFactory.getLogger(TtsService.class);

    private final TtsProperties ttsProperties;
    private final TextCleaningService textCleaningService;

    public TtsService(TtsProperties ttsProperties, TextCleaningService textCleaningService) {
        this.ttsProperties = ttsProperties;
        this.textCleaningService = textCleaningService;
        // 初始化输出目录
        String outputDir = ttsProperties.getOutputDir();
        if (outputDir == null || outputDir.isEmpty()) {
            outputDir = "tts_output"; // Default to clean path
        }
        Path outputPath = Paths.get(outputDir).toAbsolutePath().normalize(); // Normalize the path
        try {
            Files.createDirectories(outputPath);
            log.info("TTS output directory initialized: {}", outputPath);
        } catch (IOException e) {
            log.warn("Failed to create TTS output directory {}: {}", outputPath, e.getMessage());
        }
    }

    /**
     * 调用Edge-TTS API生成语音文件。
     * 包含缓存策略：如果文件已存在且文案未变，直接复用。
     * 包含文本清洗：移除敏感词和生僻字。
     * 
     * 前置要求：系统需要安装Python和edge-tts库
     * 安装命令：pip install edge-tts
     *
     * @param introText 需要转换为语音的文本。
     * @param musicId 关联的音乐ID，用于命名音频文件。
     * @return 生成的音频文件路径。
     */
    public String generateAudioFile(String introText, Long musicId) {
        // 1. 文本清洗：去除敏感词和不易发音的字符
        String cleanedText = textCleaningService.cleanIntroText(introText);
        if (cleanedText.isEmpty()) {
            log.warn("Intro text is empty after cleaning for musicId: {}", musicId);
            cleanedText = "感谢点歌";
        }

        // 2. 计算文案哈希值，用于缓存判断
        String introTextHash = textCleaningService.hashIntroText(cleanedText);

        String outputDir = ttsProperties.getOutputDir();
        if (outputDir == null || outputDir.isEmpty()) {
            outputDir = "tts_output"; // Remove leading ./ to ensure proper path resolution
        }

        // 创建输出目录
        Path outputPath = Paths.get(outputDir).toAbsolutePath().normalize(); // Normalize the path
        try {
            Files.createDirectories(outputPath);
        } catch (IOException e) {
            log.error("Failed to create output directory {}: {}", outputDir, e.getMessage());
        }

        // 3. 生成音频文件名和路径 (使用哈希值作为文件名的一部分，实现缓存)
        String audioFormat = ttsProperties.getAudioFormat() != null ? ttsProperties.getAudioFormat() : "mp3";
        String audioFileName = "intro_" + introTextHash + "." + audioFormat; // Use hash for filename
        Path audioFilePath = outputPath.resolve(audioFileName).normalize(); // Normalize the final path

        // 检查是否已存在同名音频文件（基于哈希值）
        if (Files.exists(audioFilePath)) {
            log.info("Cached TTS audio found for hash: {}, reusing existing file: {}", introTextHash, audioFilePath.toString());
            return audioFilePath.toString();
        }

        // 4. 准备TTS参数
        String voice = ttsProperties.getVoice() != null ? ttsProperties.getVoice() : "zh-CN-XiaoxiaoNeural";
        Double rate = ttsProperties.getRate() != null ? ttsProperties.getRate() : -0.15; // 降速15%更有电台感
        // Convert rate to percentage format for edge-tts: -0.15 -> "-15%"
        String ratePercentage = String.format("%+d%%", (int)(rate * 100));

        try {
            log.info("Generating TTS audio for musicId: {} with cleaned text: '{}' (hash: {})", 
                    musicId, cleanedText, introTextHash);

            // 5. 使用edge-tts命令行生成语音
            // edge-tts --voice zh-CN-XiaoxiaoNeural --rate=-15% --text "文本内容" --write-media 输出文件路径
            String edgeTtsExecutablePath = "/Users/chenshoulu/Downloads/dy1/.venv/bin/edge-tts";

            String[] command = {
                    edgeTtsExecutablePath,
                    "--text", cleanedText,
                    "--write-media", audioFilePath.toString()
            };

            Process process = Runtime.getRuntime().exec(command, null, new File(System.getProperty("user.dir")));

            // Capture error stream
            new Thread(() -> {
                try (java.io.BufferedReader errorReader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        log.error("edge-tts STDERR: {}", line);
                    }
                } catch (java.io.IOException e) {
                    log.error("Error reading edge-tts error stream", e);
                }
            }).start();

            // Capture output stream (optional, but good for debugging)
            new Thread(() -> {
                try (java.io.BufferedReader outputReader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = outputReader.readLine()) != null) {
                        log.info("edge-tts STDOUT: {}", line);
                    }
                } catch (java.io.IOException e) {
                    log.error("Error reading edge-tts output stream", e);
                }
            }).start();


            int exitCode = process.waitFor();
            if (exitCode == 0) {
                File audioFile = audioFilePath.toFile();
                if (audioFile.exists()) {
                    long fileSize = audioFile.length();
                    log.info("Successfully generated TTS audio at: {} (size: {} bytes, hash: {})", 
                            audioFilePath.toString(), fileSize, introTextHash);
                    return audioFilePath.toString();
                } else {
                    log.error("TTS audio file not found after generation: {}", audioFilePath.toString());
                    return null; // Return null if file not found after generation
                }
            } else {
                log.error("edge-tts process exited with code {} for musicId: {}", exitCode, musicId);
                return null; // Return null if edge-tts process failed
            }
        } catch (IOException | InterruptedException e) {
            log.error("Error calling edge-tts for musicId {}: {}", musicId, e.getMessage(), e);
            Thread.currentThread().interrupt();
            return null; // Return null on exception
        }
    }
}
