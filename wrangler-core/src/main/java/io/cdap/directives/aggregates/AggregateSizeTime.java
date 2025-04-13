package io.cdap.directives.aggregates;

import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.annotations.Description;
import io.cdap.wrangler.api.annotations.Name;
import io.cdap.wrangler.api.annotations.Syntax;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.UsageDefinition;
import io.cdap.wrangler.api.parser.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Name("aggregate-size-time")
@Description("Aggregates byte sizes and time durations into target columns, supports total or average")
@Syntax("aggregate-size-time <srcSizeCol> <srcTimeCol> <dstSizeCol> <dstTimeCol> [MB|GB] [avg|total]")
public class AggregateSizeTime implements Directive {

    private String srcSizeCol;
    private String srcTimeCol;
    private String dstSizeCol;
    private String dstTimeCol;
    private String outputUnit = "B";
    private String aggregationType = "total";

    private long totalBytes = 0;
    private long totalDurationMs = 0;
    private long count = 0;

    @Override
    public UsageDefinition define() {
        return UsageDefinition.of("srcSizeCol", TokenType.COLUMN_NAME, "Source column for byte size")
                .with("srcTimeCol", TokenType.COLUMN_NAME, "Source column for time duration")
                .with("dstSizeCol", TokenType.COLUMN_NAME, "Target column for aggregated size")
                .with("dstTimeCol", TokenType.COLUMN_NAME, "Target column for aggregated time")
                .with("outputSizeUnit", TokenType.TEXT, "Optional output unit (e.g. MB, GB)")
                .with("aggregationType", TokenType.TEXT, "Optional aggregation type (avg or total)");
    }

    @Override
    public void initialize(Arguments args) {
        srcSizeCol = args.value("srcSizeCol").toString();
        srcTimeCol = args.value("srcTimeCol").toString();
        dstSizeCol = args.value("dstSizeCol").toString();
        dstTimeCol = args.value("dstTimeCol").toString();

        if (args.contains("outputSizeUnit")) {
            outputUnit = args.value("outputSizeUnit").toString().toUpperCase();
        }
        if (args.contains("aggregationType")) {
            aggregationType = args.value("aggregationType").toString().toLowerCase();
        }
    }

    private void updateAggregates(Row row) {
        Object sizeObj = row.getValue(srcSizeCol);
        Object timeObj = row.getValue(srcTimeCol);
        if (sizeObj != null && timeObj != null) {
            long sizeBytes = (long) new ByteSize(sizeObj.toString()).getBytes();
            long timeMs = (long) new TimeDuration(timeObj.toString()).getMilliseconds();
            totalBytes += sizeBytes;
            totalDurationMs += timeMs;
            count++;
        }
    }

    private double convertSize(double sizeInBytes) {
        switch (outputUnit) {
            case "MB":
                return sizeInBytes / (1024.0 * 1024);
            case "GB":
                return sizeInBytes / (1024.0 * 1024 * 1024);
            default:
                return sizeInBytes;
        }
    }

    @Override
    public List<Row> execute(List<Row> rows, ExecutorContext context) {
        // Reset aggregation counters for every execute call
        totalBytes = 0;
        totalDurationMs = 0;
        count = 0;

        for (Row row : rows) {
            updateAggregates(row);
        }

        // Protect against division by zero
        double sizeResult = (count > 0 && aggregationType.equals("avg"))
                ? ((double) totalBytes / count) : totalBytes;
        double timeResult = (count > 0 && aggregationType.equals("avg"))
                ? ((double) totalDurationMs / count) : totalDurationMs;

        sizeResult = convertSize(sizeResult);

        List<Row> result = new ArrayList<>();
        Row outRow = new Row();
        outRow.add(dstSizeCol, sizeResult);
        outRow.add(dstTimeCol, timeResult);
        result.add(outRow);
        return result;
    }

    @Override
    public void destroy() {
        // No resources to cleanup
    }
}
