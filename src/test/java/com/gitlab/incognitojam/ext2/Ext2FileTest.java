package com.gitlab.incognitojam.ext2;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static com.gitlab.incognitojam.ext2.Inode.FileModes.*;
import static org.junit.Assert.*;

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

    @Test
    public void testReadDirectStart() throws FileNotFoundException {
        final byte[] expected = { 0x44, 0x69, 0x72, 0x65, 0x63, 0x74, 0x20, 0x73, 0x74, 0x61, 0x72, 0x74, 0x0a };

        Ext2File file = new Ext2File(volume, "/files/dir-s");
        byte[] data = file.read(file.getSize());

        assertArrayEquals(expected, data);
    }

    @Test
    public void testReadDirectEnd() throws FileNotFoundException {
        final byte[] expected = new byte[0xb];
        expected[0x0] = 0x44; // D
        expected[0x1] = 0x69; // i
        expected[0x2] = 0x72; // r
        expected[0x3] = 0x65; // e
        expected[0x4] = 0x63; // c
        expected[0x5] = 0x74; // t
        expected[0x6] = 0x20; //
        expected[0x7] = 0x65; // e
        expected[0x8] = 0x6e; // n
        expected[0x9] = 0x64; // d
        expected[0xa] = 0x0a; // \n

        Ext2File file = new Ext2File(volume, "/files/dir-e");
        file.seek(file.getSize() - expected.length);
        byte[] data = file.read(expected.length);

        assertArrayEquals(expected, data);
    }

    @Test
    public void testReadIndirectStart() throws FileNotFoundException {
        final byte[] expected = new byte[0xf];
        expected[0x0] = 0x49; // I
        expected[0x1] = 0x6e; // n
        expected[0x2] = 0x64; // d
        expected[0x3] = 0x69; // i
        expected[0x4] = 0x72; // r
        expected[0x5] = 0x65; // e
        expected[0x6] = 0x63; // c
        expected[0x7] = 0x74; // t
        expected[0x8] = 0x20; //
        expected[0x9] = 0x73; // s
        expected[0xa] = 0x74; // t
        expected[0xb] = 0x61; // a
        expected[0xc] = 0x72; // r
        expected[0xd] = 0x74; // t
        expected[0xe] = 0x0a; // \n

        Ext2File file = new Ext2File(volume, "/files/ind-s");
        file.seek(file.getSize() - expected.length);
        byte[] data = file.read(expected.length);

        assertArrayEquals(expected, data);
    }

    @Test
    public void testReadIndirectEnd() throws FileNotFoundException {
        final byte[] expected = new byte[0xd];
        expected[0x0] = 0x49; // I
        expected[0x1] = 0x6e; // n
        expected[0x2] = 0x64; // d
        expected[0x3] = 0x69; // i
        expected[0x4] = 0x72; // r
        expected[0x5] = 0x65; // e
        expected[0x6] = 0x63; // c
        expected[0x7] = 0x74; // t
        expected[0x8] = 0x20; //
        expected[0x9] = 0x65; // e
        expected[0xa] = 0x6e; // n
        expected[0xb] = 0x64; // d
        expected[0xc] = 0x0a; // \n

        Ext2File file = new Ext2File(volume, "/files/ind-e");
        file.seek(file.getSize() - expected.length);
        byte[] data = file.read(expected.length);

        assertArrayEquals(expected, data);
    }

    @Test
    public void testReadDoubleIndirectStart() throws FileNotFoundException {
        final byte[] expected = new byte[0x16];
        expected[0x00] = 0x44; // D
        expected[0x01] = 0x6f; // o
        expected[0x02] = 0x75; // u
        expected[0x03] = 0x62; // b
        expected[0x04] = 0x6c; // l
        expected[0x05] = 0x65; // e
        expected[0x06] = 0x20; //
        expected[0x07] = 0x69; // i
        expected[0x08] = 0x6e; // n
        expected[0x09] = 0x64; // d
        expected[0x0a] = 0x69; // i
        expected[0x0b] = 0x72; // r
        expected[0x0c] = 0x65; // e
        expected[0x0d] = 0x63; // c
        expected[0x0e] = 0x74; // t
        expected[0x0f] = 0x20; //
        expected[0x10] = 0x73; // s
        expected[0x11] = 0x74; // t
        expected[0x12] = 0x61; // a
        expected[0x13] = 0x72; // r
        expected[0x14] = 0x74; // t
        expected[0x15] = 0x0a; // \n

        Ext2File file = new Ext2File(volume, "/files/dbl-ind-s");
        file.seek(file.getSize() - expected.length);
        byte[] data = file.read(expected.length);

        assertArrayEquals(expected, data);
    }

    @Test
    public void testReadDoubleIndirectEnd() throws FileNotFoundException {
        final byte[] expected = new byte[0x14];
        expected[0x00] = 0x44; // D
        expected[0x01] = 0x6f; // o
        expected[0x02] = 0x75; // u
        expected[0x03] = 0x62; // b
        expected[0x04] = 0x6c; // l
        expected[0x05] = 0x65; // e
        expected[0x06] = 0x20; //
        expected[0x07] = 0x69; // i
        expected[0x08] = 0x6e; // n
        expected[0x09] = 0x64; // d
        expected[0x0a] = 0x69; // i
        expected[0x0b] = 0x72; // r
        expected[0x0c] = 0x65; // e
        expected[0x0d] = 0x63; // c
        expected[0x0e] = 0x74; // t
        expected[0x0f] = 0x20; //
        expected[0x10] = 0x65; // e
        expected[0x11] = 0x6e; // n
        expected[0x12] = 0x64; // d
        expected[0x13] = 0x0a; // \n

        Ext2File file = new Ext2File(volume, "/files/dbl-ind-e");
        file.seek(file.getSize() - expected.length);
        byte[] data = file.read(expected.length);

        assertArrayEquals(expected, data);
    }

    @Test
    public void testReadTripleIndirectStart() throws FileNotFoundException {
        final byte[] expected = new byte[0x16];
        expected[0x00] = 0x54; // T
        expected[0x01] = 0x72; // r
        expected[0x02] = 0x69; // i
        expected[0x03] = 0x70; // p
        expected[0x04] = 0x6c; // l
        expected[0x05] = 0x65; // e
        expected[0x06] = 0x20; //
        expected[0x07] = 0x69; // i
        expected[0x08] = 0x6e; // n
        expected[0x09] = 0x64; // d
        expected[0x0a] = 0x69; // i
        expected[0x0b] = 0x72; // r
        expected[0x0c] = 0x65; // e
        expected[0x0d] = 0x63; // c
        expected[0x0e] = 0x74; // t
        expected[0x0f] = 0x20; //
        expected[0x10] = 0x73; // s
        expected[0x11] = 0x74; // t
        expected[0x12] = 0x61; // a
        expected[0x13] = 0x72; // r
        expected[0x14] = 0x74; // t
        expected[0x15] = 0x0a; // \n

        Ext2File file = new Ext2File(volume, "/files/trpl-ind-s");
        file.seek(file.getSize() - expected.length);
        byte[] data = file.read(expected.length);
        assertArrayEquals(expected, data);
    }

    @Test
    public void testReadTripleIndirectEnd() throws FileNotFoundException {
        final byte[] expected = new byte[0x14];
        expected[0x00] = 0x54; // T
        expected[0x01] = 0x72; // r
        expected[0x02] = 0x69; // i
        expected[0x03] = 0x70; // p
        expected[0x04] = 0x6c; // l
        expected[0x05] = 0x65; // e
        expected[0x06] = 0x20; //
        expected[0x07] = 0x69; // i
        expected[0x08] = 0x6e; // n
        expected[0x09] = 0x64; // d
        expected[0x0a] = 0x69; // i
        expected[0x0b] = 0x72; // r
        expected[0x0c] = 0x65; // e
        expected[0x0d] = 0x63; // c
        expected[0x0e] = 0x74; // t
        expected[0x0f] = 0x20; //
        expected[0x10] = 0x65; // e
        expected[0x11] = 0x6e; // n
        expected[0x12] = 0x64; // d
        expected[0x13] = 0x0a; // \n

        Ext2File file = new Ext2File(volume, "/files/trpl-ind-e");
        file.seek(file.getSize() - expected.length);
        byte[] data = file.read(expected.length);

        assertArrayEquals(expected, data);
    }
}
