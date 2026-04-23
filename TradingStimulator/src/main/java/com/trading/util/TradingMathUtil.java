package com.trading.util;

/**
 * MODULE I – Operators (unary, arithmetic, logical, shift, ternary, assignment),
 *            Bitwise operators, Compound assignment,
 *            Array declaration & instantiation, Multidimensional arrays,
 *            Static variables, Static methods, Static blocks,
 *            Wrapper classes (Integer, Double, Boolean, Character), Autoboxing, Unboxing
 *
 * Central mathematical utility for price calculations in the trading simulator.
 * All methods and fields are static — no instance creation required.
 */
public class TradingMathUtil {

    // ── Static Constants (Module I — Static variables) ────────────────────────
    // 'static final' = class-level constant, shared across all usages.
    public static final double DEFAULT_TAX_RATE_PERCENT = 0.1;    // 0.1% STT on equity delivery
    public static final int    MAX_ORDER_FLAGS          = 8;       // Supports 8 binary flags in a bitmask
    public static final double ROUND_SCALE              = 100.0;   // For rounding to 2 decimal places

    // ── Static variable (mutable shared state) ────────────────────────────────
    private static long totalCalculations = 0L;

    // ── Static Block (Module I) ───────────────────────────────────────────────
    // Runs ONCE when the class is first loaded into the JVM.
    // Used for expensive one-time initialisation.
    static {
        System.out.println("[TradingMathUtil] Static initializer: utility class loaded.");
        System.out.println("[TradingMathUtil] Tax rate: " + DEFAULT_TAX_RATE_PERCENT + "% | Max flags: " + MAX_ORDER_FLAGS);
    }

    // ── Arithmetic & Assignment Operators (Module I) ──────────────────────────
    /**
     * Calculates total cost including brokerage and STT taxes.
     * Demonstrates: *, +, -, /, compound assignment (+=, *=)
     */
    public static double calculateTotalCost(double pricePerShare, int quantity, double brokeragePercent) {
        totalCalculations++; // Unary increment operator (++)

        double baseCost = pricePerShare * quantity;     // * arithmetic
        double brokerage = baseCost * (brokeragePercent / 100.0); // compound: * and /
        double stt = baseCost * (DEFAULT_TAX_RATE_PERCENT / 100.0);

        double total = baseCost;
        total += brokerage;   // Compound assignment operator +=
        total += stt;         // Compound assignment operator +=

        return Math.round(total * ROUND_SCALE) / ROUND_SCALE; // Round to 2 decimal places
    }

    // ── Bitwise Operators and Shift Operators (Module I) ──────────────────────
    /**
     * Encodes multiple boolean order flags into a single integer bitmask.
     *
     * Bit positions:
     *   Bit 0 (LSB) = isMarketOrder
     *   Bit 1       = isUrgent
     *   Bit 2       = isStopLoss
     *   Bit 3       = isAfterHours
     *
     * Bitwise operators used:
     *   |  = bitwise OR  (set a bit)
     *   &  = bitwise AND (check a bit)
     *   << = left shift  (move bit to position)
     *   >> = right shift (move bit back)
     *   ~  = bitwise NOT (complement)
     */
    public static int encodeOrderFlags(boolean isMarketOrder, boolean isUrgent,
                                        boolean isStopLoss, boolean isAfterHours) {
        int flags = 0;                             // All bits start at 0

        if (isMarketOrder)  flags |= 1;            // Set bit 0:  flags = flags | 0001
        if (isUrgent)       flags |= (1 << 1);     // Set bit 1:  flags = flags | 0010 (left shift)
        if (isStopLoss)     flags |= (1 << 2);     // Set bit 2:  flags = flags | 0100
        if (isAfterHours)   flags |= (1 << 3);     // Set bit 3:  flags = flags | 1000

        return flags;
    }

    /** Checks if a specific flag bit is set using bitwise AND */
    public static boolean isFlagSet(int flags, int bitPosition) {
        // Right-shift flags by bitPosition, then AND with 1 to isolate that bit
        return ((flags >> bitPosition) & 1) == 1;
    }

    /** Clears a specific flag bit using bitwise AND with NOT (~) */
    public static int clearFlag(int flags, int bitPosition) {
        return flags & ~(1 << bitPosition); // ~ is bitwise NOT
    }

