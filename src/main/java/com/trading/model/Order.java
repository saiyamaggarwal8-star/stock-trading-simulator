package com.trading.model;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity // Represents a financial transaction order in the system
@Table(name = "orders") // Mapped to the "orders" table
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Order extends BaseEntity implements Comparable<Order> {

    // Maps which user placed the order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Maps which stock the order is trading
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    // Enum representing whether this is a BUY or SELL order
    @Enumerated(EnumType.STRING) // Saves the enum word exactly ("BUY"/"SELL") instead of numerical index
    @Column(nullable = false)
    private OrderType orderType;

    @Column(nullable = false)
    private double price; // The price at which the order was locked in/executed

    @Column(nullable = false)
    private int quantity; // The number of shares requested

    @Column(nullable = false)
    private int filledQuantity; // The number of shares actually processed (useful for partial fills later)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status; // Enum tracking if the order is PENDING, FILLED, or CANCELLED

    // Empty constructor required by JPA
    public Order() {}

    public Order(User user, Stock stock, OrderType orderType, double price, int quantity) {
        this.user = user;
        this.stock = stock;
        this.orderType = orderType;
        this.price = price;
        this.quantity = quantity;
        this.filledQuantity = 0;
        this.status = OrderStatus.PENDING;
    }

    // Comparable for PriorityQueue logic (Price then Time)
    @Override
    public int compareTo(Order other) {
        if (this.orderType == OrderType.BUY) {
            // Higher price first for BUY
            int priceCompare = Double.compare(other.price, this.price);
            if (priceCompare != 0) return priceCompare;
        } else {
            // Lower price first for SELL
            int priceCompare = Double.compare(this.price, other.price);
            if (priceCompare != 0) return priceCompare;
        }
        // FIFO: Earlier creation time first
        return this.getCreatedAt().compareTo(other.getCreatedAt());
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
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

    public int getFilledQuantity() {
        return filledQuantity;
    }

    public void setFilledQuantity(int filledQuantity) {
        this.filledQuantity = filledQuantity;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
