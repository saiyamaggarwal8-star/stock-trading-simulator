package com.trading.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity // Database entity storing time-series data points for building candlestick chart histories
@Table(name = "price_history")
public class PriceHistory extends BaseEntity {

    @Column(nullable = false)
    private String symbol; // The target stock ticker

    @Column(nullable = false)
    private double price; // The exact price snapshot at the exact `createdAt` timestamp (from BaseEntity)

    // Empty constructor required by JPA
    public PriceHistory() {}

    public PriceHistory(String symbol, double price) {
        this.symbol = symbol;
        this.price = price;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
