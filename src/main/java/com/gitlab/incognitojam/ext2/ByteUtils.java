package com.gitlab.incognitojam.ext2;

/**
 * Utility methods relating to bytes.
 */
public class ByteUtils {
    /**
     * Format an number of bytes with the matching suffix.
     *
     * For example, see how 1_000_000L is formatted:
     * ```
     * long bytes = 1_000_000L;
     * String bytesLabel = ByteUtils.formatHumanReadable(bytes);
     *
     * bytesLabel.equals("976.6 KiB"); // true
     * ```
     *
     * @param bytes the value of bytes to format
     * @return returns formatted string with correct suffix
     */
    public static String formatHumanReadable(long bytes) {
        final int unit = 1024;

        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "KMGTPE".charAt(exp - 1) + "i";
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
