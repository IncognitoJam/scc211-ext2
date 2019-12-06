package com.gitlab.incognitojam.ext2;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
         * @see <a href="https://github.com/coreutils/gnulib/blob/master/lib/filemode.c#L96">strmode in glibc.</a>
         */
        public static String toString(final int filemode) {
            final char[] str = new char[10];

            // file type
            str[0] = parseFileType(filemode);

            // user permissions
            str[1] = testBitmask(filemode, IRUSR) ? 'r' : '-';
            str[2] = testBitmask(filemode, IWUSR) ? 'w' : '-';
            str[3] = testBitmask(filemode, IXUSR) ? 'x' : '-';

            // group permissions
            str[4] = testBitmask(filemode, IRGRP) ? 'r' : '-';
            str[5] = testBitmask(filemode, IWGRP) ? 'w' : '-';
            str[6] = testBitmask(filemode, IXGRP) ? 'x' : '-';

            // others permissions
            str[7] = testBitmask(filemode, IROTH) ? 'r' : '-';
            str[8] = testBitmask(filemode, IWOTH) ? 'w' : '-';
            str[9] = testBitmask(filemode, IXOTH) ? 'x' : '-';

            return new String(str);
        }

        /**
         * Get a character indicating the type of file described by the
         * filemode bits.
         *
         * @param filemode the filemode to parse
         * @return returns the character matching the file type
         * @see #parseFileTypeHumanReadable(int) for human readable format
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

        /**
         * Get a string indicating the type of file described by the
         * filemode bits.
         *
         * @param filemode the filemode to parse
         * @return returns a string describing the file type
         * @see #parseFileType(int) for non human readable format
         */
        public static String parseFileTypeHumanReadable(final int filemode) {
            if (testBitmask(filemode, IFREG))
                return "Regular file";
            if (testBitmask(filemode, IFDIR))
                return "Directory";
            if (testBitmask(filemode, IFBLK))
                return "Block device";
            if (testBitmask(filemode, IFCHR))
                return "Character device";
            if (testBitmask(filemode, IFLNK))
                return "Symbolic link";
            if (testBitmask(filemode, IFIFO))
                return "FIFO";
            if (testBitmask(filemode, IFSCK))
                return "Unix socket";

            // none of the tests matched, we don't know what the type is
            return "Unknown";
        }

        public static boolean testBitmask(final int bits, final int mask) {
            return (bits & mask) == mask;
        }
    }

    private final Volume volume;
    private final short fileMode;
    private final short userId;
    private final long lastAccessTime;
    private final long creationTime;
    private final long lastModifiedTime;
    private final long deletedTime;
    private final short groupId;
    private final short hardLinksCount;
    private final int[] directPtrs;
    private final int indirectPtr;
    private final int doubleIndirectPtr;
    private final int tripleIndirectPtr;
    private final long fileSize;

    /**
     * Construct an Inode by reading data from bytes.
     *
     * @param bytes the bytes to read attributes from
     */
    Inode(Volume volume, byte[] bytes) {
        this.volume = volume;
        ByteBuffer buffer = ByteUtils.wrap(bytes);

        fileMode = buffer.getShort(0);
        userId = buffer.getShort(2);
        final int fileSizeLower = buffer.getInt(4);
        lastAccessTime = buffer.getInt(8);
        creationTime = buffer.getInt(12);
        lastModifiedTime = buffer.getInt(16);
        deletedTime = buffer.getInt(20);
        groupId = buffer.getShort(24);
        hardLinksCount = buffer.getShort(26);

        // Read block pointers.
        directPtrs = new int[12];
        for (int i = 0; i < 12; i++)
            directPtrs[i] = buffer.getInt(40 + i * 4);

        indirectPtr = buffer.getInt(88);
        doubleIndirectPtr = buffer.getInt(92);
        tripleIndirectPtr = buffer.getInt(96);

        final int fileSizeUpper = buffer.getInt(108);
        fileSize = ((long) fileSizeUpper << 32) + fileSizeLower;
    }

    /**
     * Read a length of bytes from the Inode data blocks at a particular offset.
     *
     * @param startOffset The offset in the inode data to start reading from.
     * @param length      The number of bytes of data to read.
     * @return Returns a new byte array containing the requested bytes.
     */
    byte[] read(long startOffset, int length) {
        /*
         * Since it is not possible to read beyond the end of the file,
         * transform the request to reduce the length of bytes read.
         */
        if (startOffset + length > getFileSize())
            length = (int) (getFileSize() - startOffset);

        /*
         * Calculate the local block number and starting byte for the data we
         * we want to read inside this file.
         *
         * For example, to read bytes [0, 32) we need local block 0 starting at
         * local byte 0. To read bytes [1500, 1600) we need local block 1
         * starting at local byte 476.
         */
        // first inode data block to read from
        final int logicalBlockStartNumber = (int) (startOffset / volume.getBlockSize());

        // starting position in first block
        final int logicalBlockStartOffset = (int) (startOffset % volume.getBlockSize());

        byte[] dst = new byte[length];
        int bytesRead = 0;
        int logicalBlockNumber = logicalBlockStartNumber;
        while (bytesRead < length) {
            /*
             * The offset to read from in this block should be "local block
             * start offset" if it's the first block we are reading, otherwise
             * read from the start of the block.
             */
            final int localOffset = bytesRead == 0 ? logicalBlockStartOffset : 0;

            /*
             * The length of data to read is either from the local offset to
             * the end of the block, or the total number of bytes read so far
             * minus the total length to read, whichever is smaller.
             */
            final int localLength = Math.min(volume.getBlockSize() - localOffset, length - bytesRead);

            final int dataBlock = traverseDataPtrs(logicalBlockNumber);
            if (dataBlock == 0) {
                /*
                 * We are attempting to read a hole in the file, so fill with
                 * zero bytes.
                 */
                Arrays.fill(dst, bytesRead, bytesRead + localLength, (byte) 0);
            } else {
                volume.seek(dataBlock * volume.getBlockSize() + localOffset);
                volume.read(dst, bytesRead, localLength);
            }

            /*
             * Count up the total number of bytes read and increment the local
             * block pointer.
             */
            bytesRead += localLength;
            logicalBlockNumber++;
        }

        return dst;
    }

    /**
     * Read the inode data and parse it as a directory structure, returning
     * a list of {@link DirectoryEntry}s.
     *
     * @return Returns a list of {@link DirectoryEntry} objects.
     * @throws UnsupportedOperationException Throws an exception if this Inode
     *                                       does not have the {@link FileModes#IFDIR} bit set.
     */
    List<DirectoryEntry> getEntries() {
        if (!FileModes.testBitmask(getFileMode(), FileModes.IFDIR))
            throw new UnsupportedOperationException("Must only call Inode#getEntries() on directories.");

        // Read this inode's data blocks.
        final int size = (int) getFileSize();
        byte[] dataBytes = read(0, size);
        ByteBuffer buffer = ByteUtils.wrap(dataBytes);

        // Parse the data as a Directory structure.
        List<DirectoryEntry> entries = new ArrayList<>();
        int ptr = 0;
        while (ptr < size) {
            buffer.position(ptr);

            // Construct a DirectoryEntry using the data at this position.
            DirectoryEntry entry = new DirectoryEntry(buffer);

            // Skip entries which don't point to a valid inode
            if (entry.getInode() > 0)
                entries.add(entry);

            // Move the pointer forward by the previous entry's length.
            ptr += entry.getLength();
        }

        return entries;
    }

    /**
     * Read an integer from the volume at a particular offset.
     * <p>
     * This is a helper method to read four bytes from the volume at an offset
     * and construct a new Integer. It is particularly useful when reading
     * block data pointers with multiple levels of indirection.
     *
     * @param offset The offset on disk, in bytes, to read the integer value
     *               from.
     * @return Returns the integer stored at this offset.
     */
    private int readPtr(long offset) {
        byte[] ptrData = new byte[4];

        // Seek to the given offset and read the four bytes from disk.
        volume.seek(offset);
        volume.read(ptrData);

        // Construct a ByteBuffer and parse the integer.
        return ByteUtils.wrap(ptrData).getInt();
    }

    /**
     * Retrieve a data block pointer for this inode from its logical index.
     * <p>
     * The block pointer to data block 0 can be retrieved from the {@link Inode#directPtrs}
     * array at index 0, while data block 11 is found in the {@link Inode#directPtrs}
     * array at index 11. Higher indices are referenced indirectly, such as
     * data block 12 which is located in the first indirect data block at
     * position 0.
     * <p>
     * With each added level of indirection, more data blocks must be read
     * at a particular offset to retrieve the real data block pointer. This
     * method is used to perform this traversal of the tree.
     * <p>
     * This method supports up to three levels of indirection, or triple
     * indirect pointers.
     *
     * @param logicalBlockNumber the data block to read in this inode, in the
     *                           range <pre>0 <= logicalBlockNumber < 16843020</pre>
     *                           for filesystems with 1K block size.
     * @return Returns the real pointer to the data block on disk.
     */
    private int traverseDataPtrs(int logicalBlockNumber) {
        /*
         * If the requested local block number is less than 12, this means the
         * data is held in one of the first direct pointers.
         */
        if (logicalBlockNumber < 12)
            return directPtrs[logicalBlockNumber];
        logicalBlockNumber -= 12;

        /*
         * If the logicalBlockNumber is in 0 <= x < 256, then the data we need is
         * in a block referenced by the indirect block data.
         *
         * Navigate to indirectPtr + logicalBlockNumber * sizeof(int) and read
         * the int to get the ptr to the data block.
         */
        final int ptrsPerBlock = volume.getBlockSize() / 4;
        if (logicalBlockNumber < ptrsPerBlock) {
            /*
             * If this inode does not have an indirectPtr, then the requested
             * logical block is a hole.
             */
            if (indirectPtr == 0) return 0;

            return readPtr(indirectPtr * volume.getBlockSize() + logicalBlockNumber * 4);
        }
        logicalBlockNumber -= ptrsPerBlock;

        /*
         * If the logicalBlockNumber is in 0 <= x < 65536, then the data we need
         * is in a block referenced by the double indirect block data.
         *
         * Navigate to doubleIndirectPtr + (logicalBlockNumber / 256) * sizeof(int)
         * and read the int to get the ptr to the indirect data block.
         *
         * Now navigate to indirectPtr + (logicalBlockNumber % 256) * sizeof(int)
         * and read the int to get the ptr to the data block.
         */
        if (logicalBlockNumber < ptrsPerBlock * ptrsPerBlock) {
            /*
             * If this inode does not have a doubleIndirectPtr, then the
             * requested logical block is a hole.
             */
            if (doubleIndirectPtr == 0) return 0;

            int indirectPtr = readPtr(doubleIndirectPtr * volume.getBlockSize() + (logicalBlockNumber / 256) * 4);

            /*
             * If the indirectPtr is zero, then the requested logical block is
             * a hole.
             */
            if (indirectPtr == 0) return 0;

            return readPtr(indirectPtr * volume.getBlockSize() + (logicalBlockNumber % 256) * 4);
        }
        logicalBlockNumber -= ptrsPerBlock * ptrsPerBlock;

        /*
         * If the logicalBlockNumber is in 0 <= x < 16777216, then the data we
         * need is in a block referenced by the triple indirect block data.
         *
         * Navigate to tripleIndirectPtr + (logicalBlockNumber / 65536) * sizeof(int)
         * and read the int to get the ptr to the double indirect data block.
         *
         * Now navigate to doubleIndirectPtr + ((logicalBlockNumber % 65536) / 256) * sizeof(int)
         * and read the int to get the ptr to the indirect data block.
         *
         * Now navigate to indirectPtr + (logicalBlockNumber % 256) * sizeof(int) and read
         * the int to get the ptr to the data block.
         */
        if (logicalBlockNumber < ptrsPerBlock * ptrsPerBlock * ptrsPerBlock) {
            /*
             * If the inode does not have a tripleIndirectPtr, then the
             * requested logical block is a hole.
             */
            if (tripleIndirectPtr == 0) return 0;

            int doubleIndirectPtr = readPtr(tripleIndirectPtr * volume.getBlockSize() + (logicalBlockNumber / 65536) * 4);
            /*
             * If the doubleIndirectPtr is zero, then the requested logical
             * block is a hole.
             */
            if (doubleIndirectPtr == 0) return 0;

            int indirectPtr = readPtr(doubleIndirectPtr * volume.getBlockSize() + ((logicalBlockNumber % 65536) / 256) * 4);
            /*
             * If the indirectPtr is zero, then the requested logical block is
             * a hole.
             */
            if (indirectPtr == 0) return 0;

            return readPtr(indirectPtr * volume.getBlockSize() + (logicalBlockNumber % 256) * 4);
        }

        // if we get here, something has gone wrong
        throw new IllegalStateException("invalid inode data ptr");
    }

    /**
     * Determines the file type and how the file's owner, it's group and others can
     * access the file.
     * <p>
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
     * The last time this inode was accessed.
     */
    public long getLastAccessTime() {
        return lastAccessTime;
    }

    /**
     * The time when this inode was created.
     */
    public long getCreationTime() {
        return creationTime;
    }

    /**
     * The last time this inode was modified.
     */
    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    /**
     * The time this inode was deleted.
     */
    public long getDeletedTime() {
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
     * The size of this file (if it is a regular file, or a symbolic link) in
     * bytes.
     */
    public long getFileSize() {
        return fileSize;
    }

    @Override
    public String toString() {
        return "Inode{" +
                "volume=" + volume +
                ", fileMode=" + fileMode +
                ", userId=" + userId +
                ", lastAccessTime=" + lastAccessTime +
                ", creationTime=" + creationTime +
                ", lastModifiedTime=" + lastModifiedTime +
                ", deletedTime=" + deletedTime +
                ", groupId=" + groupId +
                ", hardLinksCount=" + hardLinksCount +
                ", directPtrs=" + Arrays.toString(directPtrs) +
                ", indirectPtr=" + indirectPtr +
                ", doubleIndirectPtr=" + doubleIndirectPtr +
                ", tripleIndirectPtr=" + tripleIndirectPtr +
                ", fileSize=" + fileSize +
                '}';
    }
}
