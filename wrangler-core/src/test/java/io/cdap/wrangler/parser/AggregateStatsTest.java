package io.cdap.wrangler.parser;

import io.cdap.wrangler.TestingRig;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AggregateStatsTest {

    @Test
    public void testAggregateStats() throws Exception {
        // Create sample test data
        List<Row> rows = new ArrayList<>();

        // Row 1: 10KB transfer and 200ms response
        Row row1 = new Row();
        row1.add("data_transfer_size", new ByteSize("10KB"));
        row1.add("response_time", new TimeDuration("200ms"));
        rows.add(row1);

        // Row 2: 5MB transfer and 1.5s response
        Row row2 = new Row();
        row2.add("data_transfer_size", new ByteSize("5MB"));
        row2.add("response_time", new TimeDuration("1.5s"));
        rows.add(row2);

        // Row 3: 2.5MB transfer and 500ms response
        Row row3 = new Row();
        row3.add("data_transfer_size", new ByteSize("2.5MB"));
        row3.add("response_time", new TimeDuration("500ms"));
        rows.add(row3);

        // Define the recipe
        String[] recipe = new String[] {
                "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec"
        };

        // Execute the recipe using TestingRig
        List<Row> results = TestingRig.execute(recipe, rows);

        // Verify results
        Assert.assertEquals(1, results.size());

        // Calculate expected values:
        // Total size: 10KB + 5MB + 2.5MB = 7.51MB (approximately)
        double expectedTotalSizeInMB = 7.51;

        // Total time: 200ms + 1.5s + 500ms = 2.2 seconds
        double expectedTotalTimeInSeconds = 2.2;

        Assert.assertEquals(expectedTotalSizeInMB, (double)results.get(0).getValue("total_size_mb"), 0.01);
        Assert.assertEquals(expectedTotalTimeInSeconds, (double)results.get(0).getValue("total_time_sec"), 0.01);
    }

    @Test
    public void testCustomUnitConversion() throws Exception {
        // Create sample data with different byte sizes and time durations
        List<Row> rows = new ArrayList<>();

        Row row1 = new Row();
        row1.add("data_transfer_size", new ByteSize("1024KB"));
        row1.add("response_time", new TimeDuration("2000ms"));
        rows.add(row1);

        Row row2 = new Row();
        row2.add("data_transfer_size", new ByteSize("1MB"));
        row2.add("response_time", new TimeDuration("3s"));
        rows.add(row2);

        // Define recipe with custom output units (GB and minutes)
        String[] recipe = new String[] {
                "aggregate-stats :data_transfer_size :response_time total_size_gb total_time_min GB m"
        };

        // Execute the recipe
        List<Row> results = TestingRig.execute(recipe, rows);

        // Verify correct unit conversion
        // Expected size in GB: ~0.002 GB
        // Expected time in minutes: ~0.083 minutes
        Assert.assertEquals(0.002, (double)results.get(0).getValue("total_size_gb"), 0.001);
        Assert.assertEquals(0.083, (double)results.get(0).getValue("total_time_min"), 0.001);
    }
}