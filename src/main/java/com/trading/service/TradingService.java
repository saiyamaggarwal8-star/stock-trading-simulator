package com.trading.service;

import com.trading.model.*;
import com.trading.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * MODULE II – Exception Handling (try/catch/finally), Data Stream Handling,
 *              Lambda expressions (Predicate, Function, Consumer)
 * MODULE IV – Lambda expressions, Collection framework, Spring Boot Service layer
 *
 * Core business logic for placing buy/sell orders.
 * Lambda expressions (Module II/IV) are used for validation and transformation.
 * finally block (Module II) ensures audit logging always runs.
 */
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
        // ── Lambda Predicate (Module II / IV) ──────────────────────────────────
        // Predicate<T> is a functional interface: takes one argument, returns boolean.
        // Lambda syntax: (param) -> expression
        Predicate<Order> hasValidQuantity = o -> o.getQuantity() > 0;
        Predicate<Order> hasBuyType       = o -> o.getOrderType() == OrderType.BUY;
        Predicate<Order> hasSellType      = o -> o.getOrderType() == OrderType.SELL;

        // ── Lambda Function (Module IV) ────────────────────────────────────────
        // Function<T,R>: takes T, returns R. Used to compute total cost from an Order.
        Function<Order, Double> computeTotalCost = o ->
            o.getQuantity() * stockRepository.findById(o.getStock().getId())
                .map(Stock::getCurrentPrice).orElse(0.0);

        // ── Lambda Consumer (Module IV) ────────────────────────────────────────
        // Consumer<T>: takes one argument, returns nothing — used for side effects.
        Consumer<String> auditLogger = msg -> System.out.println("[AUDIT] " + msg);

        // Validate quantity using the Predicate lambda
        if (!hasValidQuantity.test(order)) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        // Retrieve the live Stock object from the database using its ID
        Stock stock = stockRepository.findById(order.getStock().getId())
            .orElseThrow(() -> new IllegalArgumentException("Stock not found"));

        // Freeze current price and compute total cost
        double currentPrice = stock.getCurrentPrice();
        double totalCost    = currentPrice * order.getQuantity();
        User   user         = order.getUser();

        // ── try/finally (Module II — Exception Handling) ──────────────────────
        // The finally block ALWAYS runs — even if an exception is thrown.
        // Used here as an audit trail: log the attempt regardless of outcome.
        try {

        // ── BUY ORDER LOGIC ────────────────────────────────────────────────────
        if (hasBuyType.test(order)) { // Use Predicate lambda instead of == comparison
            // Check if user has enough liquid cash in their simulated wallet
            if (user.getBalance() < totalCost) {
                // If not, instantly reject the order
                throw new IllegalArgumentException("Insufficient funds. Required: ₹" + String.format("%.2f", totalCost) + " Available: ₹" + String.format("%.2f", user.getBalance()));
            }
            // Deduct the cash from user's balance
            user.setBalance(user.getBalance() - totalCost);
            // Update their portfolio to reflect the new stock ownership
            updatePortfolio(user, stock, order.getQuantity(), currentPrice, true);

        // ── SELL ORDER LOGIC ───────────────────────────────────────────────────
        } else if (hasSellType.test(order)) { // Use Predicate lambda
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
            order.getOrderType() == OrderType.BUY  ? savedOrder : null,
            order.getOrderType() == OrderType.SELL ? savedOrder : null,
            stock,
            currentPrice,
            order.getQuantity()
        );
        // Save the Trade history
        tradeRepository.save(trade);

        return savedOrder;

        } finally {
            // ── finally block (Module II) ──────────────────────────────────────
            // This runs whether the order succeeded OR an exception was thrown.
            // Consumer lambda used for audit side-effect logging.
            auditLogger.accept("placeOrder called: "
                + order.getOrderType() + " " + order.getQuantity()
                + " × " + (order.getStock() != null ? order.getStock().getId() : "?")
                + " | User: " + (user != null ? user.getUsername() : "unknown"));
        }
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
