package com.gitlab.incognitojam.ext2;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class VolumeTest {
    private static final String volumeFilename = "ext2fs";
    private static Volume volume;

    @BeforeClass
    public static void setUp() throws IOException {
        volume = new Volume(volumeFilename);
    }

    @Test
    public void testVolumeReadsLabel() {
        assertEquals("SCC211 OS Module", volume.getLabel());
    }

    @Test
    public void testVolumeReadsBlocksCount() {
        assertEquals(20480, volume.getBlocks());
    }

    @Test
    public void testVolumeReadsBlockSize() {
        assertEquals(1024, volume.getBlockSize());
    }

    @Test
    public void testVolumeMagicValue() {
        assertEquals((short) 0xEF53, volume.getMagicValue());
    }
}
