package com.gitlab.incognitojam.ext2;

import java.nio.ByteBuffer;

/**
 * TODO(docs): write javadoc
 */
public class DirectoryEntry {
    /**
     * TODO(docs): write javadoc
     * @see <a href="https://wiki.osdev.org/Ext2#Directory_Entry_Type_Indicators"/>
     */
    public static class FileTypes {
        public static final short UNKNOWN = 0;
        public static final short REGULAR_FILE = 1;
        public static final short DIRECTORY = 2;
        public static final short CHARACTER_DEVICE = 3;
        public static final short BLOCK_DEVICE = 4;
        public static final short FIFO = 5;
        public static final short SOCKET = 6;
        public static final short SYMBOLIC_LINK = 7;
    }

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
        buffer.get(labelBytes);
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
     * @see DirectoryEntry.FileTypes
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

    @Override
    public String toString() {
        return "DirectoryEntry{" +
                "inode=" + inode +
                ", length=" + length +
                ", fileType=" + fileType +
                ", label='" + label + '\'' +
                '}';
    }
}
