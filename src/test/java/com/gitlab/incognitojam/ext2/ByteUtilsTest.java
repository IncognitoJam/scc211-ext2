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
}
