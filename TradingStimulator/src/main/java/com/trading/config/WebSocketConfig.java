package com.trading.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * MODULE IV – WebSockets, JEE (client-server architecture),
 *             Microservices with Spring Boot, Spring MVC framework
 *
 * WebSocket enables FULL-DUPLEX (bidirectional) persistent communication
 * between the client and server — unlike HTTP which is request-response only.
 *
 * Why WebSockets for trading?
 * ─────────────────────────────────────────────────────────────────────────
 * HTTP (traditional):   Client polls server repeatedly → wasteful, laggy
 * WebSocket:            Server PUSHes price updates to client instantly
 *
 * STOMP (Simple Text Oriented Messaging Protocol) runs over WebSocket.
 * Spring Boot's @EnableWebSocketMessageBroker sets up:
 *   - An in-memory message broker (for fan-out to subscribers)
 *   - A STOMP endpoint for client connection
 *   - Application destination prefix for routing messages to controllers
 *
 * Architecture (JEE/Microservices context):
 *   React Frontend ──WebSocket──> /ws (STOMP endpoint, this config)
 *                                  ↓
 *                          Message Broker (in-memory)
 *                                  ↓
 *                    /topic/prices ←─ PriceWebSocketController pushes
 *
 * Spring MVC Framework (Module IV):
 * Spring Boot IS a Spring MVC application. The DispatcherServlet routes
 * HTTP requests to @RestController, just like a traditional Spring MVC app.
 * WebSocket messages are dispatched to @MessageMapping methods similarly.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // ── Configure the message broker (in-memory for this app) ─────────────────
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable an in-memory simple message broker with topic prefix
        // Clients subscribe to /topic/prices to receive live stock updates
        config.enableSimpleBroker("/topic");

        // All messages sent FROM clients start with /app
        // They are routed to @MessageMapping methods in controllers
        config.setApplicationDestinationPrefixes("/app");
    }

    // ── Register the STOMP WebSocket endpoint ─────────────────────────────────
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Clients connect to ws://localhost:8081/ws
        // .withSockJS() adds fallback for browsers that don't support native WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // Allow any frontend origin (CORS)
                .withSockJS();                   // SockJS fallback transport
    }
}
