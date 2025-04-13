package io.cdap.directives.aggregates;

import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.Token;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public class AggregateSizeTimeTest {

    private AggregateSizeTime directive;
    private Arguments mockArgs;
    private ExecutorContext mockContext;

    @Before
    public void setup() {
        directive = new AggregateSizeTime();
        mockArgs = createMockArguments("inputSize", "inputTime", "outputSize", "outputTime", "MB", "avg");
        mockContext = Mockito.mock(ExecutorContext.class);
        directive.initialize(mockArgs);
    }

    private Arguments createMockArguments(String srcSize, String srcTime, String dstSize, String dstTime, String unit, String aggregation) {
        Arguments args = Mockito.mock(Arguments.class);
        when(args.value("srcSizeCol")).thenReturn(createToken(srcSize));
        when(args.value("srcTimeCol")).thenReturn(createToken(srcTime));
        when(args.value("dstSizeCol")).thenReturn(createToken(dstSize));
        when(args.value("dstTimeCol")).thenReturn(createToken(dstTime));

        // Setup optional arguments
        when(args.contains("outputSizeUnit")).thenReturn(true);
        when(args.value("outputSizeUnit")).thenReturn(createToken(unit));
        when(args.contains("aggregationType")).thenReturn(true);
        when(args.value("aggregationType")).thenReturn(createToken(aggregation));
        return args;
    }

    private Token createToken(String value) {
        Token token = Mockito.mock(Token.class);
        when(token.toString()).thenReturn(value);
        return token;
    }

    @Test
    public void testAverageAggregationInMB() {
        // Prepare sample rows with correct size and time formats
        Row row1 = new Row().add("inputSize", "3MB").add("inputTime", "2000ms");
        Row row2 = new Row().add("inputSize", "4MB").add("inputTime", "4000ms");

        List<Row> inputRows = Arrays.asList(row1, row2);

        // Execute the directive
        List<Row> outputRows = directive.execute(inputRows, mockContext);

        // Validate the output
        assertNotNull("Result should not be null", outputRows);
        assertEquals("Expected exactly one result row", 1, outputRows.size());

        Row resultRow = outputRows.get(0);
        try {
            Object size = resultRow.getValue("outputSize");
            Object time = resultRow.getValue("outputTime");

            assertNotNull(size);
            assertNotNull(time);

            double actualSize = ((Number) size).doubleValue();
            double actualTime = ((Number) time).doubleValue();

            // Expected average: (1MB + 2MB) / 2 = 1.5MB and time average: (1000ms + 3000ms) / 2 = 2000ms
            // Expected average: (3MB + 4MB) / 2 = 3.5MB and time average: (2000ms + 4000ms) / 2 = 3000ms
            assertEquals("Average size should be 3.5 MB", 3.5, actualSize, 0.01);
            assertEquals("Average time should be 3000 ms", 3000.0, actualTime, 0.01);
        } catch (Exception e) {
            fail("Unable to access output values: " + e.getMessage());
        }
    }
}
