package com.gitlab.incognitojam.ext2;

import com.gitlab.incognitojam.ext2.Inode.FileModes;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;

/**
 * TODO(docs): write javadoc
 */
public class Volume implements Closeable {
    private final RandomAccessFile fsFile;
    private final Superblock superblock;
    private final BlockGroupDescriptor[] blockGroupDescriptors;

    /**
     * TODO(docs): write javadoc
     */
    public Volume(String filepath) throws IOException {
        this.fsFile = new RandomAccessFile(filepath, "r");
        this.superblock = readSuperblock();
        this.blockGroupDescriptors = readBlockGroupDescriptorTable();
    }

    /**
     * TODO(docs): write javadoc
     */
    private Superblock readSuperblock() {
        // Superblock starts at byte 1024 so we skip the first 1024 bytes.
        seek(1024);

        // Read the next 1024 bytes.
        byte[] superblockBytes = new byte[1024];
        read(superblockBytes);

        // Construct a Superblock object from these bytes.
        return new Superblock(superblockBytes);
    }

    /**
     * TODO(docs): write javadoc
     */
    private BlockGroupDescriptor[] readBlockGroupDescriptorTable() {
        // FIXME: the index might be different for other block sizes
        final int bgdLength = 32;
        final int bgdTableBlockPtr = 2 * getBlockSize();

        // number of groups = number of blocks / number of blocks per group
        final int blockGroupCount =
                (int) Math.ceil((double) getBlocks() / (double) superblock.getBlocksInGroup());

        // Construct the BGDs.
        BlockGroupDescriptor[] bgdTable = new BlockGroupDescriptor[blockGroupCount];
        for (int i = 0; i < blockGroupCount; i++) {
            byte[] bgdBytes = new byte[bgdLength];
            int ptr = bgdTableBlockPtr + i * bgdLength;
            seek(ptr);
            read(bgdBytes);
            bgdTable[i] = new BlockGroupDescriptor(bgdBytes);
        }

        return bgdTable;
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
    Inode getInode(int inodeNumber) {
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
        int inodePtr = inodeTablePtr * getBlockSize()
                + localInodeIndex * superblock.getInodeSize();

        // Read the inode data from disk and construct the inode.
        byte[] inodeBytes = new byte[superblock.getInodeSize()];
        seek(inodePtr);
        read(inodeBytes);
        return new Inode(this, inodeBytes);
    }

    /**
     * TODO(docs): write javadoc
     */
    Inode navigate(String path) {
        /*
         * Split the given filepath into "parts" where each part is a file or
         * directory label in the tree. We follow the tree to find the Inode
         * at the path.
         */
        if (path.startsWith(Ext2File.pathSeparator))
            path = path.substring(1);
        final String[] parts = path.split(Ext2File.pathSeparator);

        Inode inode = getInode(2);

        // If the path is empty then return the root inode.
        if (path.length() == 0)
            return inode;

        /*
         * Iterate over the parts of the path, retrieving the directory entries
         * for the particular node if it is a directory.
         *
         * If a part of the path is not a directory, or the file does not exist
         * at the end of the path, return null.
         */
        List<DirectoryEntry> entries = inode.getEntries();
        for (int i = 0; i < parts.length; i++) {
            final String part = parts[i];

            /*
             * Iterate over the directory entries looking for the entry with
             * same label as this part of the path.
             */
            DirectoryEntry entry = null;
            for (DirectoryEntry anEntry : entries)
                if (anEntry.getLabel().equals(part))
                    entry = anEntry;
            if (entry == null)
                return null;

            inode = getInode(entry.getInode());

            if (i < parts.length - 1 && (inode.getFileMode() & FileModes.IFDIR) != FileModes.IFDIR)
                /*
                 * If this isn't the end of the path and the inode isn't a
                 * directory, return null as the file can't exist.
                 */
                return null;
            else if (i < parts.length - 1)
                entries = inode.getEntries();
        }

        /*
         * If we reach this point, it means we found the Inode for the given
         * path.
         */
        return inode;
    }

    /**
     * TODO(docs): write javadoc
     */
    void seek(long pos) {
        try {
            fsFile.seek(pos);
        } catch (IOException e) {
            System.err.println("An error occurred while seeking the disk.");
            e.printStackTrace();
        }
    }

    /**
     * TODO(docs): write javadoc
     */
    void read(byte[] dst) {
        read(dst, 0, dst.length);
    }

    /**
     * TODO(docs): write javadoc
     */
    void read(byte[] dst, int offset, int length) {
        try {
            fsFile.read(dst, offset, length);
        } catch (IOException e) {
            System.err.println("An error occurred while reading from the disk.");
            e.printStackTrace();
        }
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

    @Override
    public void close() throws IOException {
        fsFile.close();
    }

    @Override
    public String toString() {
        return "Volume{" +
                "fsFile=" + fsFile +
                ", superblock=" + superblock +
                ", blockGroupDescriptors=" + Arrays.toString(blockGroupDescriptors) +
                '}';
    }
}
