package com.trading.exception;

/**
 * MODULE II – Exception Handling: Checked Exception
 *             try, catch, finally, Propagate, Propel
 *
 * InsufficientFundsException is a CHECKED exception.
 *
 * Checked vs Unchecked:
 * ─────────────────────────────────────────────────────────────────────────
 * Checked (extends Exception):
 *   • Must be declared in 'throws' clause OR caught with try/catch.
 *   • Compiler ENFORCES handling — you cannot ignore it.
 *   • Use for recoverable, predictable failure scenarios.
 *   • Example: file not found, network timeout, insufficient funds.
 *
 * Unchecked (extends RuntimeException):
 *   • No mandatory declaration or catch block required.
 *   • Use for programming errors or unrecoverable situations.
 *   • Example: NullPointerException, StockNotFoundException (see other file).
 * ─────────────────────────────────────────────────────────────────────────
 *
 * Propagation (Propel / Propagate — Module II):
 *   If a method does not catch this exception, it PROPAGATES up the call stack
 *   until something catches it or the JVM terminates. The 'throws' keyword
 *   in a method signature declares intentional propagation.
 */
public class InsufficientFundsException extends Exception { // extends Exception → CHECKED

    // Extra fields for contextual error information
    private final double required;
    private final double available;

    // ── Constructor 1: message only ───────────────────────────────────────────
    public InsufficientFundsException(String message) {
        super(message); // Calls Exception(String message) constructor
        this.required  = 0;
        this.available = 0;
    }

    // ── Constructor 2: message + contextual financial details ─────────────────
    public InsufficientFundsException(double required, double available) {
        // Calls Exception(String) with a formatted message
        super(String.format(
            "Insufficient funds: required ₹%.2f but only ₹%.2f available.",
            required, available
        ));
        this.required  = required;
        this.available = available;
    }

    // ── Constructor 3: wrapping a cause (for exception chaining / Propel) ─────
    // Used when catching one exception and "re-throwing" it as this type.
    // This preserves the original stack trace — important for debugging.
    public InsufficientFundsException(String message, Throwable cause) {
        super(message, cause); // 'cause' is the original exception that triggered this
        this.required  = 0;
        this.available = 0;
    }

    // ── Getters for the contextual fields ────────────────────────────────────
    public double getRequired()  { return required; }
    public double getAvailable() { return available; }

    /** Shortfall amount — calculated from contextual data */
    public double getShortfall() {
        return Math.max(0, required - available);
    }
}
