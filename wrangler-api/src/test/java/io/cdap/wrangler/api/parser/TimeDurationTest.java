package io.cdap.wrangler.api.parser;
import org.junit.Test;
import org.junit.Assert;

public class TimeDurationTest {
    @Test
    public void testTimeDuration() {
        TimeDuration timeDuration = new TimeDuration("10s");
        Assert.assertEquals(10_000_000_000L, timeDuration.getValue(), 0.01);
        Assert.assertEquals("s", timeDuration.getUnit());
    }

    @Test
    public void testTimeDurationWithDecimal() {
        TimeDuration timeDuration = new TimeDuration("10.5m");
        Assert.assertEquals(630_000_000_000L, timeDuration.getValue(), 0.01);;
        Assert.assertEquals("m", timeDuration.getUnit());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTimeDuration() {
        new TimeDuration("10XYZ");
    }
}
