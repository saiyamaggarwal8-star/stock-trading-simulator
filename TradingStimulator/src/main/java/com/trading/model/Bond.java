package com.trading.model;

/**
 * MODULE I  – Inheritance (hierarchical), Encapsulation, Security aspects in class design
 * MODULE II – Method Overriding, Concrete Class, Abstraction
 *
 * Bond is a HIERARCHICAL sibling of Stock and ETF — all three extend Instrument directly.
 *
 * Hierarchical Inheritance:
 *              Instrument   (abstract superclass)
 *             /     |     \
 *          Stock   ETF    Bond        ← all are direct subclasses
 *
 * This means Instrument is the common ancestor, and Stock/ETF/Bond do NOT
 * inherit from each other — they share only what Instrument provides.
 *
 * Security Aspects (Module I): sensitive fields like couponRate or maturityDate
 * must not be publicly writable without validation — enforced by setters.
 */
public class Bond extends Instrument {

    // ── Private fields (Encapsulation / Security design) ─────────────────────
    private String issuer;            // e.g., "Government of India", "HDFC Ltd"
    private double faceValue;         // Par value of the bond (e.g., ₹1000)
    private double couponRate;        // Annual interest rate in % (e.g., 7.5)
    private int    maturityYears;     // Years until the bond matures

    // ── Static constant (Module I) ────────────────────────────────────────────
    // Shared across all Bond instances — maximum allowed coupon rate (regulatory cap)
    private static final double MAX_COUPON_RATE = 15.0;

    // ── No-arg constructor ────────────────────────────────────────────────────
    public Bond() {}

    // ── Parameterised constructor ─────────────────────────────────────────────
    public Bond(String issuer, double faceValue, double couponRate, int maturityYears) {
        this.issuer       = issuer;
        this.faceValue    = faceValue;
        // Use the secure setter for validation
        setCouponRate(couponRate);
        this.maturityYears = maturityYears;
    }

    // ── Method OVERRIDING (Module II) ─────────────────────────────────────────
    // Implements the abstract method from Instrument — the Bond's asset type label.
    @Override
    public String getAssetType() {
        return "BOND";
    }

    @Override
    public String describe() {
        return super.describe()
            + " | Issuer: " + issuer
            + " | Coupon: " + couponRate + "%"
            + " | Matures in: " + maturityYears + " yrs";
    }

    // ── Secure setter with validation (Security aspects in class design) ───────
    // The coupon rate must be between 0 and MAX_COUPON_RATE — never set raw
    public void setCouponRate(double couponRate) {
        if (couponRate < 0 || couponRate > MAX_COUPON_RATE) {
            throw new IllegalArgumentException(
                "Coupon rate must be between 0 and " + MAX_COUPON_RATE + "%");
        }
        this.couponRate = couponRate;
    }

    // ── Getters (controlled access — Encapsulation) ──────────────────────────
    public String getIssuer()          { return issuer; }
    public void   setIssuer(String i)  { this.issuer = i; }
    public double getFaceValue()       { return faceValue; }
    public void   setFaceValue(double v) { this.faceValue = v; }
    public double getCouponRate()      { return couponRate; }
    public int    getMaturityYears()   { return maturityYears; }
    public void   setMaturityYears(int y) { this.maturityYears = y; }
}
