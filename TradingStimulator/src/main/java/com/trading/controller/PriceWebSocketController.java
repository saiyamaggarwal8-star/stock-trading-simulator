package com.trading.controller;

import com.trading.model.Stock;
import com.trading.repository.StockRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * MODULE IV – WebSockets, Spring MVC framework, Lambda expressions,
 *             Collection framework (HashMap), Microservices with Spring Boot
 * MODULE III – Session Management (subscribe/unsubscribe), JEE client-server
 *
 * PriceWebSocketController pushes live stock prices to all subscribed frontend
 * clients via the STOMP message broker configured in WebSocketConfig.
 *
 * Spring MVC (Module IV):
 *   @Controller handles WebSocket messages just like @RestController handles HTTP.
 *   @MessageMapping is the WebSocket equivalent of @GetMapping/@PostMapping.
 *   @SendTo broadcasts the return value to all subscribers of that topic.
 *
 * Lambda expressions (Module IV) are used extensively in stream operations.
 * Function<T,R> is a functional interface: takes T, returns R — used below.
 */
@Controller
public class PriceWebSocketController {

    private final StockRepository  stockRepository;
    private final SimpMessagingTemplate messagingTemplate; // Used to push from server → clients

    public PriceWebSocketController(StockRepository stockRepository,
                                     SimpMessagingTemplate messagingTemplate) {
        this.stockRepository  = stockRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // ── @MessageMapping: handles messages sent BY clients TO the server ────────
    // Client sends: stompClient.send("/app/subscribe", {}, "RELIANCE")
    // Returns response to: /topic/prices/subscribe
    @MessageMapping("/subscribe")
    @SendTo("/topic/prices/subscribe")
    public Map<String, Object> handleSubscription(String symbol) {
        // ── Lambda with Function<T,R> (Module IV) ────────────────────────────
        // Function<String, Map<String,Object>> transforms a symbol → a price snapshot map.
        Function<String, Map<String, Object>> buildSnapshot = sym -> {
            Map<String, Object> snapshot = new HashMap<>();
            stockRepository.findBySymbol(sym.trim().toUpperCase()).ifPresent(stock -> {
                snapshot.put("symbol",      stock.getSymbol());
                snapshot.put("price",       stock.getCurrentPrice());
                snapshot.put("change",      stock.getPercentChange());
                snapshot.put("companyName", stock.getCompanyName());
            });
            return snapshot;
        };

        return buildSnapshot.apply(symbol); // Apply the lambda function
    }

    // ── Scheduled push: broadcasts ALL prices every 5 seconds ─────────────────
    // This is the PRIMARY price feed — no client request needed.
    // All clients subscribed to /topic/prices/live receive these updates.
    @Scheduled(fixedRate = 5000) // Every 5 seconds
    public void broadcastAllPrices() {
        // Fetch all active stocks
        List<Stock> stocks = stockRepository.findAll().stream()
            .filter(s -> !s.getSymbol().endsWith("_OLD"))
            .collect(Collectors.toList());

        // ── Lambda + Stream + HashMap (Module IV) ────────────────────────────
        // Convert each Stock into a Map payload using a lambda expression.
        List<Map<String, Object>> payload = stocks.stream()
            .map(stock -> {                               // Lambda: Stock → Map
                Map<String, Object> entry = new HashMap<>();
                entry.put("symbol",  stock.getSymbol());
                entry.put("price",   stock.getCurrentPrice());
                entry.put("change",  stock.getPercentChange());
                entry.put("name",    stock.getCompanyName());
                return entry;
            })
            .collect(Collectors.toList());

        // ── Push to all subscribed clients (Module IV WebSocket) ──────────────
        // convertAndSend() sends the payload to every client subscribed to the topic.
        messagingTemplate.convertAndSend("/topic/prices/live", payload);
    }
}
