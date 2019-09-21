package com.gitlab.incognitojam.ext2;

import java.nio.ByteBuffer;

/**
 * Inodes hold information about the files and directories held within the
 * filesystem. They identify who should have access to the data, timestamps
 * giving update information, and links to all the data blocks that form each
 * file.
 */
public class Inode {
    /**
     * The contents of the file mode field will be some combination of the following
     * values, i.e. the values ORed together. These bits are used to identify the
     * type of file references by the inode and file permissions, the rwxr-xr-x seen
     * in the Unix directory listings.
     */
    public static class FileModes {
        /**
         * Socket
         */
        public static final int IFSCK = 0xC000;
        /**
         * Symbolic Link
         */
        public static final int IFLNK = 0xA000;
        /**
         * Regular File
         */
        public static final int IFREG = 0x8000;
        /**
         * Block Device
         */
        public static final int IFBLK = 0x6000;
        /**
         * Directory
         */
        public static final int IFDIR = 0x4000;
        /**
         * Character Device
         */
        public static final int IFCHR = 0x2000;
        /**
         * FIFO
         */
        public static final int IFIFO = 0x1000;

        /**
         * Set process User ID
         */
        public static final int ISUID = 0x0800;
        /**
         * Set process Group ID
         */
        public static final int ISGID = 0x0400;
        /**
         * Sticky bit
         */
        public static final int ISVTX = 0x0200;

        /**
         * User read
         */
        public static final int IRUSR = 0x0100;
        /**
         * User write
         */
        public static final int IWUSR = 0x0080;
        /**
         * User execute
         */
        public static final int IXUSR = 0x0040;

        /**
         * Group read
         */
        public static final int IRGRP = 0x0020;
        /**
         * Group write
         */
        public static final int IWGRP = 0x0010;
        /**
         * Group execute
         */
        public static final int IXGRP = 0x0008;

        /**
         * Others read
         */
        public static final int IROTH = 0x0004;
        /**
         * Others write
         */
        public static final int IWOTH = 0x0002;
        /**
         * Others execute
         */
        public static final int IXOTH = 0x0001;

        /**
         * Convert a filemode value to it's string representation in unix.
         * 
         * @param filemode the filemode value to parse
         * @return returns ASCII representation of the filemode value
         * @see <a href="https://github.com/coreutils/gnulib/blob/master/lib/filemode.c#L96}">strmode in glibc.</a>
         */
        public static String toString(final int filemode) {
            final char[] str = new char[10];

            // file type
            str[0] = parseFileType(filemode);

            // user permissions
            str[1] = (filemode & IRUSR) == IRUSR ? 'r' : '-';
            str[2] = (filemode & IWUSR) == IWUSR ? 'w' : '-';
            str[3] = (filemode & IXUSR) == IXUSR ? 'x' : '-';

            // group permissions
            str[4] = (filemode & IRGRP) == IRGRP ? 'r' : '-';
            str[5] = (filemode & IWGRP) == IWGRP ? 'w' : '-';
            str[6] = (filemode & IXGRP) == IXGRP ? 'x' : '-';

            // others permissions
            str[7] = (filemode & IROTH) == IROTH ? 'r' : '-';
            str[8] = (filemode & IWOTH) == IWOTH ? 'w' : '-';
            str[9] = (filemode & IXOTH) == IXOTH ? 'x' : '-';

            return new String(str);
        }

        /**
         * Get a character indicating the type of file descriped by the
         * filemode bits.
         * 
         * @param filemode the filemode to parse
         * @return returns the character matching the file type
         */
        private static char parseFileType(final int filemode) {
            if (testBitmask(filemode, IFREG))
                return '-';
            if (testBitmask(filemode, IFDIR))
                return 'd';
            if (testBitmask(filemode, IFBLK))
                return 'b';
            if (testBitmask(filemode, IFCHR))
                return 'c';
            if (testBitmask(filemode, IFLNK))
                return 'l';
            if (testBitmask(filemode, IFIFO))
                return 'p';
            if (testBitmask(filemode, IFSCK))
                return 's';

            // none of the tests matched, we don't know what the type is
            return '?';
        }

        private static boolean testBitmask(final int bits, final int mask) {
            return (bits & mask) == mask;
        }
    }

    /**
     * The size of an Inode in bytes.
     */
    public static final int STRUCT_SIZE = 128;

    private final short fileMode;
    private final short userId;
    private final int fileSizeLower;
    private final int lastAccessTime;
    private final int creationTime;
    private final int lastModifiedTime;
    private final int deletedTime;
    private final short groupId;
    private final short hardLinksCount;
    private final int[] directPtrs;
    private final int indirectPtr;
    private final int doubleIndirectPtr;
    private final int tripleIndirectPtr;
    private final int fileSizeUpper;

