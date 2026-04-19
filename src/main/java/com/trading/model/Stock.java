package com.trading.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity // Database entity representing a tradeable company stock available in the market
@Table(name = "stocks")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Stock extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String symbol; // The ticker symbol (e.g., AAPL, RELIANCE)

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private double currentPrice; // The live or most recently simulated market price

    @Column(nullable = false)
    private double percentChange; // The % movement of the stock today relative to the 24h previous close

    // Empty constructor required by JPA
    public Stock() {}

    public Stock(String symbol, String companyName, double currentPrice) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.currentPrice = currentPrice;
        this.percentChange = 0.0;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public double getPercentChange() {
        return percentChange;
    }

    public void setPercentChange(double percentChange) {
        this.percentChange = percentChange;
    }
}
