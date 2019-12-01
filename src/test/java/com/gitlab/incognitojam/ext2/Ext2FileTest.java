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
    private static Ext2File root;

    @BeforeClass
    public static void setUp() throws IOException {
        volume = new Volume(volumeFilename);
        root = new Ext2File(volume, "/");
    }

    @Test
    public void testRootFile() {
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

    @Test(expected = IllegalArgumentException.class)
    public void testReadLongLength() {
        root.read(0L, (long) Integer.MAX_VALUE + 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadNegativeLength() {
        root.read(0L, -1L);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadBeyondFile() {
        root.read(root.getSize(), 1L);
    }

    @Test
    public void testSeekBeyondFile() {
        /*
         * This call should not throw an exception, since seeking beyond the
         * bounds of the file is valid. This would happen when creating a hole
         * in the file, if writing was supported.
         */
        root.seek(root.getSize() + 1024L);
    }
}
