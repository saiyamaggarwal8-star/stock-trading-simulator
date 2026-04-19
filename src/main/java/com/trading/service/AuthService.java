package com.trading.service;

import com.trading.model.User;
import com.trading.repository.UserRepository;
import com.trading.util.PasswordUtil;
import com.trading.config.TenantContext;
import com.trading.config.RoutingDataSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.sql.DataSource;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TenantService tenantService;
    private final RoutingDataSource routingDataSource;
    private final DataInitializer dataInitializer;

    public AuthService(UserRepository userRepository, 
                       TenantService tenantService, 
                       RoutingDataSource routingDataSource,
                       DataInitializer dataInitializer) {
        this.userRepository = userRepository;
        this.tenantService = tenantService;
        this.routingDataSource = routingDataSource;
        this.dataInitializer = dataInitializer;
    }

    @Transactional // Rolls back the entire account creation if any step fails
    public User register(String username, String password, String securityQuestion, String securityAnswer) {
        // Enforce working in the master database for user core records
        TenantContext.setCurrentTenant("master");
        
        // Check if username is already taken
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        // 1. Create User in Master DB with a starting virtual balance of ₹100,000
        User user = new User(username, PasswordUtil.hashPassword(password), 100000.0, securityQuestion, securityAnswer);
        user = userRepository.save(user);

        // 2. Create an isolated sub-database (Tenant DB) strictly for this user's simulated trading activity
        tenantService.createTenantDatabase(username);

        // 3. Register the new Database Source so the app can route to it dynamically
        DataSource tenantDS = tenantService.createDataSource(username);
        routingDataSource.addDataSource(username, tenantDS);

        // 4. Temporarily switch context to the newly created user DB
        TenantContext.setCurrentTenant(username);
        // Seed initial stocks into this isolated tenant for the user to trade
        dataInitializer.seed();

        return user;
    }

    // Handles user login and data source mounting verification
    public User login(String username, String password) {
        // Must check master database for credentials
        TenantContext.setCurrentTenant("master");
        
        // Find user or fail
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        // Compare plain text password vs stored encrypted hash securely
        if (!PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Ensure user's personal database context is active in the routing mapper for this session
        if (!routingDataSource.hasDataSource(username)) {
            DataSource tenantDS = tenantService.createDataSource(username);
            routingDataSource.addDataSource(username, tenantDS);
        }

        return user;
    }

    // Fetches the security question safely without exposing the answer
    public String getSecurityQuestion(String username) {
        TenantContext.setCurrentTenant("master");
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Username not found"));
        return user.getSecurityQuestion();
    }

    @Transactional
    public void resetPassword(String username, String answer, String newPassword) {
        // Lookup user in master DB
        TenantContext.setCurrentTenant("master");
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Username not found"));

        // Validate the provided answer against the stored answer (case-insensitive)
        if (user.getSecurityAnswer() == null || !user.getSecurityAnswer().equalsIgnoreCase(answer)) {
            throw new RuntimeException("Incorrect security answer");
        }

        // Apply new hashed password
        user.setPasswordHash(PasswordUtil.hashPassword(newPassword));
        userRepository.save(user);
    }
}
