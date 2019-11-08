package com.gitlab.incognitojam.demo;

import com.gitlab.incognitojam.ext2.ByteUtils;
import com.gitlab.incognitojam.ext2.Volume;

import java.io.IOException;

public class Demo {
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
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Demo();
    }
}
