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
        fsFile = new RandomAccessFile(filepath, "r");
        superblock = readSuperblock();
        blockGroupDescriptors = readBlockGroupDescriptorTable();
    }

    /**
     * Read the second block on disk and construct a new {@link Superblock}.
     *
     * @see Superblock
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
     * Read the third block on disk and construct the {@link BlockGroupDescriptor}
     * table.
     *
     * @see BlockGroupDescriptor
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
            seek(bgdTableBlockPtr + i * bgdLength);
            read(bgdBytes);
            bgdTable[i] = new BlockGroupDescriptor(bgdBytes);
        }

        return bgdTable;
    }

    /**
     * Locates and reads an Inode on disk from its number.
     * <p>
     * Calculates the block group containing the inode, finds it's inode table
     * position and reads the required bytes to construct an {@link Inode}
     * object.
     *
     * @param inodeNumber The inode number to access.
     * @return Returns the newly constructed Inode retrieved from disk.
     * @see Inode
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
     * Navigate the directory structure on disk to find the Inode represented
     * by a given file path.
     */
    Inode navigate(String filePath) {
        /*
         * Split the given filepath into "parts" where each part is a file or
         * directory label in the tree. We follow the tree to find the Inode
         * at the file path.
         */
        final String[] parts = filePath.split(Ext2File.pathSeparator);

        Inode inode = getInode(2);

        // If the file path is empty then return the root inode.
        if (filePath.length() == 0)
            return inode;

        /*
         * Iterate over the parts of the file path, retrieving the directory
         * entries for the particular node if it is a directory.
         *
         * If a part of the file path is not a directory, or the file does not
         * exist at the end of the file path, return null.
         */
        List<DirectoryEntry> entries = inode.getEntries();
        for (int i = 0; i < parts.length; i++) {
            final String part = parts[i];

            /*
             * If this "part" of the file path is empty, for example if there
             * were two forward-slashes immediately following one another,
             * skip this part.
             */
            if (part.length() == 0)
                continue;

            /*
             * Iterate over the directory entries looking for the entry with
             * same label as this part of the file path.
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
                 * If this isn't the end of the filePath and the inode isn't a
                 * directory, return null as the file can't exist.
                 */
                return null;
            else if (i < parts.length - 1)
                entries = inode.getEntries();
        }

        /*
         * If we reach this point, it means we found the Inode for the given
         * file path.
         */
        return inode;
    }

    /**
     * Move the file-pointer offset to the given position.
     * <p>
     * This method is used to alter where future read operations take place.
     *
     * @param position The position to move the file-pointer offset to, in bytes.
     */
    void seek(long position) {
        try {
            fsFile.seek(position);
        } catch (IOException e) {
            System.err.println("An error occurred while seeking the disk.");
            e.printStackTrace();
        }
    }

    /**
     * Read a range of bytes from disk into the given array.
     * <p>
     * This operation begins reading from the disk at the location set by {@link #seek(long)}
     * and reads <pre>dst.length</pre> bytes.
     *
     * @param dst The array to read the bytes into.
     * @see #seek(long)
     */
    void read(byte[] dst) {
        read(dst, 0, dst.length);
    }

    /**
     * Read a requested length of bytes from disk into the given array, at a
     * particular offset.
     * <p>
     * This operation begins reading from the disk at the location set by {@link #seek(long)}
     * and reads <pre>length</pre> bytes into the array starting at <pre>offset</pre>.
     *
     * @param dst    The array to read the bytes into.
     * @param offset The position in the destination array to start writing at.
     * @param length The number of bytes to read into the destination array.
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
     * The Ext2 signature (0xEF53) used to confirm the presence of an Ext2
     * filesystem on a volume.
     *
     * @see Superblock#getMagic()
     */
    public short getMagicValue() {
        return superblock.getMagic();
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
