package com.trading.controller;

import com.trading.model.Portfolio;
import com.trading.model.Trade;
import com.trading.model.User;
import com.trading.repository.PortfolioRepository;
import com.trading.repository.TradeRepository;
import com.trading.repository.UserRepository;
import com.trading.service.AuthService;
import com.trading.dto.AuthRequest;
import com.trading.config.TenantContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Marks this class as a Spring REST Controller to handle web API requests
@RequestMapping("/api") // All routes in this controller start with "/api"
@CrossOrigin(origins = "*") // Allows API calls from any frontend domain
public class UserController {

    // Repository and Service dependencies for user operations
    private final UserRepository userRepository; // Accessing user data
    private final PortfolioRepository portfolioRepository; // Accessing portfolio data
    private final TradeRepository tradeRepository; // Accessing trade data
    private final AuthService authService; // Security/auth logic

    // Dependency Injection constructor
    public UserController(UserRepository userRepository, 
                          PortfolioRepository portfolioRepository, 
                          TradeRepository tradeRepository,
                          AuthService authService) {
        this.userRepository = userRepository;
        this.portfolioRepository = portfolioRepository;
        this.tradeRepository = tradeRepository;
        this.authService = authService;
    }

    // Endpoint to register a new user
    @PostMapping("/auth/register")
    public User register(@RequestBody AuthRequest request) { // Maps JSON to AuthRequest
        // Calls the AuthService to handle registration logic
        return authService.register(request.getUsername(), request.getPassword(), 
                                  request.getSecurityQuestion(), request.getSecurityAnswer());
    }

    // Endpoint for user login
    @PostMapping("/auth/login")
    public User login(@RequestBody AuthRequest request) {
        // Authenticates user and returns the core User object
        return authService.login(request.getUsername(), request.getPassword());
    }

    // Endpoint to retrieve a user's security question during password reset
    @PostMapping("/auth/forgot-password/question")
    public String getQuestion(@RequestBody AuthRequest request) {
        // Passes username to find their specific question
        return authService.getSecurityQuestion(request.getUsername());
    }

    // Endpoint to reset a password given the correct security answer
    @PostMapping("/auth/forgot-password/reset")
    public void resetPassword(@RequestBody AuthRequest request) {
        // Validates answer and updates password
        authService.resetPassword(request.getUsername(), request.getSecurityAnswer(), request.getPassword());
    }

    // Endpoint to get the current balance for a logged-in user
    @GetMapping("/user/balance")
    public double getBalance(@RequestHeader("X-Tenant-ID") String tenantId) { // Tenant ID identifies the user session
        // Find the user by their ID, or throw an error if missing
        User user = userRepository.findByUsername(tenantId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // Returns only the financial balance
        return user.getBalance();
    }

    // Endpoint to add simulated funds to the user's wallet
    @PostMapping("/user/wallet/add")
    public double addFunds(@RequestBody AddFundsRequest request, @RequestHeader("X-Tenant-ID") String tenantId) {
        // The "master" database has no user wallet, so skip updating if master
        if ("master".equals(tenantId)) return 0.0;
        
        // Find the user using the Tenant ID
        User user = userRepository.findByUsername(tenantId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Validate the deposit amount to ensure it is positive
        if (request.getAmount() <= 0) {
            throw new RuntimeException("Amount must be positive");
        }
        
        // Add the funds to the existing balance
        user.setBalance(user.getBalance() + request.getAmount());
        // Save the updated balance back to the database
        userRepository.save(user);
        // Return the new current balance
        return user.getBalance();
    }

    // Endpoint to get the logged-in user's stock portfolio
    @GetMapping("/user/portfolio")
    public List<Portfolio> getPortfolio() {
        // Tenant is set by Interceptor, so repositories will use the correct DB
        String tenantId = TenantContext.getCurrentTenant();
        // Master DB doesn't own a portfolio, return an empty list
        if ("master".equals(tenantId)) return List.of();
        
        // Load the given user
        User user = userRepository.findByUsername(tenantId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // Fetch all portfolio entries (stocks owned) by this user
        return portfolioRepository.findByUser(user);
    }

    // Endpoint to get the user's historic trades
    @GetMapping("/user/trades")
    public List<Trade> getTrades() {
        // Retrieve tenant ID from context
        String tenantId = TenantContext.getCurrentTenant();
        // Skip for master
        if ("master".equals(tenantId)) return List.of();

        // Get matching user
        User user = userRepository.findByUsername(tenantId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // Fetch all trades where this user was either the buyer or the seller, sorted by date (newest first)
        return tradeRepository.findByBuyOrderUserOrSellOrderUserOrderByCreatedAtDesc(user, user);
    }

    // Static Data Transfer Object (DTO) structure for processing "Add Funds" JSON payload
    public static class AddFundsRequest {
        private double amount; // The amount of money to deposit

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }
    }
}
