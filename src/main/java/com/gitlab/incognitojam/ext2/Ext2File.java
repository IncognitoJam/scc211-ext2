package com.gitlab.incognitojam.ext2;

import com.gitlab.incognitojam.ext2.Inode.FileModes;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

/**
 * Ext2File represents a file within the Ext2 filesystem and holds a reference
 * to the relevant {@link Inode} object within a given {@link Volume}.
 */
public class Ext2File {
    public static final String pathSeparator = "/";

    private final Volume volume;
    private final String filePath;
    private final Inode inode;
    private long position;

    /**
     * Construct a new Ext2File within the filesystem using an absolute path.
     *
     * @param volume   The volume in which this file exists.
     * @param filePath The path to access this resource from within the
     *                 filesystem. Supports relative path modifiers.
     */
    public Ext2File(Volume volume, String filePath) throws FileNotFoundException {
        this.volume = volume;
        this.filePath = Path.of(filePath).normalize().toString();
        this.inode = volume.navigate(filePath);
        if (inode == null)
            throw new FileNotFoundException("Could not find file " + filePath);
        position = 0L;
    }

    /**
     * Construct a new Ext2File within the filesystem using a relative path,
     * having provided a reference to the parent directory and a child path.
     *
     * @param parent The parent directory to find the file in.
     * @param child  The child file to access.
     */
    public Ext2File(Ext2File parent, String child) throws FileNotFoundException {
        this(parent.volume, Path.of(parent.filePath, child).normalize().toString());
    }

    /**
     * @return Returns whether or not this Ext2File represents a directory.
     */
    public boolean isDirectory() {
        return FileModes.testBitmask(getFileMode(), FileModes.IFDIR);
    }

    /**
     * @return Returns whether or not this Ext2File represents a regular file.
     */
    public boolean isRegularFile() {
        return FileModes.testBitmask(getFileMode(), FileModes.IFREG);
    }

    /**
     * @return Returns the list of {@link DirectoryEntry}s for this file.
     * @throws UnsupportedOperationException Throws an exception if this file is not a directory.
     * @see #isDirectory()
     */
    public List<DirectoryEntry> getEntries() {
        return inode.getEntries();
    }

    /**
     * Output the directory entries for this file in a Unix format to the
     * system console.
     */
    public void printEntries() {
        // Iterate over the directory entries.
        for (DirectoryEntry entry : getEntries()) {
            Ext2File file;
            try {
                file = new Ext2File(this, entry.getLabel());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                continue;
            }

            System.out.printf(
                    "%s %d %4d %4d %6s %s %s\n",
                    FileModes.toString(file.getFileMode()),
                    file.getHardLinksCount(),
                    file.getUnixUid(),
                    file.getUnixGid(),
                    ByteUtils.formatHumanReadable(file.getSize()),
                    DateUtils.formatDirectoryListingDate(new Date(file.getLastModifiedTime() * 1000)),
                    entry.getLabel()
            );
        }
    }

    /**
     * @return Returns this Ext2File's file path in the filesystem.
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * @return Returns the name of this file.
     */
    public String getFileName() {
        Path fileName = Path.of(filePath).getFileName();
        if (fileName == null)
            return "/";
        return fileName.toString();
    }

    /**
     * @return Returns a reference to the parent directory as an Ext2File.
     */
    public Ext2File getParentDirectory() {
        try {
            return new Ext2File(this, "..");
        } catch (FileNotFoundException e) {
            System.err.println("An error occurred when attempting to find the parent directory");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @return Returns the current seek position in the file.
     */
    public long getPosition() {
        return position;
    }

    /**
     * Change the pointer position for reading from this file.
     */
    public void seek(long position) {
        if (position < 0)
            throw new IllegalArgumentException("Cannot seek to before start of the file");

        this.position = position;
    }

    /**
     * Read an array of bytes from the file at a specific offset.
     *
     * @param startByte the starting offset in the file to read the bytes from
     * @param length    the number of bytes to read
     * @return Returns an array of bytes.
     * @see Ext2File#read(long)
     */
    public byte[] read(long startByte, long length) {
        if (length < 0)
            throw new IllegalArgumentException("Cannot retrieve byte array of length less than zero");
        if (length >= Integer.MAX_VALUE)
            throw new IllegalArgumentException("Cannot retrieve byte array of length greater than or equal to Integer.MAX_VALUE");

        position = startByte + length;
        if (position > getSize())
            throw new IndexOutOfBoundsException("Cannot read beyond the bounds of the file");

        return inode.read(startByte, (int) length);
    }

    /**
     * Read an array of bytes from the file.
     * <p>
     * This method starts reading from the current seek position.
     *
     * @param length the number of bytes to read
     * @return Returns an array of bytes.
     * @see Ext2File#read(long, long)
     */
    public byte[] read(long length) {
        return read(position, length);
    }

    /**
     * The size of this file in bytes.
     */
    public long getSize() {
        return inode.getFileSize();
    }

    /**
     * The UNIX timestamp for when this file was created.
     */
    public long getCreationTime() {
        return inode.getCreationTime();
    }

    /**
     * The UNIX timestamp for when this file was last accessed.
     */
    public long getLastAccessTime() {
        return inode.getLastAccessTime();
    }

    /**
     * The UNIX timestamp for when this file was last modified.
     */
    public long getLastModifiedTime() {
        return inode.getLastModifiedTime();
    }

    /**
     * The UNIX timestamp for when this file was deleted. This value is 0 if
     * the file has not been deleted.
     */
    public long getDeletedTime() {
        return inode.getDeletedTime();
    }

    /**
     * The filemode bits for this file.
     *
     * @see Inode.FileModes
     */
    public short getFileMode() {
        return inode.getFileMode();
    }

    /**
     * The UNIX user id who owns this file.
     */
    public short getUnixUid() {
        return inode.getUserId();
    }

    /**
     * The UNIX group id who owns this file.
     */
    public short getUnixGid() {
        return inode.getGroupId();
    }

    /**
     * The number of hard links to this file on the filesystem.
     */
    public short getHardLinksCount() {
        return inode.getHardLinksCount();
    }

    @Override
    public String toString() {
        return "Ext2File{" +
                "volume=" + volume +
                ", filePath='" + filePath + '\'' +
                ", inode=" + inode +
                ", position=" + position +
                '}';
    }
}
