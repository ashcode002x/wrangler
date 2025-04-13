package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;

public class TimeDuration implements Token {
    private final long nanoseconds;
    private final String unit;

    public TimeDuration(String value) {
        super();
        // Parse value like "10ms" into numeric and unit components
        String numeric = value.replaceAll("[^0-9.]", "");
        String unit = value.replaceAll("[0-9.]", "").toLowerCase();

        double numericValue = Double.parseDouble(numeric);
        nanoseconds = convertToNanoseconds(numericValue, unit);
        this.unit=unit;
    }

    private long convertToNanoseconds(double value, String unit) {
        switch (unit) {
            case "ns": return (long) value;
            case "ms": return (long) (value * 1_000_000);
            case "s": return (long) (value * 1_000_000_000);
            case "m": return (long) (value * 60 * 1_000_000_000);
            case "h": return (long) (value * 60 * 60 * 1_000_000_000);
            case "d": return (long) (value * 24 * 60 * 60 * 1_000_000_000);
            default: throw new IllegalArgumentException("Unknown time unit: " + unit);
        }
    }

    public long getNanoseconds() {
        return nanoseconds;
    }

    public double getMilliseconds() {
        return nanoseconds / 1_000_000.0;
    }

    public double getSeconds() {
        return nanoseconds / 1_000_000_000.0;
    }

    public long getValue(){
        return nanoseconds;
    }
    public String getUnit(){
        return unit;
    }

    @Override
    public Object value() {
        return this.nanoseconds;
    }

    @Override
    public TokenType type() {
        return TokenType.TIME_DURATION;
    }

    @Override
    public JsonElement toJson() {
        return null;
    }

    // Add other unit conversion methods as needed
}