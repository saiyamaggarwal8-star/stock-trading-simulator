package com.trading.service;

import com.trading.model.Stock;
import com.trading.model.User;
import com.trading.repository.PriceHistoryRepository;
import com.trading.repository.StockRepository;
import com.trading.repository.UserRepository;
import com.trading.util.PasswordUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class DataInitializer {

    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    // Updated Indian NSE stocks with more accurate 2024/2025 base prices
    private static final Object[][] INDIAN_STOCKS = {
        {"RELIANCE",   "Reliance Industries",         1392.00},
        {"TCS",        "Tata Consultancy Services",   3810.00},
        {"HDFCBANK",   "HDFC Bank Ltd",               1636.00},
        {"INFY",       "Infosys Ltd",                  1520.00},
        {"ICICIBANK",  "ICICI Bank Ltd",               1080.00},
        {"WIPRO",      "Wipro Ltd",                     458.00},
        {"SBIN",       "State Bank of India",           772.00},
        {"BAJFINANCE", "Bajaj Finance Ltd",            6880.00},
    };

    public DataInitializer(StockRepository stockRepository, 
                           UserRepository userRepository,
                           PriceHistoryRepository priceHistoryRepository) {
        this.stockRepository = stockRepository;
        this.userRepository = userRepository;
        this.priceHistoryRepository = priceHistoryRepository;
    }

    @PostConstruct
    public void seed() {
        // Clear old price history to reset charts and percent changes
        priceHistoryRepository.deleteAllInBatch();

        // Update or insert each Indian stock
        for (Object[] data : INDIAN_STOCKS) {
            String symbol = (String) data[0];
            String company = (String) data[1];
            double price = (Double) data[2];

            Optional<Stock> existing = stockRepository.findBySymbol(symbol);
            if (existing.isPresent()) {
                Stock s = existing.get();
                s.setCompanyName(company);
                // Force reset to the accurate seed price
                s.setCurrentPrice(price);
                s.setPercentChange(0.0);
                stockRepository.save(s);
            } else {
                Stock n = new Stock(symbol, company, price);
                n.setPercentChange(0.0);
                stockRepository.save(n);
            }
        }

        // Handle legacy US stocks
        List<String> usSymbols = Arrays.asList("AAPL", "TSLA", "GOOGL", "AMZN", "MSFT");
        for (String sym : usSymbols) {
            stockRepository.findBySymbol(sym).ifPresent(stock -> {
                try {
                    stockRepository.delete(stock);
                } catch (Exception e) {
                    stock.setSymbol(stock.getSymbol() + "_OLD");
                    stockRepository.save(stock);
                }
            });
        }

        if (userRepository.count() == 0) {
            User guest = new User("guest", PasswordUtil.hashPassword("guest"), 1000000.00, "Initial Question", "Initial Answer");
            userRepository.save(guest);
        }
    }
}
