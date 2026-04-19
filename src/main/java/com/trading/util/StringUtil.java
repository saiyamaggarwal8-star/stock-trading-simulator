package com.trading.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.Predicate;

/**
 * MODULE I  – String methods (concat, indexOf, split, length, toLowerCase,
 *             toUpperCase, replace, trim)
 * MODULE IV – Regular Expressions (Pattern, Matcher), Lambda expressions
 *             (Predicate as a functional interface)
 *
 * Central utility for string manipulation across the trading application.
 * All methods are static — no instance required.
 */
public class StringUtil {

    // ── Compiled regex patterns (Module IV) ──────────────────────────────────
    // Compiled once at class-load time for efficiency.
    // Pattern represents a compiled regular expression.
    private static final Pattern SYMBOL_PATTERN      = Pattern.compile("^[A-Z]{1,20}$");
    private static final Pattern USERNAME_PATTERN    = Pattern.compile("^[a-zA-Z0-9_]{3,30}$");
    private static final Pattern NUMERIC_PATTERN     = Pattern.compile("-?\\d+(\\.\\d+)?");

    // ── Lambda Predicate (Module IV) ──────────────────────────────────────────
    // Predicate<T> is a functional interface — it takes one argument and returns boolean.
    // Lambda syntax: (argument) -> expression
    private static final Predicate<String> IS_VALID_SYMBOL
        = symbol -> symbol != null && SYMBOL_PATTERN.matcher(symbol).matches();

    private static final Predicate<String> IS_VALID_USERNAME
        = username -> username != null && USERNAME_PATTERN.matcher(username).matches();

    // ── String.concat (Module I) ──────────────────────────────────────────────
    // Concatenates two strings explicitly using the String.concat() method.
    public static String buildOrderLabel(String type, String symbol) {
        // concat() is the explicit method form of the + operator
        return type.concat(" ").concat(symbol);
    }

    // ── indexOf, length, split (Module I) ────────────────────────────────────
    /**
     * Parses a raw order string "BUY:RELIANCE:100" into [type, symbol, qty].
     */
    public static String[] parseOrderString(String raw) {
        // trim removes leading and trailing whitespace
        String cleaned = raw.trim();

        // indexOf returns the position of ':' or -1 if not found
        if (cleaned.indexOf(':') == -1) {
            throw new IllegalArgumentException("Invalid order format. Expected: TYPE:SYMBOL:QTY");
        }

        // split breaks the string at ':' and returns an array — Module I
        String[] parts = cleaned.split(":");

        // length gives the size of the array
        if (parts.length != 3) {
            throw new IllegalArgumentException("Expected 3 parts, got: " + parts.length);
        }

        return parts; // e.g., ["BUY", "RELIANCE", "100"]
    }

    // ── toLowerCase, toUpperCase, replace, trim (Module I) ────────────────────
    /**
     * Normalises a stock symbol: trims whitespace, converts to uppercase,
     * and strips non-alphabetic characters.
     */
    public static String normaliseSymbol(String raw) {
        if (raw == null) return "";
        // trim()       → remove leading/trailing spaces
        // toUpperCase()→ convert to uppercase (RELIANCE, not reliance)
        // replace()    → remove any non-word characters (defensive clean)
        return raw.trim().toUpperCase().replace(" ", "").replace("-", "");
    }

    /**
     * Normalises a username: trims and converts to lowercase.
     */
    public static String normaliseUsername(String raw) {
        if (raw == null) return "";
        return raw.trim().toLowerCase(); // toLowerCase — Module I
    }

    // ── Regex validation with Pattern & Matcher (Module IV) ───────────────────
    /**
     * Validates a stock symbol using compiled regular expression.
     * Pattern.matcher(input) creates a Matcher that applies the regex to input.
     * Matcher.matches() returns true if the ENTIRE input matches the pattern.
     */
    public static boolean isValidSymbol(String symbol) {
        // Using the lambda Predicate defined above (Module IV)
        return IS_VALID_SYMBOL.test(symbol);
    }

    public static boolean isValidUsername(String username) {
        return IS_VALID_USERNAME.test(username); // Lambda Predicate.test() call
    }

    /**
     * Demonstrates explicit Matcher usage for partial regex matching.
     * find() matches any subsequence (unlike matches() which needs full match).
     */
    public static boolean containsNumber(String input) {
        Matcher matcher = NUMERIC_PATTERN.matcher(input);
        return matcher.find(); // Returns true if any part of input matches the pattern
    }

    // ── String formatting and composition ─────────────────────────────────────
    /**
     * Builds a formatted trade confirmation message.
     * Demonstrates String.format(), concat, and length.
     */
    public static String formatTradeConfirmation(String type, String symbol, int qty, double price) {
        String base = "TRADE CONFIRMED: "
            .concat(type.toUpperCase())
            .concat(" ")
            .concat(symbol.toUpperCase());

        // String.format for decimal formatting (Module I String methods)
        String details = String.format(" | Qty: %d | Price: ₹%.2f | Total: ₹%.2f",
            qty, price, qty * price);

        // length() returns the number of characters in the string
        String separator = "─".repeat(base.concat(details).length());

        return separator + "\n" + base + details + "\n" + separator;
    }
}
