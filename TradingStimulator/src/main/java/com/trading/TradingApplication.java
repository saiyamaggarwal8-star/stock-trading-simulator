package com.trading;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * MODULE I  – Platform Independence of Java (JVM)
 * MODULE III – JEE (client-server architecture), Microservices Architecture,
 *              Startups on programming (ApplicationReadyEvent), Session Management
 * MODULE IV – Spring MVC framework, Microservices with Spring Boot
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * JVM & Platform Independence (Module I):
 * ─────────────────────────────────────────────────────────────────────────────
 * Java source (.java) → javac → Bytecode (.class) → JVM interprets on any OS.
 * The same compiled JAR runs on Windows, Linux, and macOS without recompilation.
 * The JVM also handles:
 *   - Memory management (heap/stack allocation)
 *   - Garbage Collection (automatic deallocation — no destructors needed)
 *   - Class loading (lazy loading of .class files)
 *   - JIT (Just-In-Time) compilation for performance optimisation
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * Spring MVC Framework (Module IV):
 * ─────────────────────────────────────────────────────────────────────────────
 * Spring MVC implements the Model-View-Controller pattern for web applications.
 *   Model      → JPA Entities (@Entity classes in com.trading.model)
 *   View       → JSON responses (REST APIs consumed by React frontend)
 *   Controller → @RestController classes in com.trading.controller
 *
 * The DispatcherServlet (registered by Spring Boot automatically) acts as the
 * Front Controller — ONE entry point that routes all HTTP requests.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * Microservices Architecture (Module III & IV):
 * ─────────────────────────────────────────────────────────────────────────────
 * This Spring Boot application IS a self-contained microservice:
 *   - Embedded Tomcat server (no external app server needed)
 *   - Single deployable JAR (fat jar with all dependencies)
 *   - Exposes REST APIs consumed by the React frontend (separate service)
 *   - Spring Actuator endpoints: /actuator/health, /actuator/info
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * JEE Client-Server Architecture (Module III):
 * ─────────────────────────────────────────────────────────────────────────────
 *   Client (React, port 9000) ─── HTTP/WebSocket ──→ Server (Spring Boot, port 8081)
 *                                                       ↓
 *                                              Embedded Tomcat (Servlet Container)
 *                                                       ↓
 *                                              DispatcherServlet (Front Controller)
 *                                                       ↓
 *                                              @RestController / @Controller
 *                                                       ↓
 *                                              @Service (Business Logic)
 *                                                       ↓
 *                                              @Repository (JPA / H2 / MySQL)
 */
@SpringBootApplication
@EnableScheduling  // Enables @Scheduled methods (MarketSimulator, PriceWebSocketController)
public class TradingApplication {

    public static void main(String[] args) {
        // SpringApplication.run() bootstraps the entire Spring IoC container,
        // starts the embedded Tomcat server, and registers all @Component beans.
        SpringApplication.run(TradingApplication.class, args);
    }

    /**
     * MODULE III – Startups on programming (ApplicationReadyEvent).
     *
     * @EventListener(ApplicationReadyEvent.class) fires AFTER the application
     * has fully started — all beans are wired, Tomcat is listening.
     * This is safer than @PostConstruct for startup actions that depend
     * on the full application context being available.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║   Trading Simulator — Spring Boot Microservice  ║");
        System.out.println("║   API:       http://localhost:8081/api           ║");
        System.out.println("║   WebSocket: ws://localhost:8081/ws              ║");
        System.out.println("║   Actuator:  http://localhost:8081/actuator      ║");
        System.out.println("║   H2 Console:http://localhost:8081/h2-console    ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
    }
}
