package com.gitlab.incognitojam.ext2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Utility methods relating to bytes.
 * <p>
 * TODO: improve documentation
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
        if (bytes < unit) return String.valueOf(bytes);

        /*
         * Determine the "exponent" of the unit, or how many times you would
         * need to multiply the unit by itself to get to this number of bytes.
         *
         * This tells us which suffix to use.
         */
        int exponent = (int) (Math.log(bytes) / Math.log(unit));

        // Get the correct suffix.
        char suffix = "KMGTPE".charAt(exponent - 1);

        // Convert number of bytes to new unit (if MB then divide by 1 mil...).
        double value = bytes / Math.pow(unit, exponent);

        // Round the value to 1 DP and format it!
        return String.format("%.1f%c", value, suffix);
    }

    private static final String COLOUR_NULL = "\033[0;90m"; // black bright
    private static final String COLOUR_ASCII_WHITESPACE = "\033[0;32m"; // green
    private static final String COLOUR_ASCII_PRINTABLE = "\033[0;36m"; // cyan
    private static final String COLOUR_OTHER = "\033[0;35m"; // purple
    private static final String COLOUR_RESET = "\033[0m"; // reset

    /**
     * Present an array of bytes in a hexadecimal view, similar to the output
     * of the hexdump command on Unix systems. The formatted data will be
     * written to the console.
     * <p>
     * TODO: optionally, condense duplicate lines
     *
     * @param bytes       the byte array to format
     * @param showAddress whether or not to show the address line numbers in
     *                    the output
     * @param showColours whether or not to format the output with colours
     */
    public static void dumpHexBytes(byte[] bytes, boolean showAddress, boolean showColours) {
        StringBuilder builder = new StringBuilder();

        /*
         * The position in the array to read the next line from, incremented
         * by two bytes with each pass.
         */
        int address = 0;
        while (address < bytes.length) {
            /*
             * Read the current line from the byte array. We print two bytes at
             * a time.
             */
            final byte[] line = Arrays.copyOfRange(bytes, address, address + 16);

            /*
             * As we iterate over the bytes in this line, we need to build
             * strings for both their hexadecimal and ascii representations.
             *
             * We use StringBuilder to efficiently construct the strings.
             */
            StringBuilder hex = new StringBuilder();
            StringBuilder ascii = new StringBuilder();

            // Iterate over each byte in the line.
            for (final byte b : line) {
                if (showColours) {
                    final String color = getByteColour(b);
                    hex.append(color);
                    ascii.append(color);
                }

                // Format it in hex, appending it to the hex builder.
                hex.append(String.format("%02x", b));
                hex.append(' ');

                // Calculate the ASCII representation.
                if (b < 32 || b == 127)
                    /*
                     * The value 127 and all values less than 32 aren't printable
                     * in ascii, so we use a period instead.
                     */
                    ascii.append('.');
                else
                    /*
                     * Simply casting the byte to a char will print the ASCII
                     * representation in Java.
                     */
                    ascii.append((char) b);
            }

            /*
             * Append the hex and ascii strings to the main string builder.
             *
             * Optionally insert the "line number" if the showAddress argument
             * is truthy.
             */
            if (showAddress) {
                builder.append(String.format("%07x", address));
                builder.append(": ");
            }
            builder.append(hex);
            builder.append(' ');
            builder.append(ascii);
            if (showColours) builder.append(COLOUR_RESET);
            builder.append('\n');

            // Finally, increment the position by two bytes.
            address += 16;
        }

        // Print the resulting string to the console.
        System.out.println(builder);
    }

    /**
     * TODO(docs): write javadoc
     */
    private static String getByteColour(byte b) {
        if (b == 0)
            return COLOUR_NULL;
        else if (isAsciiWhitespace(b))
            return COLOUR_ASCII_WHITESPACE;
        else if (isAsciiPrintable(b))
            return COLOUR_ASCII_PRINTABLE;
        else
            return COLOUR_OTHER;
    }

    /**
     * TODO(docs): write javadoc
     */
    private static boolean isAsciiPrintable(byte b) {
        return b >= 0x20 && b <= 0x7e;
    }

    /**
     * TODO(docs): write javadoc
     */
    private static boolean isAsciiWhitespace(byte b) {
        return b == 0x20;
    }

    /**
     * TODO(docs): write javadoc
     */
    public static ByteBuffer wrap(byte[] array) {
        ByteBuffer buffer = ByteBuffer.wrap(array);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer;
    }
}
