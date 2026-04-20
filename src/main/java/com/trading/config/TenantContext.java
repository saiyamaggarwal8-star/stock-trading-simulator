package com.trading.config;

/**
 * MODULE III – Session Management, Microservices Architecture, JEE
 * MODULE IV  – Struts framework (architectural context), Spring MVC
 *
 * TenantContext uses ThreadLocal to store the current user's tenant ID
 * per HTTP request thread — a clean, thread-safe alternative to passing
 * the tenant ID through every method call.
 *
 * ─────────────────────────────────────────────────────────────────────────
 * MODULE III – Swing features & JavaFX Features (Context note):
 * ─────────────────────────────────────────────────────────────────────────
 * Swing and JavaFX are Java's desktop GUI toolkits:
 *
 *  Swing  (javax.swing.*)
 *    - Original Java GUI toolkit (JFrame, JPanel, JButton, JTable)
 *    - Event-Driven: ActionListener callbacks run on the Event Dispatch Thread (EDT)
 *    - NOT thread-safe: ALL UI updates must happen on the EDT via
 *      SwingUtilities.invokeLater(() -> label.setText(price));
 *    - In a desktop trading app, JTable would render the stock list;
 *      JFrame would host the main window layout.
 *
 *  JavaFX  (javafx.*)
 *    - Modern replacement for Swing (FXML layouts, CSS styling)
 *    - Observable Properties with data binding (ObservableList<Stock>)
 *    - Platform.runLater() is the JavaFX equivalent of invokeLater()
 *    - In this simulator, JavaFX would bind the price label directly to
 *      the stock's priceProperty so the UI auto-updates on price changes.
 *
 * This Spring Boot application is a SERVER-SIDE web app (REST + WebSocket),
 * not a desktop app, so Swing/JavaFX are not instantiated here. However,
 * the concepts of:
 *   - Event-driven programming (ActionListener / EventListener)
 *   - Thread safety of UI updates (EDT / Platform.runLater)
 *   - Observable data binding
 * are directly applicable to the React frontend's event handlers.
 *
 * ─────────────────────────────────────────────────────────────────────────
 * MODULE IV – Struts Framework (Architectural Context):
 * ─────────────────────────────────────────────────────────────────────────
 * Apache Struts is a Java MVC web framework that PREDATES Spring MVC.
 * Comparison with this project's Spring Boot stack:
 *
 *  | Concept          | Struts                         | This Project (Spring MVC)       |
 *  |──────────────────|────────────────────────────────|─────────────────────────────────|
 *  | Front Controller | ActionServlet                  | DispatcherServlet               |
 *  | Action           | Action / ActionSupport classes | @RestController methods         |
 *  | Configuration    | struts-config.xml / struts.xml | @Configuration / application.yml|
 *  | View             | JSP / Tiles                    | JSON (consumed by React)        |
 *  | Form beans       | ActionForm                     | @RequestBody DTOs               |
 *  | Validation       | validate() in ActionForm       | @Valid + BindingResult          |
 *
 * Both implement the MVC pattern. Spring MVC is the modern industry standard;
 * Struts is legacy but foundational to understanding Java web architecture.
 *
 * ─────────────────────────────────────────────────────────────────────────
 * ThreadLocal — Session Management per request thread:
 * ─────────────────────────────────────────────────────────────────────────
 * Each incoming HTTP request is handled by a new thread from Tomcat's pool.
 * ThreadLocal<T> stores a value that is ISOLATED to a specific thread —
 * like a per-thread session variable without database overhead.
 *
 * Flow:
 *   1. TenantInterceptor.preHandle()  → TenantContext.setCurrentTenant(id)
 *   2. @Service / @Repository code   → TenantContext.getCurrentTenant()
 *   3. TenantInterceptor.afterCompletion() → TenantContext.clear()
 *
 * IMPORTANT: clear() MUST be called after every request to prevent
 * thread-pool thread reuse from leaking one user's context to another.
 */
public class TenantContext {

    // ThreadLocal holds one String per thread — fully isolated, no synchronization needed
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    /** Returns the tenant ID set for the current request thread */
    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    /** Sets the tenant ID for the current request thread */
    public static void setCurrentTenant(String tenant) {
        CURRENT_TENANT.set(tenant);
    }

    /**
     * Clears the tenant ID from the current thread.
     * MUST be called at the end of every request to avoid context leakage
     * across thread-pool thread reuse.
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
