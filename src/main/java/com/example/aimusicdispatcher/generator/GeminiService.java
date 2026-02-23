package com.example.aimusicdispatcher.generator;

import com.example.aimusicdispatcher.config.GeminiProperties;
import com.example.aimusicdispatcher.model.gemini.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);
    private static final String FALLBACK_INTRO_TEXT = "感谢您的点歌，接下来为您播放您喜欢的歌曲！";

    private final RestTemplate restTemplate;
    private final GeminiProperties geminiProperties;

    public GeminiService(RestTemplate restTemplate, GeminiProperties geminiProperties) {
        this.restTemplate = restTemplate;
        this.geminiProperties = geminiProperties;
    }

    /**
     * 调用Gemini API生成歌曲口播文案。
     *
     * @param user     点歌用户
     * @param songName 歌曲名称
     * @return 生成的口播文案，如果API调用失败则返回默认文案。
     */
    public String generateIntroText(String user, String songName) {
        String prompt = geminiProperties.getSystemPrompt()
                .replace("[USER]", user)
                .replace("[SONG]", songName);

        GeminiRequest request = buildGeminiRequest(prompt);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);

        String apiUrl = geminiProperties.getApiUrl().replace("{apiKey}", geminiProperties.getApiKey());

        try {
            log.info("Calling Gemini API for song: {}", songName);
            ResponseEntity<GeminiResponse> response = restTemplate.postForEntity(apiUrl, entity, GeminiResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return extractTextFromGeminiResponse(response.getBody());
            } else {
                log.warn("Gemini API returned non-successful status for song {}: {}", songName, response.getStatusCode());
                return FALLBACK_INTRO_TEXT;
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Gemini API error for song {}. Status: {}, Body: {}", songName, e.getStatusCode(), e.getResponseBodyAsString());
            return FALLBACK_INTRO_TEXT;
        } catch (Exception e) {
            log.error("Unexpected error calling Gemini API for song {}: {}", songName, e.getMessage(), e);
            return FALLBACK_INTRO_TEXT;
        }
    }

    private GeminiRequest buildGeminiRequest(String prompt) {
        Part userMessage = new Part();
        userMessage.setText(prompt);

        Content content = new Content();
        content.setRole("user");
        content.setParts(Collections.singletonList(userMessage));

        GenerationConfig generationConfig = new GenerationConfig();
        generationConfig.setTemperature(0.9);
        generationConfig.setTopK(1);
        generationConfig.setTopP(1);
        generationConfig.setCandidateCount(1);
        generationConfig.setMaxOutputTokens(50); // Set max output tokens to around 30 Chinese characters
        // generationConfig.setStopSequences(Collections.singletonList("\n")); // Example stop sequence

        SafetySetting safetySetting = new SafetySetting();
        safetySetting.setCategory("HARM_CATEGORY_DANGEROUS_CONTENT");
        safetySetting.setThreshold("BLOCK_NONE"); // Or BLOCK_ONLY_HIGH

        GeminiRequest request = new GeminiRequest();
        request.setContents(Collections.singletonList(content));
        request.setGenerationConfig(generationConfig);
        request.setSafetySettings(Collections.singletonList(safetySetting));
        return request;
    }

    private String extractTextFromGeminiResponse(GeminiResponse response) {
        if (response.getCandidates() != null && !response.getCandidates().isEmpty()) {
            Candidate candidate = response.getCandidates().get(0);
            if (candidate.getContent() != null && candidate.getContent().getParts() != null && !candidate.getContent().getParts().isEmpty()) {
                return candidate.getContent().getParts().get(0).getText();
            }
        }
        return FALLBACK_INTRO_TEXT;
    }
}
