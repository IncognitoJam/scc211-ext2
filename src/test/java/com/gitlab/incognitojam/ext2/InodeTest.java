package com.gitlab.incognitojam.ext2;

import com.gitlab.incognitojam.ext2.Inode.FileModes;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.gitlab.incognitojam.ext2.Inode.FileModes.*;
import static org.junit.Assert.*;

public class InodeTest {
    private static final String volumeFilename = "ext2fs";
    private static Volume volume;

    @BeforeClass
    public static void setUp() throws IOException {
        volume = new Volume(volumeFilename);
    }

    @Test
    public void testFileModeToString() {
        assertEquals("----------", FileModes.toString(IFREG));
        assertEquals("d---------", FileModes.toString(IFDIR));
        assertEquals("-rwxr-xr-x", FileModes.toString(IFREG | IRUSR | IWUSR | IXUSR | IRGRP | IXGRP | IROTH | IXOTH));
        assertEquals("-rwxrwxrwx",
                FileModes.toString(IFREG | IRUSR | IWUSR | IXUSR | IRGRP | IWGRP | IXGRP | IROTH | IWOTH | IXOTH));
    }

    @Test
    public void testRootInode() {
        Inode root = volume.getInode(2);

        assertNotNull(root);
        assertEquals("file mode", IFDIR | IRUSR | IWUSR | IXUSR | IRGRP | IXGRP | IROTH | IXOTH, root.getFileMode());
        assertEquals("user id", 0, root.getUserId());
        assertEquals("last access time", 1415188273, root.getLastAccessTime());
        assertEquals("creation time", 1415188180, root.getCreationTime());
        assertEquals("last modified time", 1415188180, root.getLastModifiedTime());
        assertEquals("deleted time", 0, root.getDeletedTime());
        assertEquals("group id", 0, root.getGroupId());
        assertEquals("hard links count", 6, root.getHardLinksCount());
        assertEquals("file size", 1024, root.getFileSize());

        List<DirectoryEntry> entries = root.getEntries();
        assertNotNull(entries);
        assertEquals("entries length", 7, entries.size());

        final List<String> expectedEntriesLabels = Arrays.asList(".", "..", "lost+found", "files", "big-dir", "two-cities", "deep");
        List<String> entriesLabels = entries.stream().map(DirectoryEntry::getLabel).collect(Collectors.toList());
        assertEquals("entries labels", expectedEntriesLabels, entriesLabels);
    }
}
