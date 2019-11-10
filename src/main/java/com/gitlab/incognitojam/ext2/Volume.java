package com.gitlab.incognitojam.ext2;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Volume implements Closeable {
    private final RandomAccessFile fsFile;
    private Superblock superblock;
    private BlockGroupDescriptor[] blockGroupDescriptors;

    public Volume(String filepath) throws IOException {
        this.fsFile = new RandomAccessFile(filepath, "r");
        initialiseSuperblock();
        initialiseBlockGroupDescriptorTable();
    }

    private void initialiseSuperblock() throws IOException {
        // Superblock starts at byte 1024.
        fsFile.seek(1024);

        // Read the next 1024 bytes.
        byte[] superblockBytes = new byte[1024];
        fsFile.readFully(superblockBytes, 0, 1024);

        // Create a Superblock object from these bytes.
        ByteBuffer buffer = ByteBuffer.wrap(superblockBytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        superblock = new Superblock(buffer);
    }

    private void initialiseBlockGroupDescriptorTable() {
        // Read the BGD table from disk.
        // FIXME: the index might be different for other block sizes
        ByteBuffer buffer = getBlock(2);

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
     * Read an inode from disk by its number.
     * <p>
     * Calculates the block group containing the inode, finds it's inode table
     * and reads the required bytes to construct an inode struct.
     *
     * @param inodeNumber the inode number
     * @return returns the inode from disk
     */
    private Inode getInode(int inodeNumber) {
        /*
         * Calculate the block group number from the inode number and the
         * number of inodes per group.
         */
        int blockGroupIndex = (inodeNumber - 1) / superblock.getInodesInGroup();

        // Get the block group descriptor and the inode table block index.
        int inodeTablePtr = blockGroupDescriptors[blockGroupIndex].getInodeTablePtr();

        /*
         * Calculate the local inode index from the inode number and the number
         * of inodes per group.
         */
        int localInodeIndex = (inodeNumber - 1) % superblock.getInodesInGroup();

        // Calculate the inode starting position.
        int inodePtr = inodeTablePtr * superblock.getFsBlockSize() + localInodeIndex * superblock.getInodeSize();

        // Read the inode data from disk and construct the inode.
        ByteBuffer buffer = getBytes(inodePtr, superblock.getInodeSize());
        return new Inode(buffer);
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
     */
    private ByteBuffer getBlock(int index) {
        // Location of the block from the start of the disk.
        final int offset = index * superblock.getFsBlockSize();

        return getBytes(offset, superblock.getFsBlockSize());
    }

    /**
     * Read a contiguous range of data from the disk.
     * <p>
     * The bytes are read from the provided starting offset, and includes the
     * entire length of bytes.
     *
     * @param offset the offset from the beginning of the disk to read from
     * @return Returns a ByteBuffer backed by an array of bytes read from the
     * disk.
     */
    private ByteBuffer getBytes(int offset, int length) {
        // Read the bytes from disk.
        final byte[] bytes = readRange(offset, length);

        // Construct ByteBuffer and set endianness to LITTLE_ENDIAN.
        final ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        return buffer;
    }

    /**
     * Read an array of bytes from disk given an offset and a length.
     *
     * @param offset the offset from the beginning of the disk to read from
     * @param length the number of bytes to read
     * @return Returns a byte array containing the bytes read from the disk.
     */
    private byte[] readRange(int offset, int length) {
        /*
         * Create a backing array for the data, size determined from the given
         * length argument.
         */
        final byte[] bytes = new byte[length];

        try {
            // Seek to offset.
            fsFile.seek(offset);

            // Read the data into the array.
            fsFile.readFully(bytes);
        } catch (IOException ignored) {
        }

        return bytes;
    }

    @Override
    public void close() throws IOException {
        fsFile.close();
    }

    /**
     * The volume label.
     *
     * @see Superblock#getVolumeLabel()
     */
    public String getLabel() {
        return superblock.getVolumeLabel();
    }

    /**
     * The number of blocks in the volume.
     *
     * @see Superblock#getBlocksCount()
     */
    public int getBlocks() {
        return superblock.getBlocksCount();
    }

    /**
     * The size of each block, in bytes.
     *
     * @see Superblock#getFsBlockSize()
     */
    public int getBlockSize() {
        return superblock.getFsBlockSize();
    }

    /**
     * The total capacity of the volume, equivalent to the product of the
     * number of blocks in the volume and the size of each block, in bytes.
     *
     * @see Volume#getBlocks()
     * @see Volume#getBlockSize()
     */
    public int getCapacity() {
        return getBlocks() * getBlockSize();
    }
}
