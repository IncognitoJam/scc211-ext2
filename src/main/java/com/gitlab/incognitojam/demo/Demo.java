package com.gitlab.incognitojam.demo;

import com.gitlab.incognitojam.ext2.ByteUtils;
import com.gitlab.incognitojam.ext2.Ext2File;
import com.gitlab.incognitojam.ext2.Volume;

import java.io.IOException;

public class Demo {
    private Demo() {
        try (final Volume volume = new Volume("ext2fs-plus")) {
            /*
             * Print details about the volume.
             */
            System.out.println("Label:       " + volume.getLabel());
            System.out.println("Blocks:      " + volume.getBlocks());
            System.out.println("Block size:  " + ByteUtils.formatHumanReadable(volume.getBlockSize()));
            System.out.println("Capacity:    " + ByteUtils.formatHumanReadable(volume.getCapacity()));
            System.out.println("Magic value: " + volume.getMagicValue());

            System.out.println();
            System.out.println("$ ls -lh /");
            Ext2File root = new Ext2File(volume, "/");
            root.printEntries();

            System.out.println();
            System.out.println("$ ls -lh /files");
            Ext2File files = new Ext2File(volume, "/files");
            files.printEntries();

            System.out.println();
            System.out.println("$ hexdump /files/dir-s");
            Ext2File directStart = new Ext2File(volume, "/files/dir-s");
            ByteUtils.dumpHexBytes(directStart.read(directStart.getSize()));

            System.out.println();
            System.out.println("$ hexdump /files/dir-e");
            Ext2File directEnd = new Ext2File(volume, "/files/dir-e");
            ByteUtils.dumpHexBytes(directEnd.read(directEnd.getSize()));

            System.out.println();
            System.out.println("$ hexdump /files/ind-s");
            Ext2File indirectStart = new Ext2File(volume, "/files/ind-s");
            ByteUtils.dumpHexBytes(indirectStart.read(indirectStart.getSize()));

            System.out.println();
            System.out.println("$ hexdump /files/ind-e");
            Ext2File indirectEnd = new Ext2File(volume, "/files/ind-e");
            ByteUtils.dumpHexBytes(indirectEnd.read(indirectEnd.getSize()));

            System.out.println();
            System.out.println("$ hexdump /files/dbl-ind-s");
            Ext2File doubleIndirectStart = new Ext2File(volume, "/files/dbl-ind-s");
            ByteUtils.dumpHexBytes(doubleIndirectStart.read(doubleIndirectStart.getSize()));

            System.out.println();
            System.out.println("$ hexdump /files/dbl-ind-e");
            Ext2File doubleIndirectEnd = new Ext2File(volume, "/files/dbl-ind-e");
            ByteUtils.dumpHexBytes(doubleIndirectEnd.read(doubleIndirectEnd.getSize()));

            System.out.println();
            System.out.println("$ hexdump /files/trpl-ind-s");
            Ext2File tripleIndirectStart = new Ext2File(volume, "/files/trpl-ind-s");
            ByteUtils.dumpHexBytes(tripleIndirectStart.read(tripleIndirectStart.getSize()));

            System.out.println();
            System.out.println("$ hexdump /files/trpl-ind-e");
            Ext2File tripleIndirectEnd = new Ext2File(volume, "/files/trpl-ind-e");
            ByteUtils.dumpHexBytes(tripleIndirectEnd.read(tripleIndirectEnd.getSize() - 1024L, 1024L));

            System.out.println();
            System.out.println("$ cat /two-cities");
            Ext2File twoCities = new Ext2File(volume, "/two-cities");
            System.out.println(new String(twoCities.read(twoCities.getSize())));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Demo();
    }
}
