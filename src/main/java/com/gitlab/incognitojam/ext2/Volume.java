package com.gitlab.incognitojam.ext2;

import java.io.*;
import java.nio.ByteBuffer;

public class Volume implements Closeable {
    private final RandomAccessFile fsFile;
    private final Superblock superblock;

    public Volume(String filepath) throws IOException {
        // Open the volume file for reading
        fsFile = new RandomAccessFile(filepath, "r");

        // Skip the first 1024 bytes, always the boot sector
        fsFile.skipBytes(1024);

        // Read the next 1024 bytes, always the super block
        byte[] superblockBytes = new byte[1024];
        fsFile.readFully(superblockBytes, 0, 1024);

        // Create a Superblock object from these bytes
        superblock = new Superblock(ByteBuffer.wrap(superblockBytes));
    }

    /**
     * Read a block of data from the disk.
     * <p>
     * {@link Superblock} <b>must</b> be read before use, as the block size
     * value is required to calculate the block offset.
     * <p>
     * The bytes are read from the starting offset of the block, and includes
     * the entire range of the block (see {@link Superblock#getFsBlockSize}).
     *
     * @param index the block index
     * @return Returns a ByteBuffer backed by an array of bytes read from the
     * disk.
     * @throws IndexOutOfBoundsException throws if index is not in range
     *                                   [0, {@link Volume#getBlocks})
     */
    private ByteBuffer readBlock(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= superblock.getBlocksCount())
            throw new IndexOutOfBoundsException("block index must be in range [0, Volume#getBlocks())");

        // Location of the block from the start of the disk
        final int offset = 1024 + index * superblock.getFsBlockSize();

        // Create backing array for bytes, size determined from block size
        byte[] bytes = new byte[superblock.getFsBlockSize()];

        try {
            // Seek to offset and read the bytes into the array
            fsFile.seek(offset);
            fsFile.readFully(bytes);
        } catch (IOException ignored) {
        }

        return ByteBuffer.wrap(bytes);
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
}
