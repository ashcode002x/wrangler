package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;

public class ByteSize implements Token {
    private final double bytes;

    public ByteSize(String value) {
        super();
        // Parse value like "10KB" into numeric and unit components
        String numeric = value.replaceAll("[^0-9.]", "");
        String unit = value.replaceAll("[0-9.]", "").toUpperCase();

        double numericValue = Double.parseDouble(numeric);
        bytes = convertToBytes(numericValue, unit);
    }

    private double convertToBytes(double value, String unit) {
        switch (unit) {
            case "B": return value;
            case "KB": return value * 1024;
            case "MB": return value * 1024 * 1024;
            case "GB": return value * 1024 * 1024 * 1024;
            case "TB": return value * 1024 * 1024 * 1024 * 1024;
            case "PB": return value * 1024 * 1024 * 1024 * 1024 * 1024;
            case "EB": return value * 1024 * 1024 * 1024 * 1024 * 1024 * 1024;
            default: throw new IllegalArgumentException("Unknown byte unit: " + unit);
        }
    }

    public double getBytes() {
        return bytes;
    }

    public double getKilobytes() {
        return bytes / 1024;
    }

    public double getMegabytes() {
        return bytes / (1024 * 1024);
    }

    @Override
    public Object value() {
        return this.bytes;
    }

    @Override
    public TokenType type() {
        return TokenType.BYTE_SIZE;
    }

    @Override
    public JsonElement toJson() {
        return null;
    }

    // Add other unit conversion methods as needed
}