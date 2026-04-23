package com.trading.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController // Marks this class as a Spring MVC controller where methods return domain objects instead of views.
public class SystemController {

    // Maps HTTP GET requests to the root path "/" to this status method
    @GetMapping("/")
    public Map<String, String> status() {
        // Create a new HashMap to hold the status key-value pairs
        Map<String, String> status = new HashMap<>();
        // Add a "status" entry indicating the system is "UP"
        status.put("status", "UP");
        // Add a descriptive "message" entry
        status.put("message", "Trading Simulator Backend is running");
        // Add an "apiVersion" entry indicating the current API version
        status.put("apiVersion", "1.0");
        // Return the map, which Spring will automatically convert to a JSON response
        return status;
    }
}
