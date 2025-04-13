package io.cdap.wrangler.api.parser;
import org.junit.Test;
import org.junit.Assert;

public class ByteSizeTest {
    @Test
    public void testByteSize() {
        ByteSize byteSize = new ByteSize("10KB");
        Assert.assertEquals(10240.0, byteSize.getValue(), 0.01);
        Assert.assertEquals("KB", byteSize.getUnit());
    }

    @Test
    public void testByteSizeWithDecimal() {
        ByteSize byteSize = new ByteSize("10.5MB");
        Assert.assertEquals(11010048.0, byteSize.getValue(), 0.01);
        Assert.assertEquals("MB", byteSize.getUnit());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidByteSize() {
        new ByteSize("10XYZ");
    }
}
