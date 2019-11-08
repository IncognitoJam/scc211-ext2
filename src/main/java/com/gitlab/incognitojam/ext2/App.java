package com.gitlab.incognitojam.ext2;

import java.io.IOException;

public class App {
    private App() {
        try (final Volume volume = new Volume("ext2fs")) {
            System.out.println("Label:      " + volume.getLabel());
            System.out.println("Blocks:     " + volume.getBlocks());
            System.out.println("Block size: " + ByteUtils.formatHumanReadable(volume.getBlockSize()));
            System.out.println("Capacity:   " + ByteUtils.formatHumanReadable(volume.getCapacity()));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new App();
    }
}
