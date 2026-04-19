package com.trading.model;

/**
 * MODULE I  – Abstraction & Objects Lifecycle
 * MODULE II – Abstract Class, Inheritance (single, multilevel, hierarchical), Polymorphism
 *
 * 'Instrument' is the abstract superclass for every tradeable financial product
 * in the system.  It cannot be instantiated directly (abstract class), but defines
 * the shared contract for all child types (Stock, ETF, Bond).
 *
 *  Inheritance hierarchy used in this project
 *  ─────────────────────────────────────────
 *  BaseEntity          ← mapped superclass (JPA)
 *    └── Instrument    ← abstract class (this file) — implements Tradeable
 *          ├── Stock   ← single inheritance
 *          └── ETF     ← multilevel  (Instrument → ETF)
 *
 *  Bond extends Instrument as a hierarchical sibling of Stock/ETF.
 */
public abstract class Instrument {

    // ── Static variable (Module I) ────────────────────────────────────────────
    // Shared across ALL instances of every Instrument subclass.
    // 'static' means it belongs to the class, not to any specific object.
    private static int totalInstrumentsCreated = 0;

    // ── Instance Initialization Block (Module I) ──────────────────────────────
    // Runs before every constructor call.  Demonstrates IIB concept explicitly.
    {
        totalInstrumentsCreated++;  // Increment count every time any Instrument is created
        System.out.println("[IIB] Instrument instance #" + totalInstrumentsCreated + " initialised.");
    }

    // ── Abstract method (Module II) ───────────────────────────────────────────
    // No body here — each subclass MUST provide its own implementation.
    // This enforces the Abstraction principle: define WHAT, not HOW.
    public abstract String getAssetType();

    // ── Concrete method inherited by all subclasses ───────────────────────────
    // Demonstrates runtime polymorphism — the correct version is called based on
    // the actual (runtime) type, not the declared (compile-time) type.
    public String describe() {
        // getAssetType() is resolved at runtime — this is dynamic dispatch / polymorphism
        return "Financial Instrument [Type: " + getAssetType() + "]";
    }

    // ── Static method (Module I) ──────────────────────────────────────────────
    // Can be called without creating an object: Instrument.getTotalCreated()
    public static int getTotalInstrumentsCreated() {
        return totalInstrumentsCreated;
    }
}
