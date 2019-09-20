package com.gitlab.incognitojam.ext2;

import com.gitlab.incognitojam.ext2.Inode.FileModes;
import org.junit.Test;

import static com.gitlab.incognitojam.ext2.Inode.FileModes.*;
import static org.junit.Assert.assertEquals;

public class InodeTest {
    @Test
    public void testFileModeToString() {
        assertEquals("----------", FileModes.toString(IFREG));
        assertEquals("d---------", FileModes.toString(IFDIR));
        assertEquals("-rwxr-xr-x", FileModes.toString(IFREG | IRUSR | IWUSR | IXUSR | IRGRP | IXGRP | IROTH | IXOTH));
        assertEquals("-rwxrwxrwx", FileModes.toString(IFREG | IRUSR | IWUSR | IXUSR | IRGRP | IWGRP | IXGRP | IROTH | IWOTH | IXOTH));
    }
}
