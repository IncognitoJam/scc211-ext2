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
        assertEquals("1 B", ByteUtils.formatHumanReadable(1));
        assertEquals("1.0 KiB", ByteUtils.formatHumanReadable(unit));
        assertEquals("1.0 MiB", ByteUtils.formatHumanReadable(unit * unit));
        assertEquals("1.0 GiB", ByteUtils.formatHumanReadable(unit * unit * unit));
        assertEquals("1.0 TiB", ByteUtils.formatHumanReadable(unit * unit * unit * unit));
        assertEquals("1.0 PiB", ByteUtils.formatHumanReadable(unit * unit * unit * unit * unit));
        assertEquals("1.0 EiB", ByteUtils.formatHumanReadable(unit * unit * unit * unit * unit * unit));

        assertEquals("976.6 KiB", ByteUtils.formatHumanReadable(1_000_000L));
    }
}
