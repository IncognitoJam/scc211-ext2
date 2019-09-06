package com.gitlab.incognitojam.ext2;

import java.io.*;
import java.nio.ByteBuffer;

public class Volume implements Closeable {
    private final RandomAccessFile fsFile;
    private final Superblock superblock;

    public Volume(String filepath) throws IOException {
        // Read superblock from file
        fsFile = new RandomAccessFile(filepath, "r");

        // skip the boot sector
        fsFile.skipBytes(1024);

        // read the superblock from disk
        byte[] superblockBytes = readBytes(fsFile, 1024);
        superblock = new Superblock(ByteBuffer.wrap(superblockBytes));
    }

    @Override
    public void close() throws IOException {
        fsFile.close();
    }

    public String getLabel() {
        return superblock.getVolumeLabel();
    }

    public int getBlocks() {
        return superblock.getBlocksCount();
    }

    public int getBlockSize() {
        return superblock.getFsBlockSize();
    }

    public int getCapacity() {
        return getBlocks() * getBlockSize();
    }

    public void printDebugInfo() {
        System.out.println("Label: " + getLabel());
        System.out.println("Blocks: " + getBlocks());
        System.out.println("Block size: " + ByteUtils.formatHumanReadable(getBlockSize()));
        System.out.println("Capacity: " + ByteUtils.formatHumanReadable(getCapacity()));
    }

    private static byte[] readBytes(DataInput input, int size) throws IOException {
        byte[] bytes = new byte[size];
        input.readFully(bytes, 0, size);
        return bytes;
    }
}
