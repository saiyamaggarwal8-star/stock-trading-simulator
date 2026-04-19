package com.trading.dto;

import java.util.List;

public class PatternDTO {
    private String name;
    private String description;
    private String type; // BULLISH, BEARISH, NEUTRAL
    private List<LineSegment> lines; // Multiple lines per pattern (e.g., Resistance & Support)

    public PatternDTO() {}

    public PatternDTO(String name, String description, String type, List<LineSegment> lines) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.lines = lines;
    }

    public static class LineSegment {
        private String label;
        private List<Point> points;

        public LineSegment() {}
        public LineSegment(String label, List<Point> points) {
            this.label = label;
            this.points = points;
        }
        public String getLabel() { return label; }
        public List<Point> getPoints() { return points; }
    }

    public static class Point {
        private String time;
        private double price;

        public Point() {}
        public Point(String time, double price) {
            this.time = time;
            this.price = price;
        }
        public String getTime() { return time; }
        public double getPrice() { return price; }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public List<LineSegment> getLines() { return lines; }
    public void setLines(List<LineSegment> lines) { this.lines = lines; }
}
