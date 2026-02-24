package com.example.aimusicdispatcher.connector;

import com.example.aimusicdispatcher.model.dy.DyMessage;
import com.example.aimusicdispatcher.dispatcher.MessageDispatcher;
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
    private final MessageDispatcher messageDispatcher;

    public DyWebSocketHandler(ObjectMapper objectMapper, MessageDispatcher messageDispatcher) {
        this.objectMapper = objectMapper;
        this.messageDispatcher = messageDispatcher;
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
                    log.info("Parsed DyMessage in handleTextMessage: method={}, userId={}, content={}", 
                            dyMessage.getMethod(), 
                            dyMessage.getUser() != null ? dyMessage.getUser().getId() : "unknown",
                            dyMessage.getContent());
                    processMessage(dyMessage, session);
                }
            } else {
                DyMessage dyMessage = objectMapper.treeToValue(jsonNode, DyMessage.class);
                log.info("Parsed DyMessage in handleTextMessage: method={}, userId={}, content={}", 
                            dyMessage.getMethod(), 
                            dyMessage.getUser() != null ? dyMessage.getUser().getId() : "unknown",
                            dyMessage.getContent());
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

        // 将消息分发给 MessageDispatcher 处理
        messageDispatcher.dispatch(dyMessage);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: {}, status: {}", session.getId(), status);
    }
}
