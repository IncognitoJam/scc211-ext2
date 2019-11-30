package com.gitlab.incognitojam.ext2;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.*;
import static com.gitlab.incognitojam.ext2.Inode.FileModes.*;

public class Ext2FileTest {
    private static final String volumeFilename = "ext2fs";
    private static Volume volume;

    @BeforeClass
    public static void setUp() throws IOException {
        volume = new Volume(volumeFilename);
    }

    @Test
    public void testRootFile() throws FileNotFoundException {
        Ext2File root = new Ext2File(volume, "/");
        assertEquals("/", root.getFileName());
        assertEquals("/", root.getFilePath());

        assertEquals(1024L, root.getSize());
        assertEquals(6, root.getHardLinksCount());

        assertEquals(1415188180L, root.getCreationTime());
        assertEquals(1415188273L, root.getLastAccessTime());
        assertEquals(1415188180L, root.getLastModifiedTime());
        assertEquals(0L, root.getDeletedTime());

        assertEquals(IFDIR | IXUSR | IWUSR | IRUSR | IXGRP | IRGRP | IXOTH | IROTH, root.getFileMode());
        assertTrue(root.isDirectory());
        assertFalse(root.isRegularFile());

        assertEquals(0, root.getUnixUid());
        assertEquals(0, root.getUnixGid());
    }
}