    /**
     * Construct an Inode by reading data from bytes.
     * 
     * @param buffer the buffer to read data from
     */
    Inode(ByteBuffer buffer) {
        // Record the starting buffer position so that we can skip forward later.
        final int pos = buffer.position();

        fileMode = buffer.getShort();
        userId = buffer.getShort();
        fileSizeLower = buffer.getInt();
        lastAccessTime = buffer.getInt();
        creationTime = buffer.getInt();
        lastModifiedTime = buffer.getInt();
        deletedTime = buffer.getInt();
        groupId = buffer.getShort();
        hardLinksCount = buffer.getShort();

        // skip irrelevant fields
        buffer.position(pos + 28);

        // Read block pointers.
        directPtrs = new int[12];
        for (int i = 0; i < 12; i++)
            directPtrs[i] = buffer.getInt(); // 1-12

        indirectPtr = buffer.getInt(); // 13
        doubleIndirectPtr = buffer.getInt(); // 14
        tripleIndirectPtr = buffer.getInt(); // 15

        // skip irrelevant fields
        buffer.position(pos + 88);

        fileSizeUpper = buffer.getInt();

        // skip irrelevant fields
        buffer.position(pos + 128);
    }

    /**
     * Determines the file type and how the file's owner, it's group and others can
     * access the file.
     * 
     * See {@link FileModes} for values.
     */
    public short getFileMode() {
        return fileMode;
    }

    /**
     * Records the user ID of the owner of the file.
     */
    public short getUserId() {
        return userId;
    }

    /**
     * The lower bytes of the file size field.
     * 
     * @see Inode#getFileSizeUpper()
     * @see Inode#getFileSize()
     */
    public int getFileSizeLower() {
        return fileSizeLower;
    }

    /**
     * The last time this inode was accessed.
     */
    public int getLastAccessTime() {
        return lastAccessTime;
    }

    /**
     * The time when this inode was created.
     */
    public int getCreationTime() {
        return creationTime;
    }

    /**
     * The last time this inode was modified.
     */
    public int getLastModifiedTime() {
        return lastModifiedTime;
    }

    /**
     * The time this inode was deleted.
     */
    public int getDeletedTime() {
        return deletedTime;
    }

    /**
     * Records the group ID of the owner of the file.
     */
    public short getGroupId() {
        return groupId;
    }

    /**
     * Count of how many times this particular node is linked to.
     * <p>
     * Most files have a link count of one. Files with hard links pointing to
     * them will hae an additional count for each hard link.
     * <p>
     * When the link count reaches zero, the inode and all of its associated
     * blocks are freed.
     */
    public short getHardLinksCount() {
        return hardLinksCount;
    }

    /**
     * The indices pointing to the blocks containing the data for this inode.
     */
    public int[] getDirectPtrs() {
        return directPtrs;
    }

    /**
     * The block number of the first indirect block, which is a block
     * containing an array of block indices containing the data for this inode.
     * <p>
     * The 13th block of this file will be the first block index contained in
     * this indirect block, since blocks 1-12 are referenced directly.
     * 
     * @see Inode#getDirectPtrs()
     */
    public int getIndirectPtr() {
        return indirectPtr;
    }

    /**
     * The block number of the first doubly-indirect block, which is a block
     * containing an array of indirect block indices, with each of those
     * indirect blocks containing an array of blocks indices pointing to the
     * data.
     * 
     * @see Inode#getIndirectPtr()
     */
    public int getDoubleIndirectPtr() {
        return doubleIndirectPtr;
    }

    /**
     * The block number of the triply-indirect block, which is a block
     * containing an array of double-indirect block indices, with each of those
     * doubly-indirect blocks containing an array of indirect block indices,
     * and each of those indirect blocks containing an array of block indices
     * pointing to the data.
     * 
     * @see Inode#getDoubleIndirectPtr()
     */
    public int getTripleIndirectPtr() {
        return tripleIndirectPtr;
    }

    /**
     * The upper bytes of the file size field.
     * 
     * @see Inode#getFileSizeLower()
     * @see Inode#getFileSize()
     */
    public int getFileSizeUpper() {
        return fileSizeUpper;
    }

    /**
     * The size of this file (if it is a regular file, or a symbolic link) in
     * bytes.
     */
    public long getFileSize() {
        return ((long)getFileSizeUpper() << 32) | (getFileSizeLower() & 0xFFFFFFFFL);
    }
}
