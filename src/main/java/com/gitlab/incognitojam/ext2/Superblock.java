package com.gitlab.incognitojam.ext2;

import java.nio.ByteBuffer;

/**
 * The superblock is a table that holds information about the volume structure,
 * such as the number of blocks, the block size and the volume label.
 */
public class Superblock {
    private final int inodesCount;
    private final int blocksCount;
    private final int fsBlockSize;
    private final int blocksInGroup;
    private final int inodesInGroup;
    private final short magic;
    private final int inodeSize;
    private final String volumeLabel;

    /**
     * Read new superblock table from a byte buffer.
     *
     * @param bytes the byte array to read the table from
     */
    Superblock(byte[] bytes) {
        ByteBuffer buffer = ByteUtils.wrap(bytes);
        inodesCount = buffer.getInt(0);
        blocksCount = buffer.getInt(4);

        // NOTE: filesystem image is corrupt, block size is zero
        // Assume block size is 1024
        int fsBlockSize = buffer.getInt(24);
        this.fsBlockSize = fsBlockSize == 0 ? 1024 : fsBlockSize;

        blocksInGroup = buffer.getInt(32);
        inodesInGroup = buffer.getInt(40);
        magic = buffer.getShort(56);
        inodeSize = buffer.getInt(88);

        byte[] labelBytes = new byte[16];
        buffer.position(120);
        buffer.get(labelBytes);
        volumeLabel = new String(labelBytes);
    }

    /**
     * The total number of inodes in the filesystem.
     */
    public int getInodesCount() {
        return inodesCount;
    }

    /**
     * The total number of blocks in the filesystem.
     */
    public int getBlocksCount() {
        return blocksCount;
    }

    /**
     * The size of each filesystem block, in bytes.
     */
    public int getFsBlockSize() {
        return fsBlockSize;
    }

    /**
     * The number of blocks in each block group.
     */
    public int getBlocksInGroup() {
        return blocksInGroup;
    }

    /**
     * The number of inodes in each block group.
     */
    public int getInodesInGroup() {
        return inodesInGroup;
    }

    /**
     * The filesystem 'magic number'.
     * <p>
     * This is always 0xEF53 in an ext2 filesystem.
     */
    public short getMagic() {
        return magic;
    }

    /**
     * The size of each inode, in bytes.
     */
    public int getInodeSize() {
        return inodeSize;
    }

    /**
     * The volume label.
     */
    public String getVolumeLabel() {
        return volumeLabel;
    }

    @Override
    public String toString() {
        return "Superblock{" +
                "inodesCount=" + inodesCount +
                ", blocksCount=" + blocksCount +
                ", fsBlockSize=" + fsBlockSize +
                ", blocksInGroup=" + blocksInGroup +
                ", inodesInGroup=" + inodesInGroup +
                ", magic=" + magic +
                ", inodeSize=" + inodeSize +
                ", volumeLabel='" + volumeLabel + '\'' +
                '}';
    }
}
