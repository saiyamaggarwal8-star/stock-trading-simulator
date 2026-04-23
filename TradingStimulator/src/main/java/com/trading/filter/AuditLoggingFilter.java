package com.trading.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * MODULE III – Generic Servlet, HTTP Servlet, Servlet Config, Web Filters,
 *              Servlet to handle GET and POST Methods, Session Management
 *              JEE (client-server architecture for web based applications)
 *
 * AuditLoggingFilter implements the Servlet Filter interface — the Spring Boot
 * equivalent of a Java EE HttpFilter / GenericFilter.
 *
 * JEE/Servlet Architecture explained:
 * ─────────────────────────────────────────────────────────────────────────
 * In classic JEE:
 *   Client (Browser) → HTTP Request → Servlet Container (Tomcat)
 *                    → Filter Chain → Servlet.service() → Response
 *
 * In Spring Boot:
 *   Client → HTTP Request → Embedded Tomcat → Filter Chain (this class)
 *          → DispatcherServlet → @RestController → Response
 *
 * The Filter interface is the SAME jakarta.servlet.Filter from the JEE spec.
 * Spring Boot registers @Component filters automatically into the filter chain.
 *
 * Concepts from the syllabus demonstrated here:
 *   - Filter (= Web Filter Servlet — Module III)
 *   - HttpServletRequest / HttpServletResponse (= HttpServlet concept)
 *   - FilterChain.doFilter() (= passing to next Servlet in the chain)
 *   - Reading GET/POST method from the request
 *   - Session ID extraction (Session Management — Module III)
 *   - FilterConfig (lifecycle method init(FilterConfig))
 */
@Component // Spring auto-registers this as a Servlet Filter
public class AuditLoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(AuditLoggingFilter.class);

    // ── Filter lifecycle: init (Servlet Config concept) ───────────────────────
    // Called ONCE when the filter is first created by the container.
    // FilterConfig provides access to init parameters (analogous to web.xml config).
    @Override
    public void init(FilterConfig filterConfig) {
        // The filter name is available via filterConfig — equivalent to servlet-name in JEE
        logger.info("[Filter] AuditLoggingFilter initialised. Filter name: {}",
            filterConfig.getFilterName());
    }

    // ── doFilter: called for EVERY HTTP request (GET, POST, PUT, etc.) ────────
    // This is the core of the Filter pattern — the most important method.
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Cast to HTTP-specific types to access HTTP-level information
        HttpServletRequest  httpRequest  = (HttpServletRequest)  request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // ── Reading HTTP Method (GET/POST) — Module III ───────────────────────
        String method = httpRequest.getMethod();   // e.g., "GET", "POST", "PUT", "DELETE"
        String uri    = httpRequest.getRequestURI(); // e.g., "/api/market/stocks"
        long   start  = System.currentTimeMillis();

        // ── Session Management (Module III) ──────────────────────────────────
        // getSession(false) returns the existing session without creating a new one.
        // Returns null if no session exists.
        // In this app, auth is header-based (X-Tenant-ID), but we log the session ID
        // if one happens to exist (OAuth2 sessions, etc.)
        String sessionId = "none";
        if (httpRequest.getSession(false) != null) {
            sessionId = httpRequest.getSession(false).getId();
        }

        // ── Read custom auth header used for tenant routing ───────────────────
        String tenantId = httpRequest.getHeader("X-Tenant-ID");
        if (tenantId == null) tenantId = "anonymous";

        // Log request details BEFORE passing to the next filter/servlet
        logger.info("[AUDIT] → {} {} | User: {} | Session: {}", method, uri, tenantId, sessionId);

        // ── Handle GET and POST differently ──────────────────────────────────
        // This mirrors the concept of HttpServlet.doGet() / doPost() from JEE
        if ("GET".equalsIgnoreCase(method)) {
            // GET: read-only, log query string if any
            String queryString = httpRequest.getQueryString();
            if (queryString != null) {
                logger.debug("[AUDIT] GET params: {}", queryString);
            }
        } else if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
            // POST/PUT: mutating, log content type for awareness
            logger.debug("[AUDIT] {} body content-type: {}", method, httpRequest.getContentType());
        }

        // ── FilterChain.doFilter — pass to the next filter or servlet ─────────
        // This is MANDATORY — without it, the request never reaches the controller.
        chain.doFilter(request, response);

        // ── Post-processing: log response status ──────────────────────────────
        long elapsed = System.currentTimeMillis() - start;
        int  status  = httpResponse.getStatus();
        logger.info("[AUDIT] ← {} {} | Status: {} | {}ms", method, uri, status, elapsed);
    }

    // ── Filter lifecycle: destroy ─────────────────────────────────────────────
    // Called once when the container shuts down or the filter is removed.
    @Override
    public void destroy() {
        logger.info("[Filter] AuditLoggingFilter destroyed (container shutdown).");
    }
}
