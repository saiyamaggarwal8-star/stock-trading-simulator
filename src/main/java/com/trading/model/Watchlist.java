package com.trading.model;

import java.util.ArrayList;
import java.util.List;

/**
 * MODULE I  – Encapsulation, Security aspects in class design, Constructors
 * MODULE II – ArrayList Collection
 *
 * A Watchlist is a named list of stock symbols a user wants to track.
 * It is a standalone (non-JPA) domain object that illustrates strict
 * encapsulation: the internal list is NEVER exposed directly.
 *
 * Security aspects covered:
 *  - No public reference to the internal mutable list is returned (defensive copy).
 *  - Validation in every setter prevents invalid state.
 *  - Constructor initialises the object into a valid, consistent state.
 */
public class Watchlist {

    // ── Private mutable state — never accessible directly (Encapsulation) ────
    private final String ownerUsername;   // Immutable owner (final)
    private String name;                  // Watchlist display name
    private final List<String> symbols;   // ArrayList of ticker symbols — Module II

    // ── Constructor (Module I) ────────────────────────────────────────────────
    // Parameters validated inside the constructor to ensure the object starts valid.
    public Watchlist(String ownerUsername, String name) {
        if (ownerUsername == null || ownerUsername.isBlank()) {
            throw new IllegalArgumentException("Owner username cannot be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Watchlist name cannot be blank");
        }
        this.ownerUsername = ownerUsername;
        this.name           = name;
        this.symbols        = new ArrayList<>();  // ArrayList<String> — Module II collection
    }

    // ── Encapsulated mutation — add with validation ───────────────────────────
    public void addSymbol(String symbol) {
        if (symbol == null || symbol.isBlank()) return;
        String upper = symbol.trim().toUpperCase(); // trim, toUpperCase — Module I Strings
        if (!symbols.contains(upper)) {
            symbols.add(upper);
        }
    }

    public boolean removeSymbol(String symbol) {
        return symbols.remove(symbol != null ? symbol.trim().toUpperCase() : "");
    }

    // ── Defensive getter — returns a COPY, not the real internal list ─────────
    // This is a key security / encapsulation technique: callers cannot mutate
    // the internal state by modifying the returned list.
    public List<String> getSymbols() {
        return new ArrayList<>(symbols); // Defensive copy
    }

    public int size() { return symbols.size(); }

    // ── Standard getters / setters ────────────────────────────────────────────
    public String getOwnerUsername()  { return ownerUsername; }
    public String getName()           { return name; }
    public void   setName(String n)   {
        if (n == null || n.isBlank()) throw new IllegalArgumentException("Name cannot be blank");
        this.name = n;
    }

    @Override
    public String toString() {
        return "Watchlist{owner='" + ownerUsername + "', name='" + name
            + "', symbols=" + symbols + "}";
    }
}
