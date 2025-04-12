package io.cdap.wrangler.parser;

import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AggregateStats implements Directive {
    private String sizeColumn;
    private String timeColumn;
    private String totalSizeColumn;
    private String totalTimeColumn;
    private String sizeUnit;
    private String timeUnit;

    @Override
    public UsageDefinition define() {
        UsageDefinition.Builder builder = UsageDefinition.builder("aggregate-stats");
        builder.define("size_column", TokenType.COLUMN_NAME);
        builder.define("time_column", TokenType.COLUMN_NAME);
        builder.define("total_size_column", TokenType.COLUMN_NAME);
        builder.define("total_time_column", TokenType.COLUMN_NAME);
        builder.define("size_unit", TokenType.STRING, String.valueOf(Optional.ofNullable("MB")));
        builder.define("time_unit", TokenType.STRING, String.valueOf(Optional.ofNullable("s")));
        return builder.build();
    }

    @Override
    public void initialize(Arguments args) throws DirectiveParseException {
        this.sizeColumn = ((ColumnName) args.value("size_column")).value();
        this.timeColumn = ((ColumnName) args.value("time_column")).value();
        this.totalSizeColumn = ((ColumnName) args.value("total_size_column")).value();
        this.totalTimeColumn = ((ColumnName) args.value("total_time_column")).value();
        this.sizeUnit = ((Text) args.value("size_unit")).value();
        this.timeUnit = ((Text) args.value("time_unit")).value();
    }

    @Override
    public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
        // Get or create the aggregation store
        Map<String, Object> store = (Map<String, Object>) context.getTransientStore();

        // Initialize or get the accumulators
        double totalBytes = (double) store.getOrDefault("totalBytes", 0.0);
        long totalNanos = (long) store.getOrDefault("totalNanos", 0L);

        // Process all rows and accumulate values
        for (Row row : rows) {
            Object sizeObj = row.getValue(sizeColumn);
            Object timeObj = row.getValue(timeColumn);

            if (sizeObj instanceof ByteSize) {
                ByteSize byteSize = (ByteSize) sizeObj;
                totalBytes += byteSize.getBytes();
            }

            if (timeObj instanceof TimeDuration) {
                TimeDuration timeDuration = (TimeDuration) timeObj;
                totalNanos += timeDuration.getNanoseconds();
            }
        }

        // Store updated values
        store.put("totalBytes", totalBytes);
        store.put("totalNanos", totalNanos);

        // Check if this is the final processing step
        // Note: Check the actual API to see how to determine if this is the last batch
        boolean isLastBatch = false; // Replace with actual check from context if available

        if (isLastBatch) {
            // Convert to requested units
            double sizeInRequestedUnit = convertBytes(totalBytes, sizeUnit);
            double timeInRequestedUnit = convertNanos(totalNanos, timeUnit);

            // Create result row
            Row result = new Row();
            result.add(totalSizeColumn, sizeInRequestedUnit);
            result.add(totalTimeColumn, timeInRequestedUnit);

            return Collections.singletonList(result);
        }

        // Return empty list for intermediate batches
        return Collections.emptyList();
    }

    @Override
    public void destroy() {
        // Clean up resources if needed
    }

    private double convertBytes(double bytes, String unit) {
        switch (unit.toUpperCase()) {
            case "B": return bytes;
            case "KB": return bytes / 1024;
            case "MB": return bytes / (1024 * 1024);
            case "GB": return bytes / (1024 * 1024 * 1024);
            default: return bytes / (1024 * 1024); // Default to MB
        }
    }

    private double convertNanos(long nanos, String unit) {
        switch (unit.toLowerCase()) {
            case "ns": return nanos;
            case "ms": return nanos / 1_000_000.0;
            case "s": return nanos / 1_000_000_000.0;
            case "m": return nanos / (60.0 * 1_000_000_000);
            case "h": return nanos / (60.0 * 60 * 1_000_000_000);
            default: return nanos / 1_000_000_000.0; // Default to seconds
        }
    }
}