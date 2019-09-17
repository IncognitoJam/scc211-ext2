package com.gitlab.incognitojam.ext2;

import java.nio.ByteBuffer;

/**
 * The Block Group Descriptor contains information regarding where important
 * data structures for that block group are located.
 */
public class BlockGroupDescriptor {
    private final int blockBitmapPtr;
    private final int inodeBitmapPtr;
    private final int inodeTablePtr;
    private final short freeBlockCount;
    private final short freeInodeCount;
    private final short directoriesCount;
    private final short unused;

    BlockGroupDescriptor(ByteBuffer buffer) {
        // Read the data from the buffer.
        blockBitmapPtr = buffer.getInt(); // 0
        inodeBitmapPtr = buffer.getInt(); // 4
        inodeTablePtr = buffer.getInt(); // 8
        freeBlockCount = buffer.getShort(); // 12
        freeInodeCount = buffer.getShort(); // 14
        directoriesCount = buffer.getShort(); // 16
        unused = buffer.getShort(); // 18
    }

    /**
     * The block index of the block usage bitmap.
     */
    public int getBlockBitmapPtr() {
        return blockBitmapPtr;
    }

    /**
     * The block index of the inode usage bitmap.
     */
    public int getInodeBitmapPtr() {
        return inodeBitmapPtr;
    }

    /**
     * The block index of the inode table.
     */
    public int getInodeTablePtr() {
        return inodeTablePtr;
    }

    /**
     * The number of unallocated blocks in this block group.
     */
    public short getFreeBlockCount() {
        return freeBlockCount;
    }

    /**
     * The number of unallocated inodes in this block group.
     */
    public short getFreeInodeCount() {
        return freeInodeCount;
    }

    /**
     * The number of directories in this block group.
     */
    public short getDirectoriesCount() {
        return directoriesCount;
    }
}
