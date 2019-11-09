package com.gitlab.incognitojam.ext2;

import java.nio.ByteBuffer;

class DirectoryEntry {
    private final int inode;
    private final short length;
    private final short labelLength;
    private final short fileMode;
    private final String label;

    DirectoryEntry(ByteBuffer buffer) {
        inode = buffer.getInt();
        length = buffer.getShort();

        final short data = buffer.getShort();
        labelLength = (short) (data & 0xF);
        fileMode = (short) (data << 8);

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
     * The length of the directory entry label.
     *
     * @return returns the length of the directory entry label, in bytes
     */
    public short getLabelLength() {
        return labelLength;
    }

    /**
     * Determines the file type and how the file's owner, it's group and others can
     * access the file.
     * <p>
     * See {@link Inode.FileModes} for values.
     *
     * @return returns the file mode bits
     */
    public short getFileMode() {
        return fileMode;
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
