package com.trading.model;

/**
 * MODULE I  – Inheritance (single), Encapsulation, Objects Lifecycle
 * MODULE II – Method Overriding, Concrete Class / Abstraction
 * MODULE IV – JPA (Java Persistence API)
 *
 * ETF (Exchange-Traded Fund) extends Instrument demonstrating MULTILEVEL inheritance:
 *   BaseEntity → Instrument → ETF
 *
 * It also demonstrates:
 *  - Method OVERRIDING (@Override) of getAssetType() from Instrument (Module II)
 *  - Concrete Class that provides body for the abstract method (Module II)
 *  - Encapsulation: all fields private, exposed only via getters/setters (Module I)
 */
public class ETF extends Instrument implements com.trading.service.Tradeable {

    // ── Encapsulation (Module I) ──────────────────────────────────────────────
    // All instance fields are private — external code must use getters/setters.
    private String symbol;
    private String name;
    private double currentPrice;
    private String underlyingIndex; // e.g., "NIFTY 50", "SENSEX"
    private double expenseRatio;    // Annual management fee in %

    // ── Instance Initialization Block (Module I) ──────────────────────────────
    // Executes before every constructor — shared setup code without duplication.
    {
        this.expenseRatio = 0.05; // Default expense ratio of 0.05% for all ETFs
    }

    // ── No-arg constructor ────────────────────────────────────────────────────
    public ETF() {}

    // ── Parameterised constructor ─────────────────────────────────────────────
    public ETF(String symbol, String name, double currentPrice, String underlyingIndex) {
        this.symbol = symbol;
        this.name = name;
        this.currentPrice = currentPrice;
        this.underlyingIndex = underlyingIndex;
        // expenseRatio already initialised by IIB above
    }

    // ── Method OVERRIDING (Module II) ─────────────────────────────────────────
    // @Override proves this IS overriding the abstract method from Instrument.
    // Polymorphism: calling getAssetType() on an Instrument reference that holds
    // an ETF object will execute THIS body at runtime (dynamic dispatch).
    @Override
    public String getAssetType() {
        return "ETF";
    }

    // ── Also overrides describe() for more specific output ────────────────────
    @Override
    public String describe() {
        // super.describe() calls Instrument's version first, then appends ETF-specific info
        return super.describe() + " | Index: " + underlyingIndex + " | Expense Ratio: " + expenseRatio + "%";
    }

    // ── Interface contract (Tradeable) ────────────────────────────────────────
    @Override
    public double getCurrentPrice() {
        return currentPrice;
    }

    // ── Getters & Setters (Encapsulation) ─────────────────────────────────────
    public String getSymbol()                     { return symbol; }
    public void   setSymbol(String symbol)        { this.symbol = symbol; }
    public String getName()                       { return name; }
    public void   setName(String name)            { this.name = name; }
    public void   setCurrentPrice(double p)       { this.currentPrice = p; }
    public String getUnderlyingIndex()            { return underlyingIndex; }
    public void   setUnderlyingIndex(String idx)  { this.underlyingIndex = idx; }
    public double getExpenseRatio()               { return expenseRatio; }
    public void   setExpenseRatio(double r)       { this.expenseRatio = r; }
}
