package com.trading.service;

import com.trading.dto.CandleDTO;
import com.trading.dto.PatternDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class PatternDetectionService {

    // Primary entry point to scan an entire candlestick chart for known technical patterns
    public List<PatternDTO> detectPatterns(List<CandleDTO> candles) {
        List<PatternDTO> detected = new ArrayList<>();
        // Require at least 10 candles of history to form any meaningful pattern
        if (candles.size() < 10) return detected;

        // Run independent pattern matching algorithms
        detectTripleBottom(candles, detected);
        detectTriangles(candles, detected);
        
        // Add a fallback "Trend" pattern if nothing advanced is found, ensuring the UI always has educational feedback
        if (detected.isEmpty()) {
            detectGeneralTrend(candles, detected);
        }
        
        return detected;
    }

    // Looks for a "Triple Bottom" pattern where the price drops to the same support level three times
    private void detectTripleBottom(List<CandleDTO> candles, List<PatternDTO> detected) {
        List<Integer> lowIndices = new ArrayList<>();
        // Find local "valleys" - candles which are lower than both their immediate neighbors
        for (int i = 1; i < candles.size() - 1; i++) {
            if (candles.get(i).getLow() < candles.get(i-1).getLow() && 
                candles.get(i).getLow() < candles.get(i+1).getLow()) {
                lowIndices.add(i); // Store the index of the valley
            }
        }

        // We need at least 3 distinct valleys to test for a Triple Bottom
        if (lowIndices.size() >= 3) {
            // Iterate backwards to find the most recent 3 consecutive valleys that match the standard
            for (int k = lowIndices.size() - 1; k >= 2; k--) {
                int i3 = lowIndices.get(k);
                int i2 = lowIndices.get(k - 1);
                int i1 = lowIndices.get(k - 2);

                double l1 = candles.get(i1).getLow();
                double l2 = candles.get(i2).getLow();
                double l3 = candles.get(i3).getLow();

                // Calculate the average price of these three bottoms to form a baseline
                double avgLow = (l1 + l2 + l3) / 3;
                // Allow a 1.5% margin of error since real stock charts are rarely perfectly flat
                double threshold = avgLow * 0.015;

                // If the valleys are roughly equal (within threshold), we found a support line
                if (Math.abs(l1 - l2) < threshold && Math.abs(l2 - l3) < threshold) {
                    // Build coordinate points to draw on the frontend chart
                    List<PatternDTO.Point> points = new ArrayList<>();
                    points.add(new PatternDTO.Point(candles.get(i1).getTime(), l1));
                    points.add(new PatternDTO.Point(candles.get(i2).getTime(), l2));
                    points.add(new PatternDTO.Point(candles.get(i3).getTime(), l3));

                    // Add to the list of detected shapes
                    detected.add(new PatternDTO(
                        "Triple Bottom",
                        "A bullish reversal pattern with three roughly equal lows indicating strong support.",
                        "BULLISH",
                        Arrays.asList(new PatternDTO.LineSegment("Support", points))
                    ));
                    break; // Only capture the most recently valid pattern found
                }
            }
        }
    }

    // Looks for narrowing "Triangle" or "Wedge" consolidation patterns over the last 15 candles
    private void detectTriangles(List<CandleDTO> candles, List<PatternDTO> detected) {
        int n = candles.size();
        if (n < 15) return;

        // Take a snapshot of the oldest and newest candles in the 15-period scope
        int startIdx = n - 15;
        int endIdx = n - 1;
        
        double highStart = candles.get(startIdx).getHigh();
        double highEnd = candles.get(endIdx).getHigh();
        double lowStart = candles.get(startIdx).getLow();
        double lowEnd = candles.get(endIdx).getLow();

        // Calculate slopes. If highs are dropping, it's a descending ceiling.
        boolean descendingResistance = highEnd < highStart * 0.995;
        // If lows are rising, it's an ascending floor.
        boolean ascendingSupport = lowEnd > lowStart * 1.005;
        // If lows are not moving, it's a flat floor.
        boolean flatSupport = Math.abs(lowEnd - lowStart) < lowStart * 0.008;

        boolean flatResistance = Math.abs(highEnd - highStart) < highStart * 0.008;

        // Both squeeze together
        if (descendingResistance && ascendingSupport) {
            detected.add(createTrianglePattern("Symmetrical Triangle", "Consolidation before a major breakout.", "NEUTRAL", candles, startIdx, endIdx));
        } 
        // Ceiling drops but floor is flat
        else if (descendingResistance && flatSupport) {
            detected.add(createTrianglePattern("Descending Triangle", "Bearish pattern with flat support and lower highs.", "BEARISH", candles, startIdx, endIdx));
        } 
        // Ceiling is flat but floor rises
        else if (flatResistance && ascendingSupport) {
            detected.add(createTrianglePattern("Ascending Triangle", "Bullish pattern with flat resistance and higher lows.", "BULLISH", candles, startIdx, endIdx));
        }
        // Ceiling drops, and floor drops natively (implied by previous logic) - falling wedge shape
        else if (descendingResistance) {
            detected.add(createTrianglePattern("Falling Wedge", "Bullish reversal pattern as price narrows downwards.", "BULLISH", candles, startIdx, endIdx));
        }
    }

    // Fallback: Just draw a trendline from start to end if the stock went up overall
    private void detectGeneralTrend(List<CandleDTO> candles, List<PatternDTO> detected) {
        int n = candles.size();
        double startPrice = candles.get(0).getClose();
        double endPrice = candles.get(n-1).getClose();
        
        // Connect point A to point B
        List<PatternDTO.Point> p = new ArrayList<>();
        p.add(new PatternDTO.Point(candles.get(0).getTime(), startPrice));
        p.add(new PatternDTO.Point(candles.get(n-1).getTime(), endPrice));
        
        // Categorize into Ascending, Descending, or Sideways
        if (endPrice > startPrice * 1.02) {
            detected.add(new PatternDTO("Ascending Trend", "A clear series of higher highs and higher lows.", "BULLISH", 
                Arrays.asList(new PatternDTO.LineSegment("Trendline", p))));
        } else if (endPrice < startPrice * 0.98) {
            detected.add(new PatternDTO("Descending Trend", "A clear bearish trend with lower highs and lower lows.", "BEARISH", 
                Arrays.asList(new PatternDTO.LineSegment("Trendline", p))));
        } else {
            detected.add(new PatternDTO("Sideways Market", "Consolidation with no clear trend direction.", "NEUTRAL", 
                Arrays.asList(new PatternDTO.LineSegment("Trendline", p))));
        }
    }

    // Helper: Formats the data endpoints into DTOs that the frontend can read and draw SVGs over
    private PatternDTO createTrianglePattern(String name, String desc, String type, List<CandleDTO> candles, int start, int end) {
        // Draw ceiling
        List<PatternDTO.Point> resPoints = new ArrayList<>();
        resPoints.add(new PatternDTO.Point(candles.get(start).getTime(), candles.get(start).getHigh()));
        resPoints.add(new PatternDTO.Point(candles.get(end).getTime(), candles.get(end).getHigh()));

        // Draw floor
        List<PatternDTO.Point> supPoints = new ArrayList<>();
        supPoints.add(new PatternDTO.Point(candles.get(start).getTime(), candles.get(start).getLow()));
        supPoints.add(new PatternDTO.Point(candles.get(end).getTime(), candles.get(end).getLow()));

        return new PatternDTO(name, desc, type, Arrays.asList(
            new PatternDTO.LineSegment("Resistance", resPoints),
            new PatternDTO.LineSegment("Support", supPoints)
        ));
    }
}
