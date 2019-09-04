package com.gitlab.incognitojam.ext2;

import java.io.*;
import java.nio.ByteBuffer;

public class Volume {
    /**
     * Filesystem block size in bytes.
     *
     * Defined in coursework spec.
     */
    private static final int fsBlockSize = 1024;

    private final File fsImage;

    private String label;
    private int blocks;
    private int capacity;

    public Volume(String fsImagePath) throws IOException {
        this.fsImage = new File(fsImagePath);
        this.initialise();
    }

    private void initialise() throws IOException {
        // Read superblock from file
        RandomAccessFile file = new RandomAccessFile(this.fsImage, "r");
        file.skipBytes(1024);

        byte[] superblock = new byte[1024];
        file.read(superblock);
        file.close();

        // Read properties from superblock
        ByteBuffer buffer = ByteBuffer.wrap(superblock);

        int inodes = buffer.getInt(0);
        blocks = buffer.getInt(4);
        int groupBlocks = buffer.getInt(32);
        int groupInodes = buffer.getInt(40);
        short magic = buffer.getShort(56);
        int inodeSize = buffer.getInt(88);

        buffer.position(120);
        byte[] labelBytes = new byte[16];
        buffer.get(labelBytes, 0, 16);
        label = new String(labelBytes);

        capacity = blocks * fsBlockSize;

        System.out.printf("Magic: 0x%04X\n", magic);
        System.out.println("Label: " + label);
        System.out.println("Blocks: " + blocks);
        System.out.println("Block size: " + ByteUtils.formatHumanReadable(fsBlockSize));
        System.out.println("Capacity: " + ByteUtils.formatHumanReadable(capacity));
    }

    public String getLabel() {
        return label;
    }

    public int getBlockSize() {
        return fsBlockSize;
    }

    public int getBlocks() {
        return blocks;
    }

    public int getCapacity() {
        return capacity;
    }
}
