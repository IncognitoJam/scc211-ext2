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
    private final int[] dataPtrs;
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

        dataPtrs = new int[12];
        for (int i = 0; i < 12; i++)
            dataPtrs[i] = buffer.getInt();

        indirectPtr = buffer.getInt();
        doubleIndirectPtr = buffer.getInt();
        tripleIndirectPtr = buffer.getInt();

        // skip irrelevant fields
        buffer.position(pos + 88);

        fileSizeUpper = buffer.getInt();
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

    public short getUserId() {
        return userId;
    }

    public int getFileSizeLower() {
        return fileSizeLower;
    }

    public int getLastAccessTime() {
        return lastAccessTime;
    }

    public int getCreationTime() {
        return creationTime;
    }

    public int getLastModifiedTime() {
        return lastModifiedTime;
    }

    public int getDeletedTime() {
        return deletedTime;
    }

    public short getGroupId() {
        return groupId;
    }

    public short getHardLinksCount() {
        return hardLinksCount;
    }

    public int[] getDataPtrs() {
        return dataPtrs;
    }

    public int getIndirectPtr() {
        return indirectPtr;
    }

    public int getDoubleIndirectPtr() {
        return doubleIndirectPtr;
    }

    public int getTripleIndirectPtr() {
        return tripleIndirectPtr;
    }

    public int getFileSizeUpper() {
        return fileSizeUpper;
    }
}
