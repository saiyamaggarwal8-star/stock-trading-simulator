package com.trading.exception;

/**
 * MODULE II – Exception Handling: Unchecked Exception
 *             Checked and unchecked exception distinction
 *
 * StockNotFoundException is an UNCHECKED exception.
 *
 * Unchecked exceptions extend RuntimeException.
 * They do NOT need to be declared in a 'throws' clause.
 * The compiler does not force you to catch them.
 * They represent programming errors or situations that generally
 * cannot be meaningfully handled at the calling site.
 *
 * Usage in the trading simulator:
 *   Thrown when a stock symbol cannot be located in the database.
 *   This is typically caused by a bad request (invalid symbol),
 *   so it is treated as a programming/client error — not recoverable.
 */
public class StockNotFoundException extends RuntimeException { // extends RuntimeException → UNCHECKED

    private final String symbol; // The symbol that was not found

    // ── Constructor: symbol only ──────────────────────────────────────────────
    public StockNotFoundException(String symbol) {
        super("Stock not found: '" + symbol + "'. Please check the symbol and try again.");
        this.symbol = symbol;
    }

    // ── Constructor: symbol + cause (exception chaining) ─────────────────────
    // Used when wrapping a lower-level exception (e.g., JPA EmptyResultDataAccessException)
    // into our more descriptive domain exception.
    public StockNotFoundException(String symbol, Throwable cause) {
        super("Stock not found: '" + symbol + "'.", cause);
        this.symbol = symbol;
    }

    /** Returns the symbol that caused the exception */
    public String getSymbol() {
        return symbol;
    }
}
