package com.trading.service;

import com.trading.model.*;
import com.trading.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TradingService {
    
    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;
    private final TradeRepository tradeRepository;
    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;

    public TradingService(OrderRepository orderRepository, StockRepository stockRepository, 
                          TradeRepository tradeRepository, PortfolioRepository portfolioRepository, 
                          UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.stockRepository = stockRepository;
        this.tradeRepository = tradeRepository;
        this.portfolioRepository = portfolioRepository;
        this.userRepository = userRepository;
    }

    @Transactional // Ensures the entire method completes successfully; if any error occurs, all database changes are rolled back
    public Order placeOrder(Order order) {
        // Retrieve the live Stock object from the database using its ID
        Stock stock = stockRepository.findById(order.getStock().getId())
            .orElseThrow(() -> new IllegalArgumentException("Stock not found"));
            
        // Validate that order quantity makes sense
        if (order.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        // Freeze the current market price of the stock for this transaction
        double currentPrice = stock.getCurrentPrice();
        // Calculate the total value of this transaction
        double totalCost = currentPrice * order.getQuantity();
        User user = order.getUser();

        // ---------------- BUY ORDER LOGIC ----------------
        if (order.getOrderType() == OrderType.BUY) {
            // Check if user has enough liquid cash in their simulated wallet
            if (user.getBalance() < totalCost) {
                // If not, instantly reject the order
                throw new IllegalArgumentException("Insufficient funds. Required: ₹" + String.format("%.2f", totalCost) + " Available: ₹" + String.format("%.2f", user.getBalance()));
            }
            // Deduct the cash from user's balance
            user.setBalance(user.getBalance() - totalCost);
            // Update their portfolio to reflect the new stock ownership
            updatePortfolio(user, stock, order.getQuantity(), currentPrice, true);
            
        // ---------------- SELL ORDER LOGIC ----------------
        } else if (order.getOrderType() == OrderType.SELL) {
            // Find all stocks owned by this user
            List<Portfolio> portfolios = portfolioRepository.findByUser(user);
            // Look for the specific stock they are trying to sell
            Portfolio portfolio = portfolios.stream()
                    .filter(p -> p.getStock().getId().equals(stock.getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("You do not own any shares of " + stock.getSymbol()));
                    
            // Ensure they are not trying to sell more shares than they actually own
            if (portfolio.getQuantity() < order.getQuantity()) {
                throw new IllegalArgumentException("Insufficient shares. You only own " + portfolio.getQuantity() + " shares of " + stock.getSymbol());
            }
            
            // Add the proceeds from the sale back into the user's cash balance
            user.setBalance(user.getBalance() + totalCost);
            // Remove the sold stocks from their portfolio
            updatePortfolio(user, stock, order.getQuantity(), currentPrice, false);
        }

        // Persist the updated user balance to the database
        userRepository.save(user);

        // Populate the final details of the executed Order record
        order.setStock(stock); // Ensure full stock object is associated instead of just ID
        order.setPrice(currentPrice); // Lock in the execution price
        order.setStatus(OrderStatus.FILLED); // Mark order as fulfilled immediately (market order)
        order.setFilledQuantity(order.getQuantity());
        // Save the order to DB
        Order savedOrder = orderRepository.save(order);
        
        // Generate a Trade record for history tracking
        Trade trade = new Trade(
            order.getOrderType() == OrderType.BUY ? savedOrder : null, // Set buy order info if it was a buy
            order.getOrderType() == OrderType.SELL ? savedOrder : null, // Set sell order info if it was a sell
            stock,
            currentPrice,
            order.getQuantity()
        );
        // Save the Trade history
        tradeRepository.save(trade);
        
        // Console log for server-side monitoring
        System.out.println("Trade Executed: " + order.getOrderType() + " " + order.getQuantity() + " " + stock.getSymbol() + " at ₹" + currentPrice);
        
        return savedOrder;
    }

    // Helper method to keep user's portfolio accurately synced after trades
    private void updatePortfolio(User user, Stock stock, int quantity, double price, boolean isBuy) {
        // Fetch existing portfolios
        List<Portfolio> portfolios = portfolioRepository.findByUser(user);
        
        // Find existing record for this stock, or create a brand new empty one if first time buying
        Portfolio portfolio = portfolios.stream()
                .filter(p -> p.getStock().getId().equals(stock.getId()))
                .findFirst()
                .orElse(new Portfolio(user, stock, 0, 0));
        
        if (isBuy) {
            // Calculate new average buy price (Cost Basis) using weighted average formula
            double totalCost = (portfolio.getQuantity() * portfolio.getAveragePrice()) + (quantity * price);
            // Add shares to existing total
            portfolio.setQuantity(portfolio.getQuantity() + quantity);
            // Set the new averaged unit price
            portfolio.setAveragePrice(totalCost / portfolio.getQuantity());
        } else {
            // If selling, simply reduce the quantity held (ensuring it doesn't drop below 0)
            portfolio.setQuantity(Math.max(0, portfolio.getQuantity() - quantity));
            // If all shares were sold, reset the average price anchor to 0
            if (portfolio.getQuantity() == 0) {
                portfolio.setAveragePrice(0); // Reset average if completely sold
            }
        }
        // Save portfolio changes to DB
        portfolioRepository.save(portfolio);
    }
}
