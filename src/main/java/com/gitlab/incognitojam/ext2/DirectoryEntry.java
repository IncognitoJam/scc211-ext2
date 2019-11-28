package com.gitlab.incognitojam.ext2;

import java.nio.ByteBuffer;

/**
 * TODO(docs): write javadoc
 */
public class DirectoryEntry {
    private final int inode;
    private final short length;
    private final byte fileType;
    private final String label;

    /**
     * TODO(docs): write javadoc
     */
    DirectoryEntry(ByteBuffer buffer) {
        inode = buffer.getInt();
        length = buffer.getShort();

        byte labelLength = buffer.get();
        fileType = buffer.get();

        final byte[] labelBytes = new byte[labelLength];
        buffer.get(labelBytes, 0, labelLength);
        label = new String(labelBytes);
    }

    /**
     * The Inode storing the information related to this directory entry.
     *
     * @return returns the inode number
     */
    public int getInode() {
        return inode;
    }

    /**
     * Retrieve the length of this directory entry.
     * <p>
     * Can be added to the pointer for the start of this directory entry to get
     * a valid pointer for the next directory entry.
     *
     * @return returns the directory entry length in bytes
     */
    public short getLength() {
        return length;
    }

    /**
     * TODO: write explanation
     *
     * @return returns the file type identifier
     */
    public byte getFileType() {
        return fileType;
    }

    /**
     * The name of this directory entry.
     *
     * @return returns the name of this directory entry
     */
    public String getLabel() {
        return label;
    }
}
