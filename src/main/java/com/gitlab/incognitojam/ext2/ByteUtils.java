package com.gitlab.incognitojam.ext2;

public class ByteUtils {
    public static String formatHumanReadable(long bytes) {
        final int unit = 1024;

        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "KMGTPE".charAt(exp - 1) + "i";
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
