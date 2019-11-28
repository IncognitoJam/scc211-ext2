package com.gitlab.incognitojam.ext2;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class Ext2FileTest {
    private static final String volumeFilename = "ext2fs";
    private static Volume volume;

    @BeforeClass
    public static void setUp() throws IOException {
        volume = new Volume(volumeFilename);
    }

    @Test
    public void testRootFile() {

    }
}
