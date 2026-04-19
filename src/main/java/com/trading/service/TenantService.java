package com.trading.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@Service
public class TenantService {

    @Value("${spring.datasource.url}")
    private String masterUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    // Verifies whether we are running an in-memory test database (H2) or a real SQL server 
    private boolean isH2() {
        return masterUrl != null && masterUrl.startsWith("jdbc:h2");
    }

    // Provisions a physically separate database for complete user data isolation (Tenant)
    public void createTenantDatabase(String tenantId) {
        // H2 in-memory DB doesn't support per-tenant CREATE DATABASE
        // Skip silently when running locally with H2 to avoid crashing
        if (isH2()) {
            System.out.println("H2 mode: skipping tenant DB creation for " + tenantId);
            return;
        }

        // Formulate the new isolated database name unique to the tenant
        String dbName = "trading_simulator_" + tenantId;
        // Parse the base connection string from the master DB properties
        String baseUrl = masterUrl.substring(0, masterUrl.indexOf("?") != -1 ? masterUrl.indexOf("?") : masterUrl.length());
        baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/") + 1);
        
        // Open raw connection to the main DBMS server to run creation commands
        try (Connection conn = DriverManager.getConnection(baseUrl, username, password);
             Statement stmt = conn.createStatement()) {
            
            // Execute SQL to construct a fresh dedicated database schema
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
            System.out.println("Created database: " + dbName);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create tenant database: " + dbName, e);
        }
    }

    // Wires a newly created Database into a valid Spring Data Source Object for runtime switching
    public DataSource createDataSource(String tenantId) {
        // In H2 mode, all tenants share the same in-memory database configuration implicitly
        if (isH2()) {
            return DataSourceBuilder.create()
                    .url(masterUrl)
                    .username(username)
                    .password(password)
                    .build();
        }

        // Map standard URL replacing standard DB with tenant DB
        String dbName = "trading_simulator_" + tenantId;
        String url = masterUrl.replaceFirst("/[^/?]+(\\?|$)", "/" + dbName + "$1");
        
        // Factory build a pooled DataSource hook for the new URL
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .build();
    }
}
