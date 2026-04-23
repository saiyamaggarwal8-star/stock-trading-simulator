package com.trading.service;

/**
 * MODULE II – Interfaces, Embedded Interface (nested interface concept),
 *             non-availability of multiple class inheritance (but multiple interfaces ✓)
 *
 * 'Alertable' is a second interface that any service can implement alongside 'Tradeable'.
 * Java does NOT allow a class to extend multiple classes (no multiple inheritance of classes),
 * but it CAN implement multiple interfaces — demonstrated by NotificationService implementing
 * both Alertable and any future interface simultaneously.
 *
 * The nested 'Severity' interface is an example of an EMBEDDED INTERFACE (also called
 * a nested or member interface).
 */
public interface Alertable {

    // ── Embedded / Nested Interface (Module II) ───────────────────────────────
    // An interface declared inside another interface.
    // Referenced as: Alertable.Severity
    interface Severity {
        String LOW    = "LOW";
        String MEDIUM = "MEDIUM";
        String HIGH   = "HIGH";
    }

    // ── Abstract method contracts ─────────────────────────────────────────────
    void sendAlert(String message, String severity);

    // ── Default method (optional override) ───────────────────────────────────
    default void sendLowAlert(String message) {
        sendAlert(message, Severity.LOW);
    }

    default void sendHighAlert(String message) {
        sendAlert(message, Severity.HIGH);
    }
}
