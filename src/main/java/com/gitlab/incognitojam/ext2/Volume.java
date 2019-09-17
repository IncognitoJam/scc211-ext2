package com.gitlab.incognitojam.ext2;

import java.io.*;
import java.nio.ByteBuffer;

public class Volume implements Closeable {
    private final RandomAccessFile fsFile;
    private Superblock superblock;
    private BlockGroupDescriptor[] blockGroupDescriptors;

    public Volume(String filepath) throws IOException {
        this.fsFile = new RandomAccessFile(filepath, "r");
        setupSuperblock();
        setupBlockGroupDescriptorTable();
    }

    private void setupSuperblock() throws IOException {
        // Superblock starts at byte 1024.
        fsFile.seek(1024);

        // Read the next 1024 bytes.
        byte[] superblockBytes = new byte[1024];
        fsFile.readFully(superblockBytes, 0, 1024);

        // Create a Superblock object from these bytes.
        superblock = new Superblock(ByteBuffer.wrap(superblockBytes));
    }

    private void setupBlockGroupDescriptorTable() {
        // Read the BGD table from disk.
        // FIXME: the index might be different for other block sizes
        ByteBuffer buffer = readBlock(2);

        // number of groups = number of blocks / number of blocks per group
        final int blockGroupCount =
                (int) Math.ceil((double) superblock.getBlocksCount() / (double) superblock.getBlocksInGroup());

        // Construct the BGDs.
        blockGroupDescriptors = new BlockGroupDescriptor[blockGroupCount];
        for (int i = 0; i < blockGroupCount; i++)
            /*
             * We pass the buffer to the BGD constructor as-is, since each BGD
             * immediately follows the previous, so the buffer pointer will
             * already be in the correct position.
             */
            blockGroupDescriptors[i] = new BlockGroupDescriptor(buffer);
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
        final int offset = index * superblock.getFsBlockSize();

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
}
