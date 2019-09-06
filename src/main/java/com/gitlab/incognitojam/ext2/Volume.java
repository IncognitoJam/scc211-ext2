package com.gitlab.incognitojam.ext2;

import java.io.*;
import java.nio.ByteBuffer;

public class Volume implements Closeable {
    private final RandomAccessFile fsFile;
    private final String label;
    private final int blocksCount;
    private final int fsBlockSize;

    public Volume(String filepath) throws IOException {
        // Read superblock from file
        fsFile = new RandomAccessFile(filepath, "r");

        // skip the boot sector
        fsFile.skipBytes(1024);

        // read the superblock from disk
        byte[] superblockBytes = readBytes(fsFile, 1024);
        Superblock superblock = new Superblock(ByteBuffer.wrap(superblockBytes));

        // retrieve superblock values
        label = superblock.getVolumeLabel();
        blocksCount = superblock.getBlocksCount();
        fsBlockSize = superblock.getFsBlockSize();
    }

    @Override
    public void close() throws IOException {
        fsFile.close();
    }

    public String getLabel() {
        return label;
    }

    public int getBlocks() {
        return blocksCount;
    }

    public int getBlockSize() {
        return fsBlockSize;
    }

    public int getCapacity() {
        return blocksCount * fsBlockSize;
    }

    public void printDebugInfo() {
        System.out.println("Label: " + label);
        System.out.println("Blocks: " + blocksCount);
        System.out.println("Block size: " + ByteUtils.formatHumanReadable(fsBlockSize));
        System.out.println("Capacity: " + ByteUtils.formatHumanReadable(blocksCount * fsBlockSize));
    }

    private static byte[] readBytes(DataInput input, int size) throws IOException {
        byte[] bytes = new byte[size];
        input.readFully(bytes, 0, size);
        return bytes;
    }
}
