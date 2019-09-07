package com.gitlab.incognitojam.ext2;

/**
 * Utility methods relating to bytes.
 */
public class ByteUtils {
    /**
     * Format an number of bytes with the matching suffix.
     * <p>
     * For example, see how 1_000_000L is formatted:
     * <p><blockquote><pre>
     * long bytes = 1_000_000L;
     * String bytesLabel = ByteUtils.formatHumanReadable(bytes);
     *
     * bytesLabel.equals("976.6 KiB"); // true
     * </pre></blockquote></p>
     * <p>
     *
     * @param bytes the value of bytes to format
     * @return returns formatted string with correct suffix
     */
    public static String formatHumanReadable(long bytes) {
        final int unit = 1024;

        /*
         * If the number of bytes is less than the lowest unit (1 MiB), just
         * append " B" and return.
         */
        if (bytes < unit) return bytes + " B";

        /*
         * Determine the "exponent" of the unit, or how many times you would
         * need to multiply the unit by itself to get to this number of bytes.
         *
         * This tells us which suffix to use.
         */
        int exponent = (int) (Math.log(bytes) / Math.log(unit));

        // Get the correct suffix.
        String suffix = "KMGTPE".charAt(exponent - 1) + "iB";

        // Convert number of bytes to new unit (if MB then divide by 1 mil...).
        double value = bytes / Math.pow(unit, exponent);

        // Round the value to 1 DP and format it!
        return String.format("%.1f %s", value, suffix);
    }
}
