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

            Ext2File directEnd = new Ext2File(volume, "/files/dir-e");
            ByteUtils.dumpHexBytes(directEnd.read(directEnd.getSize() - 20L, 20L));

            Ext2File indirectEnd = new Ext2File(volume, "/files/ind-e");
            ByteUtils.dumpHexBytes(indirectEnd.read(indirectEnd.getSize() - 20L, 20L));

            Ext2File doubleIndirectEnd = new Ext2File(volume, "/files/dbl-ind-e");
            ByteUtils.dumpHexBytes(doubleIndirectEnd.read(doubleIndirectEnd.getSize() - 20L, 20L));

            Ext2File tripleIndirectEnd = new Ext2File(volume, "/files/trpl-ind-e");
            ByteUtils.dumpHexBytes(tripleIndirectEnd.read(tripleIndirectEnd.getSize() - 20L, 20L));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Demo();
    }
}
