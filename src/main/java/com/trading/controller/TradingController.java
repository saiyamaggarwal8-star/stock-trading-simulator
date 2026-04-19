package com.trading.controller;

import com.trading.model.Order;
import com.trading.model.User;
import com.trading.repository.UserRepository;
import com.trading.service.TradingService;
import org.springframework.web.bind.annotation.*;

@RestController // Marks this class as a Spring REST Controller, meaning it will handle HTTP web requests.
@RequestMapping("/api/trade") // Maps all HTTP requests starting with "/api/trade" to this controller.
@CrossOrigin(origins = "*") // Allows Cross-Origin Resource Sharing (CORS) from any domain, allowing the frontend to call this API.
public class TradingController {

    // Dependencies injected into this controller
    private final TradingService tradingService; // Service that handles the business logic for trading
    private final UserRepository userRepository; // Repository for accessing User data from the database

    // Constructor-based dependency injection for the required services
    public TradingController(TradingService tradingService, UserRepository userRepository) {
        this.tradingService = tradingService;
        this.userRepository = userRepository;
    }

    // Maps HTTP POST requests to "/api/trade/order" to this method
    @PostMapping("/order")
    public Order placeOrder(@RequestBody Order order) { // Takes the JSON request body and converts it to an Order object
        // Retrieve the current user's tenant ID from the ThreadLocal context (set by a security/tenant interceptor)
        String tenantId = com.trading.config.TenantContext.getCurrentTenant();
        
        // If the tenant ID is valid and not the "master" database...
        if (tenantId != null && !tenantId.equals("master")) {
            // Find the user by their username (tenantId) and set them as the owner of this order
            userRepository.findByUsername(tenantId).ifPresent(order::setUser);
        }

        // Fallback safety check: If no user was assigned (e.g., during a simple demo/test without proper auth headers)
        if (order.getUser() == null) {
            // Get the first user from the database
            User user = userRepository.findAll().get(0);
            // Assign this first user to the order
            order.setUser(user);
        }
        
        // Pass the fully populated order to the TradingService to process and save it
        return tradingService.placeOrder(order);
    }
}
