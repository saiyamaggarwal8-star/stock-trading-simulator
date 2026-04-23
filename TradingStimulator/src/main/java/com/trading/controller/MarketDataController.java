package com.trading.controller;

import com.trading.model.PriceHistory;
import com.trading.model.Stock;
import com.trading.repository.PriceHistoryRepository;
import com.trading.repository.StockRepository;
import com.trading.service.IndicatorCalculator;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController // Tells Spring this class sets up REST API endpoints
@RequestMapping("/api/market") // All routes here start with "/api/market"
@CrossOrigin(origins = "*") // Allow requests from any frontend domain
public class MarketDataController {

    // Dependencies to fetch stocks, price histories, and detect candlestick patterns
    private final StockRepository stockRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final com.trading.service.PatternDetectionService patternDetectionService;

    // Constructor for injecting dependencies
    public MarketDataController(StockRepository stockRepository, 
                               PriceHistoryRepository priceHistoryRepository,
                               com.trading.service.PatternDetectionService patternDetectionService) {
        this.stockRepository = stockRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.patternDetectionService = patternDetectionService;
    }

    // Endpoint to get a list of all available stocks
    @GetMapping("/stocks")
    public List<Stock> getAllStocks() {
        // Fetch all stocks from database, filter out old symbols, and sort alphabetically
        return stockRepository.findAll().stream()
            .filter(s -> !s.getSymbol().endsWith("_OLD")) // Exclude obsolete/old stock symbols
            .sorted((a, b) -> a.getSymbol().compareTo(b.getSymbol())) // Sort alphabetically by symbol name
            .collect(Collectors.toList()); // Collect them back into a List
    }

    // Endpoint to fetch a single stock's details by its symbol (e.g., AAPL)
    @GetMapping("/stocks/{symbol}")
    public Stock getStockBySymbol(@PathVariable String symbol) { // Extracts {symbol} from the URL
        // Tries to find the stock, throws an error if it doesn't exist
        return stockRepository.findBySymbol(symbol).orElseThrow();
    }

    // Endpoint to get the rapid real-time price history chart data
    @GetMapping("/history/{symbol}")
    public List<Map<String, Object>> getStockHistory(@PathVariable String symbol) {
        // Fetch the 20 most recent price history records for the stock
        List<PriceHistory> history = priceHistoryRepository.findTop20BySymbolOrderByCreatedAtDesc(symbol);
        
        // Convert to a simplified list of Maps (JSON objects) for the frontend chart format
        return history.stream()
            .map(ph -> {
                Map<String, Object> point = new HashMap<>(); // Create a data point dictionary
                point.put("time", ph.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm:ss"))); // Add timestamp
                point.put("price", ph.getPrice()); // Add the price
                point.put("symbol", ph.getSymbol()); // Add the symbol identifier
                return point; // Return the data point map
            })
            .collect(Collectors.toList());
    }

    /**
     * Returns live Open, High, Low, Volume for a symbol using stored price history.
     */
    @GetMapping("/stats/{symbol}")
    public Map<String, Object> getStockStats(@PathVariable String symbol) {
        // Fetch the last 100 history points to calculate High, Low, Open metrics
        List<PriceHistory> history = priceHistoryRepository.findTop100BySymbolOrderByCreatedAtDesc(symbol);

        // Map to return JSON directly
        Map<String, Object> stats = new HashMap<>();
        
        // If there's no history data, return default zeros
        if (history.isEmpty()) {
            stats.put("open", 0);
            stats.put("high", 0);
            stats.put("low", 0);
            stats.put("volume", 0);
            return stats;
        }

        // The Open price is visually defined as the oldest record in our most recent 100 (which is at the tail of the list)
        double open = history.get(history.size() - 1).getPrice();
        // The High price is the absolute maximum over the 100 entries
        double high = history.stream().mapToDouble(PriceHistory::getPrice).max().orElse(0);
        // The Low price is the absolute minimum over the 100 entries
        double low  = history.stream().mapToDouble(PriceHistory::getPrice).min().orElse(0);
        
        // Proportional mock volume estimation based on how widely the price moved (volatility)
        long volume = Math.round((high - low) / open * 1_000_000 * (1 + Math.random()));

        // Return statistics rounded cleanly to two decimal places
        stats.put("open",   Math.round(open * 100.0) / 100.0);
        stats.put("high",   Math.round(high * 100.0) / 100.0);
        stats.put("low",    Math.round(low  * 100.0) / 100.0);
        stats.put("volume", volume);

        return stats;
    }

    /**
     * Returns SMA, EMA, RSI computed from real price history.
     */
    @GetMapping("/indicators/{symbol}")
    public Map<String, Object> getIndicators(@PathVariable String symbol,
                                              @RequestParam(defaultValue = "14") int period) { // Default period is 14 for typical indicators
        // Fetch last 100 history items down to the most recent second
        List<PriceHistory> history = priceHistoryRepository.findTop100BySymbolOrderByCreatedAtDesc(symbol);

        Map<String, Object> result = new HashMap<>();

        // If there are fewer records than the requested period length, we can't reliably compute the indicator
        if (history.size() < period + 1) {
            result.put("error", "Not enough history data yet. Need at least " + (period + 1) + " data points.");
            return result;
        }

        // Map PriceHistory models to purely Double prices, and reverse order so oldest is first (ascending time order)
        List<Double> prices = history.stream()
                .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt())) // Sort so oldest first
                .map(PriceHistory::getPrice) // Extract just the pricing data
                .collect(Collectors.toList());

