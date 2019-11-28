package com.gitlab.incognitojam.demo;

import com.gitlab.incognitojam.ext2.ByteUtils;
import com.gitlab.incognitojam.ext2.DirectoryEntry;
import com.gitlab.incognitojam.ext2.Ext2File;
import com.gitlab.incognitojam.ext2.Inode.FileModes;
import com.gitlab.incognitojam.ext2.Volume;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class Demo {
    private static final SimpleDateFormat CURRENT_YEAR = new SimpleDateFormat("MMM dd HH:mm");
    private static final SimpleDateFormat ALTERNATE_YEAR = new SimpleDateFormat("MMM dd YYYY");

    /**
     * TODO(refactor): move to another class
     */
    private static String formatDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        final int year = calendar.get(Calendar.YEAR);
        final int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        return (year == currentYear ? CURRENT_YEAR : ALTERNATE_YEAR).format(date);
    }

    private Demo() {
        System.out.println();
        try (final Volume volume = new Volume("ext2fs")) {
            /*
             * Print details about the volume.
             */
            System.out.println("Label:      " + volume.getLabel());
            System.out.println("Blocks:     " + volume.getBlocks());
            System.out.println("Block size: " + ByteUtils.formatHumanReadable(volume.getBlockSize()));
            System.out.println("Capacity:   " + ByteUtils.formatHumanReadable(volume.getCapacity()));
            System.out.println();

            Ext2File files = new Ext2File(volume, "/");
            for (DirectoryEntry entry : files.getEntries()) {
                if (entry.getLabel().contains("."))
                    continue;

                Ext2File file = new Ext2File(files, entry.getLabel());
                System.out.printf(
                        "%s %d %d %d %s %s %s\n",
                        FileModes.toString(file.getFileMode()),
                        file.getHardLinksCount(),
                        file.getUnixUid(),
                        file.getUnixGid(),
                        ByteUtils.formatHumanReadable(file.getSize()),
                        formatDate(new Date(file.getLastModifiedTime() * 1000)),
                        entry.getLabel()
                );
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Demo();
    }
}
