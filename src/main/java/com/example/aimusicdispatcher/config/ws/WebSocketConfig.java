package com.example.aimusicdispatcher.config.ws;

import com.example.aimusicdispatcher.connector.DyWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final DyWebSocketHandler dyWebSocketHandler;

    public WebSocketConfig(DyWebSocketHandler dyWebSocketHandler) {
        this.dyWebSocketHandler = dyWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Register the handler at "/ws/dy" and allow all origins
        registry.addHandler(dyWebSocketHandler, "/ws/dy")
                .setAllowedOrigins("*");
    }
}