        // Try computing the Simple Moving Average (SMA)
        try {
            List<Double> sma = IndicatorCalculator.calculateSMA(prices, period);
            result.put("SMA", sma.get(sma.size() - 1)); // We only want the most recent SMA
        } catch (Exception ignored) {}

        // Try computing the Exponential Moving Average (EMA)
        try {
            List<Double> ema = IndicatorCalculator.calculateEMA(prices, period);
            result.put("EMA", ema.get(ema.size() - 1)); // Most recent EMA
        } catch (Exception ignored) {}

        // Try computing the Relative Strength Index (RSI)
        try {
            List<Double> rsi = IndicatorCalculator.calculateRSI(prices, period);
            result.put("RSI", rsi.get(rsi.size() - 1)); // Most recent RSI point
        } catch (Exception ignored) {}

        // Supply the final most recent current price for context
        result.put("currentPrice", prices.get(prices.size() - 1));

        return result;
    }

    // Endpoint defining the logic for getting trading chart candles (OHLC elements)
    @GetMapping("/candles/{symbol}")
    public List<com.trading.dto.CandleDTO> getCandles(@PathVariable String symbol) {
        // Fetch real current stock price to use as the anchor for mock generation if real history is sparse
        double currentStockPrice = stockRepository.findBySymbol(symbol)
                .map(Stock::getCurrentPrice).orElse(0.0);

        // Fetch all chronological price history stored in the DB
        List<PriceHistory> history = priceHistoryRepository.findAllBySymbolOrderByCreatedAtAsc(symbol);
        List<com.trading.dto.CandleDTO> candles = new java.util.ArrayList<>();

        // If no history exists at all, yield pure mock data relying heavily on the current benchmark stock price
        if (history.isEmpty()) {
            return generateMockCandles(symbol, currentStockPrice > 0 ? currentStockPrice : 1000.0);
        }

        // We compute median price of the history to find absurd outliers and ignore them
        List<Double> allPrices = history.stream()
                .map(PriceHistory::getPrice)
                .sorted()
                .collect(Collectors.toList());
        double medianPrice = allPrices.get(allPrices.size() / 2); // Center value is the median
        double lowerBound = medianPrice * 0.5; // Half context
        double upperBound = medianPrice * 2.0; // Double context

        // Filter history to only entries within the sane price range for this stock
        List<PriceHistory> cleanHistory = history.stream()
                .filter(ph -> ph.getPrice() >= lowerBound && ph.getPrice() <= upperBound)
                .collect(Collectors.toList());

        // Again, if the clean history drops everything, resort to pure mock generation
        if (cleanHistory.isEmpty()) {
            return generateMockCandles(symbol, currentStockPrice > 0 ? currentStockPrice : medianPrice);
        }

        // Aggregate raw price hits into structured 1-hour candles
        // Normalize the first hit down to the top of its hour (e.g., 09:35 -> 09:00:00)
        java.time.LocalDateTime startTime = cleanHistory.get(0).getCreatedAt().withMinute(0).withSecond(0).withNano(0);
        java.time.LocalDateTime endTime = cleanHistory.get(cleanHistory.size() - 1).getCreatedAt();

        java.time.LocalDateTime currentBucket = startTime;
        // Keep iterating hour-by-hour until we reach the most recent real hit
        while (currentBucket.isBefore(endTime) || currentBucket.isEqual(endTime)) {
            java.time.LocalDateTime nextBucket = currentBucket.plusHours(1); // 1 hour steps
            final java.time.LocalDateTime bucket = currentBucket; // Effective final variable for lambdas

            // Grab all PriceHistory points occurring inside this specific 1-hour time band
            List<PriceHistory> bucketData = cleanHistory.stream()
                .filter(ph -> !ph.getCreatedAt().isBefore(bucket) && ph.getCreatedAt().isBefore(nextBucket))
                .collect(Collectors.toList());

            if (!bucketData.isEmpty()) {
                // Determine candle factors from bucket data
                double open  = bucketData.get(0).getPrice(); // Earliest price is open
                double close = bucketData.get(bucketData.size() - 1).getPrice(); // Final price is close
                double high  = bucketData.stream().mapToDouble(PriceHistory::getPrice).max().orElse(open); // Find absolute peak in hour
                double low   = bucketData.stream().mapToDouble(PriceHistory::getPrice).min().orElse(open); // Find absolute bottom in hour

                // Add to list and format time as HH:mm
                candles.add(new com.trading.dto.CandleDTO(
                    bucket.format(DateTimeFormatter.ofPattern("HH:mm")),
                    open, high, low, close
                ));
            }
            currentBucket = nextBucket; // Move bucket slider forward to next hour
        }

        // If we don't naturally have 24 hours of data yet, randomly backpad missing candle slots so UI looks populated
        if (candles.size() < 24) {
            double mockBase = candles.isEmpty()
                ? (currentStockPrice > 0 ? currentStockPrice : medianPrice)
                : candles.get(0).getOpen(); // Use the oldest known "real" open as the base of mock backwards projection
            
            List<com.trading.dto.CandleDTO> mockHistory = generateMockCandles(symbol, mockBase);
            int needed = 24 - candles.size(); // The gap amount
            
            // Build a fresh combined list: [tail sequence of mock padding sequence] + [our real bucketed candles]
            List<com.trading.dto.CandleDTO> combined = new java.util.ArrayList<>();
            int mockStart = Math.max(0, mockHistory.size() - needed); // Select just the amount of mocked items needed to pad to 24
            combined.addAll(mockHistory.subList(mockStart, mockHistory.size()));
            combined.addAll(candles);
            return combined; // Deliver padded combo format
        }

        return candles; // Return pure gathered real candle list if big enough
    }

    // Generator method to produce a realistic-looking mock 24-candle array seeded by standard deviations & trend bias
    private List<com.trading.dto.CandleDTO> generateMockCandles(String symbol, double basePrice) {
        List<com.trading.dto.CandleDTO> mock = new java.util.ArrayList<>();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        // Use symbol hashCode as a deterministic seed so the same stock always
        // gets the same "look" even across backend restarts, anchored daily based on today's LocalDate
        java.util.Random rng = new java.util.Random(symbol.hashCode() + java.time.LocalDate.now().toEpochDay());

        double current = basePrice * (0.94 + rng.nextDouble() * 0.06); // Start roughly 0-6% below current price reference
        double trendBias = (rng.nextDouble() - 0.45) * 0.003; // Slight directional bias either upward/downward over period

        // Simulate going forwards historically across last 24 slots sequentially
        for (int i = 24; i > 0; i--) {
            java.time.LocalDateTime time = now.minusHours(i);

            double open = current;
            double volatility = open * 0.025; // Introduce 2.5% per-candle price volatility boundary ranges

            // Occasionally flip trend bias randomly to create realistic breakout swings
            if (rng.nextDouble() < 0.15) {
                trendBias = (rng.nextDouble() - 0.45) * 0.003;
            }

            // The body limits represent final Open->Close position using random noise coupled with our set trend logic
            double bodyMove = (rng.nextDouble() - 0.5 + trendBias) * volatility;
            double close = open + bodyMove;

            // Wicks are the long tail limits extending beyond the Open/Close box
            double highExtra = rng.nextDouble() * volatility * 0.8;
            double lowExtra  = rng.nextDouble() * volatility * 0.8;

            double high = Math.max(open, close) + highExtra;
            double low  = Math.min(open, close) - lowExtra;

            // Bundle these simulated math bounds into proper layout formats
            mock.add(new com.trading.dto.CandleDTO(
                time.format(DateTimeFormatter.ofPattern("HH:mm")),
                Math.round(open  * 100.0) / 100.0,
                Math.round(high  * 100.0) / 100.0,
                Math.round(low   * 100.0) / 100.0,
                Math.round(close * 100.0) / 100.0
            ));
            current = close; // Carry current close point sequentially onto the open value for next loop cycle
        }
        return mock;
    }

    // Simple Endpoint checking the pattern recognition Service outputs based upon gathered candle formatting
    @GetMapping("/patterns/{symbol}")
    public List<com.trading.dto.PatternDTO> getPatterns(@PathVariable String symbol) {
        // Reuse the 1h candle logic for pattern detection input mapping
        List<com.trading.dto.CandleDTO> candles = getCandles(symbol);
        // Delegate to service method
        return patternDetectionService.detectPatterns(candles);
    }
}