    // ── Wrapper Classes & Autoboxing / Unboxing (Module I) ────────────────────
    /**
     * Demonstrates Integer, Double, Boolean, Character wrapper classes.
     * Autoboxing  = automatic conversion from primitive to wrapper (int  → Integer).
     * Unboxing    = automatic conversion from wrapper to primitive (Integer → int).
     */
    public static void demonstrateWrapperClasses() {

        // ── Integer wrapper ───────────────────────────────────────────────────
        int primitiveQty      = 100;
        Integer wrappedQty    = primitiveQty;          // Autoboxing: int → Integer
        int    unboxedQty     = wrappedQty;            // Unboxing: Integer → int
        System.out.println("Integer.MAX_VALUE = " + Integer.MAX_VALUE);
        System.out.println("Parsed qty: " + Integer.parseInt("200"));   // String → int
        System.out.println("Binary of " + primitiveQty + ": " + Integer.toBinaryString(primitiveQty));

        // ── Double wrapper ────────────────────────────────────────────────────
        double  primitivePrice = 1392.50;
        Double  wrappedPrice   = primitivePrice;       // Autoboxing: double → Double
        double  unboxedPrice   = wrappedPrice;         // Unboxing: Double → double
        System.out.println("Parsed price: " + Double.parseDouble("1392.50"));
        System.out.println("Is NaN: " + Double.isNaN(wrappedPrice));

        // ── Boolean wrapper ───────────────────────────────────────────────────
        boolean primitiveFlag  = true;
        Boolean wrappedFlag    = primitiveFlag;        // Autoboxing
        boolean unboxedFlag    = wrappedFlag;          // Unboxing
        System.out.println("Parsed: " + Boolean.parseBoolean("true"));

        // ── Character wrapper ─────────────────────────────────────────────────
        char    primitiveChar  = 'B';                  // 'B' for BUY
        Character wrappedChar  = primitiveChar;        // Autoboxing
        char    unboxedChar    = wrappedChar;          // Unboxing
        System.out.println("Is uppercase: " + Character.isUpperCase(wrappedChar));
        System.out.println("As digit?: "   + Character.isDigit(unboxedChar));

        // Use variables to suppress unused-variable warnings
        System.out.println(unboxedQty + " " + unboxedPrice + " " + unboxedFlag + " " + unboxedChar);
    }

    // ── Arrays: 1D and 2D (Multidimensional) (Module I) ──────────────────────
    /**
     * Demonstrates array declaration, instantiation, and 2D arrays.
     * A 2D price matrix stores closing prices for N stocks over M days.
     *
     * Row    = stock index (e.g., 0=RELIANCE, 1=TCS, ...)
     * Column = day index (e.g., 0=Day1, 1=Day2, ...)
     */
    public static double[][] buildPriceMatrix(String[] symbols, int days) {
        int stocks = symbols.length; // 1D array .length property

        // ── Multidimensional array declaration and instantiation ──────────────
        double[][] priceMatrix = new double[stocks][days]; // [rows][columns]

        // ── Nested for loops to fill the 2D array ────────────────────────────
        for (int row = 0; row < stocks; row++) {      // Outer: iterate rows (stocks)
            for (int col = 0; col < days; col++) {    // Inner: iterate columns (days)
                // Simulate a random starting price seeded by symbol for determinism
                priceMatrix[row][col] = 100.0 + (symbols[row].hashCode() % 500) + (col * 0.5);
            }
        }

        return priceMatrix; // Return the fully populated 2D array
    }

    /** Calculates the average price for a given stock row in the 2D matrix */
    public static double averageForStock(double[][] matrix, int stockIndex) {
        double sum  = 0;
        int    days = matrix[stockIndex].length; // Array .length property

        // ── for loop (Module I) ──────────────────────────────────────────────
        for (double price : matrix[stockIndex]) { // Enhanced for-each loop
            sum += price; // Compound assignment
        }

        // ── Arithmetic + ternary operator (Module I) ─────────────────────────
        return days > 0 ? (Math.round((sum / days) * ROUND_SCALE) / ROUND_SCALE) : 0.0;
    }

    // ── Logical operators (Module I) ──────────────────────────────────────────
    /**
     * Uses &&, ||, ! to validate an order before submission.
     */
    public static boolean isOrderValid(double price, int qty, double balance) {
        boolean priceOk   = price > 0;                     // Logical: simple comparison
        boolean qtyOk     = qty > 0 && qty <= 100_000;     // && (AND): both must be true
        boolean fundOk    = balance >= (price * qty);       // Sufficient funds
        boolean notEmpty  = !( price == 0 || qty == 0 );   // ||  (OR), ! (NOT)

        return priceOk && qtyOk && fundOk && notEmpty;     // All conditions must pass
    }

    // ── Switch expression (Module I — switch for menu-driven) ─────────────────
    /**
     * Returns the transaction tax rate based on order type.
     * Switch is the preferred construct for menu/option driven branching.
     */
    public static double getTaxRateForOrderType(String orderType) {
        String type = (orderType == null) ? "UNKNOWN" : orderType.toUpperCase(); // Ternary

        // Switch expression (Java 14+) — evaluated, returns a value
        return switch (type) {
            case "BUY"   -> 0.001;  // 0.1% STT on delivery buy
            case "SELL"  -> 0.001;  // 0.1% STT on delivery sell
            case "INTRADAY_BUY"  -> 0.00025; // 0.025% intraday
            case "INTRADAY_SELL" -> 0.00025;
            case "FUTURES" -> 0.00002; // 0.002% on F&O
            default      -> DEFAULT_TAX_RATE_PERCENT / 100.0; // Fallback default
        };
    }

    /** Returns total computation count — demonstrates static variable access */
    public static long getTotalCalculations() {
        return totalCalculations;
    }
}
