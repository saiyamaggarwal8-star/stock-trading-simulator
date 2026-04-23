package com.trading.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity // Specifies that this class is mapped to a database table
@Table(name = "portfolios") // Maps this entity to the "portfolios" table in the DB
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // Prevents JSON serialization errors when returning lazy-loaded relationships
public class Portfolio extends BaseEntity {

    // A many-to-one relationship: Many portfolio entries belong to one User. Lazy fetching saves memory.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Foreign key pointing to the user owning these stocks
    private User user;

    // Many portfolio entries point to the same global Stock defined in the system
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false) // Foreign key to the stock table
    private Stock stock;

    @Column(nullable = false)
    private int quantity; // The current number of shares owned

    @Column(nullable = false)
    private double averagePrice; // The Volume Weighted Average Price (Cost Basis) for the holdings

    // Empty constructor required by JPA
    public Portfolio() {}

    public Portfolio(User user, Stock stock, int quantity, double averagePrice) {
        this.user = user;
        this.stock = stock;
        this.quantity = quantity;
        this.averagePrice = averagePrice;
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(double averagePrice) {
        this.averagePrice = averagePrice;
    }
}
