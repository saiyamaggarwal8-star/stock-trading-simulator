package com.trading.service;

/**
 * MODULE II – Interfaces, Interface vs Abstract Class
 *
 * An interface defines a PURE contract — only method signatures (and default/static helpers).
 * It cannot hold instance state, unlike an abstract class.
 *
 * Key differences from an Abstract Class:
 * ─────────────────────────────────────────────────────────────────────────────
 * | Feature              | Interface (this file) | Abstract Class (Instrument) |
 * |──────────────────────|───────────────────────|─────────────────────────── |
 * | Instance variables   | ✗ (constants only)    | ✓                          |
 * | Constructors         | ✗                     | ✓                          |
 * | Multiple inheritance | ✓ (a class can        | ✗ (only one extends)       |
 * |                      |   implement many)     |                            |
 * | Default methods      | ✓ (since Java 8)      | ✓                          |
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * 'Tradeable' marks any domain object that can be bought or sold in the simulator.
 * Both Stock and ETF implement this interface.
 */
public interface Tradeable {

    // ── Abstract method contract (no body) ────────────────────────────────────
    // Any implementing class must supply this implementation.
    double getCurrentPrice();

    // ── Default method (Java 8+) ──────────────────────────────────────────────
    // Provides a default body so existing implementors do not break when
    // new methods are added to the interface.
    default boolean isAffordable(double budget) {
        // Uses the implementor's own getCurrentPrice() via the interface contract
        return budget >= getCurrentPrice();
    }

    // ── Static helper method (Java 8+) ────────────────────────────────────────
    // Does NOT require an instance — called as Tradeable.formatPrice(...)
    static String formatPrice(double price) {
        // String.format demonstrates several String methods from Module I
        return String.format("₹%.2f", price);
    }
}
