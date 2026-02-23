package com.example.aimusicdispatcher.connector;

import com.example.aimusicdispatcher.model.dy.CastMethod;
import com.example.aimusicdispatcher.model.barrage.BarrageRequest;
import com.example.aimusicdispatcher.model.dy.DyMessage;
import com.example.aimusicdispatcher.service.BarrageService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class DyWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final BarrageService barrageService;

    public DyWebSocketHandler(ObjectMapper objectMapper, BarrageService barrageService) {
        this.objectMapper = objectMapper;
        this.barrageService = barrageService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("=== Received WebSocket message ===");
        log.info("Raw payload: {}", payload);
        
        try {
            String prettyPayload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(payload));
            log.info("Formatted payload:\n{}", prettyPayload);
        } catch (Exception e) {
            // If formatting fails, just log the raw payload
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            if (jsonNode.isArray()) {
                List<DyMessage> messages = objectMapper.convertValue(jsonNode, new TypeReference<List<DyMessage>>() {});
                for (DyMessage dyMessage : messages) {
                    processMessage(dyMessage, session);
                }
            } else {
                DyMessage dyMessage = objectMapper.treeToValue(jsonNode, DyMessage.class);
                processMessage(dyMessage, session);
            }
        } catch (Exception e) {
            log.error("Error parsing WebSocket message", e);
            session.sendMessage(new TextMessage("Error: Invalid JSON format"));
        }
    }

    private void processMessage(DyMessage dyMessage, WebSocketSession session) {
        log.info("=== Processing DyMessage ===");
        try {
            String dyMessageJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dyMessage);
            log.info("DyMessage object:\n{}", dyMessageJson);
        } catch (Exception e) {
            log.error("Error serializing DyMessage", e);
        }
        
        log.info("Processing DyMessage: id={}, method={}, user={}", 
                dyMessage.getId(), dyMessage.getMethod(), dyMessage.getUser() != null ? dyMessage.getUser().getName() : "unknown");

        if (dyMessage.getMethod() == null) {
            log.warn("Message method is null, ignoring.");
            return;
        }

        switch (dyMessage.getMethod()) {
            case CHAT:
                handleChatMessage(dyMessage);
                break;
            case GIFT:
                handleGiftMessage(dyMessage);
                break;
            case LIKE:
                handleLikeMessage(dyMessage);
                break;
            // Add other cases as needed
            default:
                log.debug("Unhandled message type: {}", dyMessage.getMethod());
        }
    }

    private void handleChatMessage(DyMessage message) {
        String userName = message.getUser() != null ? message.getUser().getName() : "Anonymous";
        log.info("Chat message from {}: {}", userName, message.getContent());
        
        BarrageRequest request = new BarrageRequest();
        request.setUser(userName);
        request.setContent(message.getContent());
        request.setTimestamp(System.currentTimeMillis());
        
        barrageService.processBarrage(request);
    }

    private void handleGiftMessage(DyMessage message) {
        if (message.getGift() != null) {
            log.info("Gift received from {}: {} x {}", 
                    message.getUser() != null ? message.getUser().getName() : "Anonymous", 
                    message.getGift().getName(), 
                    message.getGift().getCount());
        }
    }

    private void handleLikeMessage(DyMessage message) {
        log.info("Like received from {}", message.getUser() != null ? message.getUser().getName() : "Anonymous");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: {}, status: {}", session.getId(), status);
    }
}
