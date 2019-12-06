package com.gitlab.incognitojam.ext2;

import java.nio.ByteBuffer;

/**
 * A {@link DirectoryEntry} is a structure which describes a particular inode
 * as an entry of a Directory.
 * <p>
 * A Directory is simply a normal {@link Inode} except the inode data is built
 * from a structure containing solely {@link DirectoryEntry}s which immediately
 * follow one another.
 */
public class DirectoryEntry {
    /**
     * {@link DirectoryEntry}s contain a {@link DirectoryEntry#fileType}
     * attribute which indicates the type of file present at the respective
     * inode.
     *
     * @see DirectoryEntry#getFileType()
     * @see <a href="https://wiki.osdev.org/Ext2#Directory_Entry_Type_Indicators"></a>
     */
    public static class FileTypes {
        /**
         * Unknown file type.
         */
        public static final short UNKNOWN = 0;
        /**
         * Regular file.
         */
        public static final short REGULAR_FILE = 1;
        /**
         * Directory.
         */
        public static final short DIRECTORY = 2;
        /**
         * Character device.
         */
        public static final short CHARACTER_DEVICE = 3;
        /**
         * Block device.
         */
        public static final short BLOCK_DEVICE = 4;
        /**
         * FIFO.
         */
        public static final short FIFO = 5;
        /**
         * Socket.
         */
        public static final short SOCKET = 6;
        /**
         * Symbolic link.
         */
        public static final short SYMBOLIC_LINK = 7;
    }

    private final int inode;
    private final short length;
    private final byte fileType;
    private final String label;

    /**
     * Construct a new {@link DirectoryEntry} from an array of bytes.
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
     * {@link DirectoryEntry}s contain a {@link DirectoryEntry#fileType}
     * attribute which indicates the type of file present at the respective
     * inode.
     *
     * @return Returns the file type identifier.
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
