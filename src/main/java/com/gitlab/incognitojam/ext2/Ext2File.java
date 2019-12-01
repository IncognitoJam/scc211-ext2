package com.gitlab.incognitojam.ext2;

import com.gitlab.incognitojam.ext2.Inode.FileModes;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO(docs): write javadoc
 */
public class Ext2File {
    public static final String pathSeparator = "/";

    private final Volume volume;
    private final String filePath;
    private final Inode inode;
    private long position;

    /**
     * TODO(docs): write javadoc
     */
    public Ext2File(Volume volume, String filePath) throws FileNotFoundException {
        this.volume = volume;
        this.filePath = filePath;
        this.inode = volume.navigate(filePath);
        if (inode == null)
            throw new FileNotFoundException("Could not find file " + filePath);
        position = 0L;
    }

    /**
     * TODO(docs): write javadoc
     */
    public Ext2File(Ext2File parent, String child) throws FileNotFoundException {
        this(parent.volume, Path.of(parent.filePath, child).normalize().toString());
    }

    /**
     * TODO(docs): write javadoc
     */
    public boolean isDirectory() {
        return FileModes.testBitmask(getFileMode(), FileModes.IFDIR);
    }

    /**
     * TODO(docs): write javadoc
     */
    public boolean isRegularFile() {
        return FileModes.testBitmask(getFileMode(), FileModes.IFREG);
    }

    /**
     * TODO(docs): write javadoc
     */
    public List<DirectoryEntry> getEntries() {
        if (!isDirectory())
            throw new UnsupportedOperationException("Must only call Ext2File#getEntries() on directory files.");

        byte[] dataBytes = inode.read(0, (int) inode.getFileSize());
        ByteBuffer data = ByteUtils.wrap(dataBytes);

        List<DirectoryEntry> entries = new ArrayList<>();
        int ptr = 0;
        while (ptr < dataBytes.length) {
            data.position(ptr);
            DirectoryEntry entry = new DirectoryEntry(data);

            // Skip entries which don't point to a valid inode
            if (entry.getInode() > 0)
                entries.add(entry);

            ptr += entry.getLength();
        }

        return entries;
    }

    /**
     * TODO(docs): write javadoc
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * TODO(docs): write javadoc
     */
    public String getFileName() {
        Path fileName = Path.of(filePath).getFileName();
        if (fileName == null)
            return "/";
        return fileName.toString();
    }

    /**
     * TODO(docs): write javadoc
     */
    public long getPosition() {
        return position;
    }

    /**
     * TODO(docs): write javadoc
     */
    public void seek(long position) {
        if (position < 0)
            throw new IllegalArgumentException("Cannot seek to before start of the file");

        this.position = position;
    }

    /**
     * TODO(docs): write javadoc
     */
    public byte[] read(long startByte, long length) {
        if (length < 0)
            throw new IllegalArgumentException("Cannot retrieve byte array of length less than zero");
        if (length >= Integer.MAX_VALUE)
            throw new IllegalArgumentException("Cannot retrieve byte array of length greater than or equal to Integer.MAX_VALUE");

        position = startByte + length;
        if (position >= getSize())
            throw new IndexOutOfBoundsException("Cannot read beyond the bounds of the file");

        return inode.read(startByte, (int) length);
    }

    /**
     * TODO(docs): write javadoc
     */
    public byte[] read(long length) {
        return read(position, length);
    }

    /**
     * TODO(docs): write javadoc
     */
    public long getSize() {
        return inode.getFileSize();
    }

    /**
     * TODO(docs): write javadoc
     */
    public long getCreationTime() {
        return inode.getCreationTime();
    }

    /**
     * TODO(docs): write javadoc
     */
    public long getLastAccessTime() {
        return inode.getLastAccessTime();
    }

    /**
     * TODO(docs): write javadoc
     */
    public long getLastModifiedTime() {
        return inode.getLastModifiedTime();
    }

    /**
     * TODO(docs): write javadoc
     */
    public long getDeletedTime() {
        return inode.getDeletedTime();
    }

    /**
     * TODO(docs): write javadoc
     */
    public short getFileMode() {
        return inode.getFileMode();
    }

    /**
     * TODO(docs): write javadoc
     */
    public short getUnixUid() {
        return inode.getUserId();
    }

    /**
     * TODO(docs): write javadoc
     */
    public short getUnixGid() {
        return inode.getGroupId();
    }

    /**
     * TODO(docs): write javadoc
     */
    public short getHardLinksCount() {
        return inode.getHardLinksCount();
    }
}
