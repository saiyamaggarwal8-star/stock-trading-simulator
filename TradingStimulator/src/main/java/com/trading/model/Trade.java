package com.trading.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity // Database entity representing a completed historical execution between a buyer and/or seller
@Table(name = "trades") // Maps to the "trades" history ledger table
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Trade extends BaseEntity {

    // The order that initiated the buy side of this trade (nullable if market maker sold it)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buy_order_id", nullable = true)
    private Order buyOrder;

    // The order that initiated the sell side of this trade (nullable if market maker bought it)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sell_order_id", nullable = true)
    private Order sellOrder;

    // The asset that was exchanged
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private double price; // The agreed execution price at the moment the trade cleared

    @Column(nullable = false)
    private int quantity; // Volume of the transaction

    // Empty constructor required by JPA
    public Trade() {}

    public Trade(Order buyOrder, Order sellOrder, Stock stock, double price, int quantity) {
        this.buyOrder = buyOrder;
        this.sellOrder = sellOrder;
        this.stock = stock;
        this.price = price;
        this.quantity = quantity;
    }

    public Order getBuyOrder() {
        return buyOrder;
    }

    public void setBuyOrder(Order buyOrder) {
        this.buyOrder = buyOrder;
    }

    public Order getSellOrder() {
        return sellOrder;
    }

    public void setSellOrder(Order sellOrder) {
        this.sellOrder = sellOrder;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
