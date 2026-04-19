package com.trading.controller;

import com.trading.exception.InsufficientFundsException;
import com.trading.exception.StockNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * MODULE II – Exception Handling: Checked and Unchecked, try, catch, finally,
 *              Propel, Propagate
 *
 * @ControllerAdvice is Spring's cross-cutting exception handler.
 * Instead of wrapping every controller method in a try/catch, we define
 * handlers HERE for each exception type — they are invoked automatically
 * when an exception propagates out of any @RestController method.
 *
 * This pattern demonstrates the PROPAGATE concept (Module II):
 *   Controller method → throws exception → no catch in controller →
 *   exception propagates up → @ControllerAdvice catches it here.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ── CHECKED Exception handler (Module II) ─────────────────────────────────
    // InsufficientFundsException extends Exception — it was caught and re-thrown
    // (propelled) from TradingService, propagating all the way here.
    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientFunds(InsufficientFundsException ex) {
        logger.warn("[CHECKED] Insufficient funds: required={}, available={}",
            ex.getRequired(), ex.getAvailable());

        Map<String, Object> response = new HashMap<>();
        response.put("error",     "INSUFFICIENT_FUNDS");
        response.put("message",   ex.getMessage());
        response.put("required",  ex.getRequired());
        response.put("available", ex.getAvailable());
        response.put("shortfall", ex.getShortfall());
        return new ResponseEntity<>(response, HttpStatus.PAYMENT_REQUIRED); // HTTP 402
    }

    // ── UNCHECKED Exception handler (Module II) ───────────────────────────────
    // StockNotFoundException extends RuntimeException — no 'throws' declaration needed.
    // It propagated automatically from the calling method without being declared.
    @ExceptionHandler(StockNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleStockNotFound(StockNotFoundException ex) {
        logger.warn("[UNCHECKED] Stock not found: symbol={}", ex.getSymbol());

        Map<String, String> response = new HashMap<>();
        response.put("error",   "STOCK_NOT_FOUND");
        response.put("message", ex.getMessage());
        response.put("symbol",  ex.getSymbol());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND); // HTTP 404
    }

    // ── Original handler for IllegalArgumentException ─────────────────────────
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("Trade rejected: {}", ex.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // ── Catch-all for any unhandled exception ─────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        logger.error("Unexpected error occurred", ex);
        Map<String, String> response = new HashMap<>();
        response.put("message", "An unexpected error occurred. Please try again.");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
