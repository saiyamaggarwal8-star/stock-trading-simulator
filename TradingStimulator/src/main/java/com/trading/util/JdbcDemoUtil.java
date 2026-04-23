package com.trading.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * MODULE III – MySQL database, getConnection, createStatement, executeQuery,
 *              NoSQL Databases (reference), try/catch/finally (Module II)
 *
 * Demonstrates raw JDBC (Java Database Connectivity) — the low-level API
 * that underlies Spring Data JPA.  JPA/Hibernate generates these SQL calls
 * automatically; here we perform them manually for educational purposes.
 *
 * JDBC workflow:
 *   1. Load the driver (modern JDBC auto-loads via Service Provider)
 *   2. DriverManager.getConnection()  → opens a Connection to the DB
 *   3. connection.createStatement()   → creates a Statement
 *   4. statement.executeQuery(sql)    → returns a ResultSet
 *   5. Iterate ResultSet to read data
 *   6. Close resources in a finally block (or use try-with-resources)
 *
 * NOTE: This class is intentionally NOT a Spring @Component — it is a pure
 * JDBC demonstration utility that can be invoked from tests or CLI tools.
 */
public class JdbcDemoUtil {

    // ── Connection parameters (would normally come from application.properties) ─
    private static final String DB_URL  = "jdbc:h2:mem:trading_simulator";
    private static final String DB_USER = "sa";
    private static final String DB_PASS = "";

    /**
     * Demonstrates: getConnection, createStatement, executeQuery, ResultSet.
     *
     * NoSQL context: Unlike JDBC which operates on relational (SQL) databases
     * such as MySQL, H2, PostgreSQL — NoSQL databases (MongoDB, Redis, Cassandra)
     * use different drivers and APIs (MongoClient, Jedis, etc.) and do NOT use
     * the JDBC interface. Spring provides Spring Data MongoDB / Redis Repositories
     * as the equivalent abstraction layer.
     */
    public static void demonstrateJdbcQuery() {
        Connection  conn  = null;
        Statement   stmt  = null;
        ResultSet   rs    = null;

        try {
            // ── Step 1: getConnection (Module III) ──────────────────────────
            // DriverManager tries all registered JDBC drivers in order.
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("[JDBC] Connected to: " + conn.getMetaData().getURL());

            // ── Step 2: createStatement (Module III) ────────────────────────
            stmt = conn.createStatement();

            // ── Step 3: executeQuery — returns a ResultSet (Module III) ─────
            rs = stmt.executeQuery("SELECT symbol, current_price FROM stocks LIMIT 5");

            System.out.println("[JDBC] Stock Prices:");
            // Iterate through rows of the ResultSet
            while (rs.next()) {
                String symbol = rs.getString("symbol");          // Read column by name
                double price  = rs.getDouble("current_price");
                System.out.printf("  %-15s ₹%.2f%n", symbol, price);
            }

        } catch (SQLException ex) {
            // Checked exception — must be caught or declared in throws clause (Module II)
            System.err.println("[JDBC] SQL Error: " + ex.getMessage() + " [Code: " + ex.getErrorCode() + "]");

        } finally {
            // ── finally block — always runs, even if an exception occurred ──
            // Critical for closing DB resources and preventing connection leaks.
            closeQuietly(rs, stmt, conn);
            System.out.println("[JDBC] Resources closed in finally block.");
        }
    }

    /**
     * Demonstrates PreparedStatement — the safe, parameterised form of SQL.
     * Prevents SQL Injection attacks (security — Module I security concepts).
     */
    public static void demonstratePreparedStatement(String symbolToFind) {
        // try-with-resources: modern Java alternative to manually closing in finally.
        // Automatically calls close() on each resource when the block exits.
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT symbol, current_price FROM stocks WHERE symbol = ?")) {

            // ── Set parameter safely (prevents SQL injection) ─────────────
            ps.setString(1, symbolToFind); // The '?' placeholder is filled here

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("[JDBC] Found: " + rs.getString("symbol")
                        + " @ ₹" + rs.getDouble("current_price"));
                } else {
                    System.out.println("[JDBC] Symbol not found: " + symbolToFind);
                }
            }

        } catch (SQLException ex) {
            System.err.println("[JDBC] PreparedStatement error: " + ex.getMessage());
        }
    }

    // ── Private helper: close resources silently ──────────────────────────────
    private static void closeQuietly(ResultSet rs, Statement stmt, Connection conn) {
        try { if (rs   != null) rs.close();   } catch (SQLException ignored) {}
        try { if (stmt != null) stmt.close();  } catch (SQLException ignored) {}
        try { if (conn != null) conn.close();  } catch (SQLException ignored) {}
    }
}
