package com.gitlab.incognitojam.ext2;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ByteUtilsTest {
    /**
     * The size of an "order of magnitude" of bytes.
     */
    private static final long unit = 1024L;

    @Test
    public void testFormatHumanReadable() {
        assertEquals("1", ByteUtils.formatHumanReadable(1));
        assertEquals("1.0K", ByteUtils.formatHumanReadable(unit));
        assertEquals("1.0M", ByteUtils.formatHumanReadable(unit * unit));
        assertEquals("1.0G", ByteUtils.formatHumanReadable(unit * unit * unit));
        assertEquals("1.0T", ByteUtils.formatHumanReadable(unit * unit * unit * unit));
        assertEquals("1.0P", ByteUtils.formatHumanReadable(unit * unit * unit * unit * unit));
        assertEquals("1.0E", ByteUtils.formatHumanReadable(unit * unit * unit * unit * unit * unit));

        assertEquals("976.6K", ByteUtils.formatHumanReadable(1_000_000L));
    }

    @Test
    public void testFormatHexBytes() {
        final byte[] test1 = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, };
        String test1Formatted = ByteUtils.formatHexBytes(test1, false);
        final String test1Expected = "0000000: 00 00 00 00 00 00 00 00 00 00                    ..........      ";
        assertEquals(test1Expected, test1Formatted);

        final byte[] test2 = "Hello, world!".getBytes();
        String test2Formatted = ByteUtils.formatHexBytes(test2, false, false, false);
        final String test2Expected = "48 65 6c 6c 6f 2c 20 77 6f 72 6c 64 21           Hello, world!   ";
        assertEquals(test2Expected, test2Formatted);

        final byte[] test3 = new byte[] {
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        };
        String test3Formatted = ByteUtils.formatHexBytes(test3, false, true, true);
        final String test3Expected = "" +
                "0000000: 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  ................\n" +
                "*\n" +
                "0000030: 00 00 00 00 00 00 00 00 00 00 00 00 00 00        ..............  ";
        assertEquals(test3Expected, test3Formatted);
    }
}
