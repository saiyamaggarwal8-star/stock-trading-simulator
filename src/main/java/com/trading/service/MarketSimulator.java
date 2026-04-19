package com.trading.service;

import com.trading.model.Stock;
import com.trading.model.PriceHistory;
import com.trading.repository.PriceHistoryRepository;
import com.trading.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MarketSimulator {
    private static final Logger logger = LoggerFactory.getLogger(MarketSimulator.class);

    private final StockRepository stockRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MarketSimulator(StockRepository stockRepository, PriceHistoryRepository priceHistoryRepository) {
        this.stockRepository = stockRepository;
        this.priceHistoryRepository = priceHistoryRepository;
    }

    // Scheduled task to fetch real-world prices from Yahoo Finance every 60 seconds (60,000 ms)
    @Scheduled(fixedRate = 60000)
    public void refreshFromYahooFinance() {
        try {
            // Must send browser-like headers to avoid Yahoo blocking Java/Bot requests
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers); // Wrap headers in request entity

            // Fetch all stocks from DB; ignore mock legacy stocks ending in "_OLD"
            List<Stock> stocks = stockRepository.findAll().stream()
                    .filter(s -> !s.getSymbol().endsWith("_OLD"))
                    .toList();

            // Iterate through every active stock to update its price
            for (Stock stock : stocks) {
                try {
                    // Yahoo Finance suffix mapping for Indian Stocks (NSE)
                    String yahooSymbol = stock.getSymbol() + ".NS";
                    // Build the API URL for a 1-day chart with a 1-day interval
                    String url = "https://query1.finance.yahoo.com/v8/finance/chart/"
                            + yahooSymbol + "?interval=1d&range=1d";

                    // Execute the HTTP GET request to Yahoo
                    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

                    // Check if request was successful and body exists
                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                        // Parse JSON response into a navigable Tree structure
                        JsonNode root = objectMapper.readTree(response.getBody());
                        // Drill down JSON payload to find the metadata container
                        JsonNode meta = root.path("chart").path("result").get(0).path("meta");

                        // Extract the live market price
                        double regularMarketPrice = meta.path("regularMarketPrice").asDouble(0);
                        // Extract previous day's closing price for % change calculation
                        double previousClose = meta.path("chartPreviousClose").asDouble(0);

                        if (regularMarketPrice > 0) {
                            // Update the stock's current price locally, rounded to 2 decimal places
                            stock.setCurrentPrice(Math.round(regularMarketPrice * 100.0) / 100.0);

                            // Calculate the daily percentage change
                            if (previousClose > 0) {
                                double pctChange = ((regularMarketPrice - previousClose) / previousClose) * 100.0;
                                stock.setPercentChange(Math.round(pctChange * 100.0) / 100.0);
                            }
                            // Persist updated price to DB
                            stockRepository.save(stock);
                            logger.info("Yahoo Finance: {} = ₹{} ({:+.2f}%)", stock.getSymbol(), regularMarketPrice, stock.getPercentChange());
                        }
                    }
                    // Delay slightly to prevent rate-limiting by Yahoo APIs
                    Thread.sleep(300);
                } catch (Exception e) {
                    logger.warn("Yahoo Finance failed for {}: {}", stock.getSymbol(), e.getMessage());
                }
            }
            logger.info("Yahoo Finance refresh complete");
        } catch (Exception e) {
            logger.error("Error during Yahoo Finance refresh", e);
        }
    }

    // Live Simulator: Runs every 10 seconds (10,000 ms) — adds realistic micro-movements between the 60s Yahoo refreshes
    @Scheduled(fixedRate = 10000)
    public void simulateLiveTicks() {
        try {
            // Get valid stocks again
            List<Stock> stocks = stockRepository.findAll().stream()
                    .filter(s -> !s.getSymbol().endsWith("_OLD"))
                    .toList();

            for (Stock stock : stocks) {
                double currentPrice = stock.getCurrentPrice();
                double prevPrice = currentPrice;

                // Random walk logic: 5% chance of a "spike" (±0.20%), 95% chance of normal "noise" (±0.05%)
                double rand = Math.random();
                double jitter;
                if (rand < 0.05) { // Spike
                    jitter = 1 + (Math.random() * 0.004 - 0.002);  // Scale price by factor between 0.998 and 1.002
                } else { // Noise
                    jitter = 1 + (Math.random() * 0.001 - 0.0005); // Scale price by factor between 0.9995 and 1.0005
                }

                // Apply jitter to calculate new price
                double newPrice = Math.round(currentPrice * jitter * 100.0) / 100.0;
                // Floor prevention: prevents stock from dropping below ₹1
                if (newPrice < 1) newPrice = currentPrice;

                // Update percentChange incrementally relative to the last stored change
                // Calculate the tiny delta from this 10-second tick
                double existingPct = stock.getPercentChange();
                double tickDelta = ((newPrice - prevPrice) / prevPrice) * 100.0;
                // Add the delta to the existing day's change
                double newPct = Math.round((existingPct + tickDelta) * 100.0) / 100.0;

                // Save simulated tick to the Stock table metadata
                stock.setCurrentPrice(newPrice);
                stock.setPercentChange(newPct);
                stockRepository.save(stock);

                // Save this specific price snapshot to the historical chart data series
                priceHistoryRepository.save(new PriceHistory(stock.getSymbol(), newPrice));
            }
        } catch (Exception e) {
            logger.error("Error during live tick simulation", e);
        }
    }
}
