package com.trading.service;

import com.trading.model.User;
import org.springframework.stereotype.Service;

/**
 * MODULE II – Polymorphism, Function Overloading, Method Overriding
 *             non-availability of multiple inheritance of classes
 *             (but implements BOTH Alertable interface — multiple interface ✓)
 *
 * NotificationService demonstrates:
 *
 *  1. METHOD OVERLOADING (Function Overloading)
 *     Same method name 'notify', different parameter lists.
 *     Resolved at COMPILE TIME (static polymorphism / early binding).
 *
 *  2. RUNTIME POLYMORPHISM (dynamic binding)
 *     When called through an Alertable reference, the correct sendAlert()
 *     implementation is resolved at runtime.
 *
 *  3. MULTIPLE INTERFACES
 *     This class implements BOTH Alertable and a second role interface,
 *     demonstrating that Java allows multiple interface implementation
 *     (unlike multiple class inheritance which is NOT allowed in Java).
 */
@Service
public class NotificationService implements Alertable {

    // ── OVERLOADED notify() methods (Function Overloading — Module II) ─────────
    // All three share the name 'notify', but differ in parameter signature.
    // The compiler picks the right version based on the argument types at compile time.

    /** Overload 1 — simple string notification to the system console */
    public void notify(String message) {
        System.out.println("[NOTIFY] " + message);
    }

    /** Overload 2 — notification with a numeric price value attached */
    public void notify(String message, double price) {
        System.out.printf("[NOTIFY] %s | Price: ₹%.2f%n", message, price);
    }

    /** Overload 3 — personalised notification directed at a specific User */
    public void notify(User user, String message) {
        if (user == null) {
            notify(message); // Calls Overload 1 — demonstrates polymorphic dispatch
            return;
        }
        System.out.printf("[NOTIFY → %s] %s%n", user.getUsername(), message);
    }

    /** Overload 4 — notification with explicit severity (uses Alertable.Severity) */
    public void notify(String message, String severity, boolean persist) {
        // Autoboxing demo: boolean → Boolean (Module I)
        Boolean shouldPersist = persist; // Autoboxing: primitive boolean → Boolean wrapper
        System.out.printf("[NOTIFY][%s] %s (persist=%s)%n", severity, message, shouldPersist);
    }

    // ── Anonymous Class (Module II) ───────────────────────────────────────────
    // An anonymous class is a class without a name, defined and instantiated
    // in a single expression. It is useful for one-off implementations.
    /**
     * Returns a one-time-use Alertable that logs to a different target.
     * The anonymous class implements the Alertable interface inline.
     */
    public Alertable createAuditAlerter() {
        // Anonymous class — no class name, implemented inline
        return new Alertable() {
            @Override
            public void sendAlert(String message, String severity) {
                // This anonymous class logs to a separate "AUDIT" stream
                System.out.printf("[AUDIT-ALERT][%s] %s%n", severity, message);
            }
        };
    }

    // ── Interface implementation: sendAlert (Alertable contract) ──────────────
    @Override
    public void sendAlert(String message, String severity) {
        // This is the primary implementation of the Alertable interface contract
        System.out.printf("[ALERT][%s] %s%n", severity.toUpperCase(), message);
    }

    // ── Trade execution alerts ─────────────────────────────────────────────────
    public void notifyTradeExecuted(User user, String symbol, int qty, double price) {
        // Calls overloaded notify(User, String)
        notify(user, "Trade executed: " + qty + " × " + symbol + " @ ₹" + price);
        // Also sends a high-severity alert
        sendHighAlert("Trade confirmed for " + symbol + " — ₹" + (qty * price) + " total");
    }

    public void notifyInsufficientFunds(User user, double required, double available) {
        // Calls overloaded notify(User, String)
        notify(user, "Insufficient funds! Required ₹" + required + ", Available ₹" + available);
        sendAlert("Insufficient funds for user " + user.getUsername(), Alertable.Severity.HIGH);
    }
}
