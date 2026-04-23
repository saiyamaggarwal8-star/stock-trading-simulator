package com.trading.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IndicatorCalculator {

    /**
     * Calculates Simple Moving Average (SMA).
     * The SMA is the unweighted mean of the previous 'period' data points.
     */
    public static List<Double> calculateSMA(List<Double> prices, int period) {
        if (prices == null || prices.size() < period) {
            throw new IllegalArgumentException("Not enough data to calculate SMA");
        }

        // Loop through the data to compute the average for each window segment
        return IntStream.rangeClosed(period, prices.size())
                .mapToObj(i -> prices.subList(i - period, i).stream() // Extract the sublist window of size `period`
                        .mapToDouble(Double::doubleValue)
                        .average() // Averages out the numbers plainly without weights
                        .orElse(Double.NaN))
                .collect(Collectors.toList());
    }

    /**
     * Calculates Exponential Moving Average (EMA).
     * The EMA gives a higher weighting to recent prices, reacting faster to market changes.
     */
    public static List<Double> calculateEMA(List<Double> prices, int period) {
        if (prices == null || prices.size() < period) {
            throw new IllegalArgumentException("Not enough data to calculate EMA");
        }

        List<Double> ema = new java.util.ArrayList<>();
        // The multiplier determines the speed/weight decay 
        double multiplier = 2.0 / (period + 1);

        // Calculate initial SMA for the first period; since we don't have enough history to scale, the first EMA point is just an SMA
        double initialSMA = prices.subList(0, period).stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(Double.NaN);
        ema.add(initialSMA);

        // Calculate true EMA values recursively for the remaining prices
        for (int i = period; i < prices.size(); i++) {
            double currentPrice = prices.get(i);
            // EMA calculation requires the previous day's EMA
            double previousEMA = ema.get(ema.size() - 1);
            // Smoothing formula: (Price - Last EMA) * Multiplier + Last EMA
            double currentEMA = (currentPrice - previousEMA) * multiplier + previousEMA;
            ema.add(currentEMA);
        }

        return ema;
    }

    /**
     * Calculates Relative Strength Index (RSI).
     * RSI measures the speed and change of price movements, acting as an oscillator from 0 to 100.
     * > 70 is considered Overbought, < 30 is considered Oversold.
     */
    public static List<Double> calculateRSI(List<Double> prices, int period) {
        if (prices == null || prices.size() < period + 1) {
            throw new IllegalArgumentException("Not enough data to calculate RSI");
        }

        List<Double> rsiList = new java.util.ArrayList<>();
        double avgGain = 0;
        double avgLoss = 0;

        // Step 1: Calculate initial Average Gain and Average Loss over the first window
        for (int i = 1; i <= period; i++) {
            double change = prices.get(i) - prices.get(i - 1); // Day over day delta
            if (change >= 0) {
                avgGain += change;
            } else {
                avgLoss -= change; // Track loss as a positive magnitude
            }
        }
        avgGain /= period;
        avgLoss /= period;

        // Relative Strength (RS) ratio of gain/loss
        double rs = (avgLoss == 0) ? 100 : avgGain / avgLoss;
        // The core RSI formula maps the ratio into a 100-point scale
        rsiList.add(100.0 - (100.0 / (1 + rs)));

        // Step 2: Calculate Smoothed RSI for all subsequent periods using Wilder's Smoothing Technique
        for (int i = period + 1; i < prices.size(); i++) {
            double change = prices.get(i) - prices.get(i - 1);
            double gain = (change > 0) ? change : 0;
            double loss = (change < 0) ? -change : 0; // Loss is handled positively

            // Wilder's Exponential Smoothing technique for averting recalculation loops
            avgGain = ((avgGain * (period - 1)) + gain) / period;
            avgLoss = ((avgLoss * (period - 1)) + loss) / period;

            rs = (avgLoss == 0) ? 100 : avgGain / avgLoss;
            rsiList.add(100.0 - (100.0 / (1 + rs)));
        }

        return rsiList;
    }

    /**
     * Calculates Moving Average Convergence Divergence (MACD).
     * Returns a list of MACD Line values (EMA12 - EMA26).
     */
    public static List<Double> calculateMACD(List<Double> prices) {
        int shortPeriod = 12; // Fast length
        int longPeriod = 26;  // Slow length

        if (prices.size() < longPeriod) {
            throw new IllegalArgumentException("Not enough data to calculate MACD");
        }

        // Fetch independent EMA computations
        List<Double> shortEMA = calculateEMA(prices, shortPeriod);
        List<Double> longEMA = calculateEMA(prices, longPeriod);

        // Align arrays since shortEMA array will have more entries (it needed fewer warmup periods)
        int diffSize = shortEMA.size() - longEMA.size();

        // MACD Line is literally just the fast line minus the slow line
        return IntStream.range(0, longEMA.size())
                .mapToObj(i -> shortEMA.get(i + diffSize) - longEMA.get(i))
                .collect(Collectors.toList());
    }
}
