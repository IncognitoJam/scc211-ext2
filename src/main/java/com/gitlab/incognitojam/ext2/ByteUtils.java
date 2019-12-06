package com.gitlab.incognitojam.ext2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Utility methods relating to bytes.
 */
public class ByteUtils {
    /**
     * Format an number of bytes with the matching suffix.
     * <p>
     * For example, see how 1_000_000L is formatted:
     * <p>
     * <blockquote><pre>
     * long bytes = 1_000_000L;
     * String bytesLabel = ByteUtils.formatHumanReadable(bytes);
     *
     * bytesLabel.equals("976.6 KiB"); // true
     * </pre></blockquote>
     *
     * @param bytes The value of bytes to format.
     * @return Returns a formatted string with correct suffix.
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
     * returned as a string.
     *
     * @param data The array of bytes to format.
     * @return Returns the formatted string.
     * @see #formatHexBytes(byte[], boolean, boolean, boolean)
     */
    public static String formatHexBytes(byte[] data) {
        return formatHexBytes(data, true);
    }

    /**
     * Present an array of bytes in a hexadecimal view, similar to the output
     * of the hexdump command on Unix systems. The formatted data will be
     * returned as a string.
     *
     * @param data        The array of bytes to format.
     * @param showColours Whether or not the output should include ANSI colour
     *                    codes.
     * @return Returns the formatted string.
     * @see #formatHexBytes(byte[], boolean, boolean, boolean)
     */
    public static String formatHexBytes(byte[] data, boolean showColours) {
        return formatHexBytes(data, showColours, true);
    }

    /**
     * Present an array of bytes in a hexadecimal view, similar to the output
     * of the hexdump command on Unix systems. The formatted data will be
     * returned as a string.
     *
     * @param data               The array of bytes to format.
     * @param showColours        Whether or not the output should include ANSI colour
     *                           codes.
     * @param condenseDuplicates Whether or not duplicate lines in the output
     *                           should be condensed with an asterisk.
     * @return Returns the formatted string.
     * @see #formatHexBytes(byte[], boolean, boolean, boolean)
     */
    public static String formatHexBytes(byte[] data, boolean showColours, boolean condenseDuplicates) {
        return formatHexBytes(data, showColours, condenseDuplicates, true);
    }

    /**
     * Present an array of bytes in a hexadecimal view, similar to the output
     * of the hexdump command on Unix systems. The formatted data will be
     * returned as a string.
     *
     * @param data               The array of bytes to format.
     * @param showColours        Whether or not the output should include ANSI colour
     *                           codes.
     * @param condenseDuplicates Whether or not duplicate lines in the output
     *                           should be condensed with an asterisk.
     * @param showAddress        Whether or not to include the address line
     *                           numbers in the output.
     * @return Returns the formatted string.
     * @see #formatHexBytes(byte[], boolean, boolean, boolean)
     */
    public static String formatHexBytes(byte[] data, boolean showColours, boolean condenseDuplicates, boolean showAddress) {
        StringBuilder builder = new StringBuilder();

        /*
         * The position in the array to read the next line from, incremented
         * by two bytes with each pass.
         */
        int address = 0;
        byte[] previousLine = null;
        boolean condensed = false;
        while (address < data.length) {
            /*
             * Read the current line from the byte array. We print two bytes at
             * a time.
             */
            final byte[] line = Arrays.copyOfRange(data, address, address + 16);

            // Replace duplicate lines with an asterisk character.
            if (condenseDuplicates) {
                if (Arrays.equals(previousLine, line) && address + 16 < data.length) {
                    address += 16;
                    if (!condensed) {
                        condensed = true;
                        builder.append("*\n");
                    }
                    continue;
                }
                previousLine = line;
            }

            /*
             * As we iterate over the bytes in this line, we need to build
             * strings for both their hexadecimal and ascii representations.
             *
             * We use StringBuilder to efficiently construct the strings.
             */
            StringBuilder hex = new StringBuilder();
            StringBuilder ascii = new StringBuilder();

            // Iterate over each byte in the line.
            int i = address;
            for (final byte b : line) {
                if (i >= data.length) {
                    hex.append("   ");
                    ascii.append(" ");
                    i++;
                    continue;
                }

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

                i++;
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

            // Finally, increment the position by two bytes.
            address += 16;
            if (address < data.length) builder.append('\n');
        }

        return builder.toString();
    }

    /**
     * Present an array of bytes in a hexadecimal view, similar to the output
     * of the hexdump command on Unix systems. The formatted data will be
     * written to the console.
     *
     * @param data The array of bytes to format.
     */
    public static void dumpHexBytes(byte[] data) {
        dumpHexBytes(data, true);
    }

    /**
     * Present an array of bytes in a hexadecimal view, similar to the output
     * of the hexdump command on Unix systems. The formatted data will be
     * written to the console.
     *
     * @param data        The array of bytes to format.
     * @param showColours Whether or not the output should include ANSI colour
     *                    codes.
     */
    public static void dumpHexBytes(byte[] data, boolean showColours) {
        dumpHexBytes(data, showColours, true);
    }

    /**
     * Present an array of bytes in a hexadecimal view, similar to the output
     * of the hexdump command on Unix systems. The formatted data will be
     * written to the console.
     *
     * @param data               The array of bytes to format.
     * @param showColours        Whether or not the output should include ANSI colour
     *                           codes.
     * @param condenseDuplicates Whether or not duplicate lines in the output
     *                           should be condensed with an asterisk.
     */
    public static void dumpHexBytes(byte[] data, boolean showColours, boolean condenseDuplicates) {
        dumpHexBytes(data, showColours, condenseDuplicates, true);
    }

    /**
     * Present an array of bytes in a hexadecimal view, similar to the output
     * of the hexdump command on Unix systems. The formatted data will be
     * written to the console.
     *
     * @param data               The array of bytes to format.
     * @param showColours        Whether or not the output should include ANSI colour
     *                           codes.
     * @param condenseDuplicates Whether or not duplicate lines in the output
     *                           should be condensed with an asterisk.
     * @param showAddress        Whether or not to include the address line
     *                           numbers in the output.
     */
    public static void dumpHexBytes(byte[] data, boolean showColours, boolean condenseDuplicates, boolean showAddress) {
        String formattedBytes = formatHexBytes(data, showColours, condenseDuplicates, showAddress);
        System.out.println(formattedBytes);
    }

    /**
     * @return Returns the appropriate ANSI colour for outputting this byte to
     * the console.
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
     * @return Returns whether or not this byte is within the ASCII printable
     * characters range.
     */
    private static boolean isAsciiPrintable(byte b) {
        return b >= 0x20 && b <= 0x7e;
    }

    /**
     * @return Returns whether or not this byte is an ASCII whitespace
     * character (a space).
     */
    private static boolean isAsciiWhitespace(byte b) {
        return b == 0x20;
    }

    /**
     * Wrap the given byte array in a ByteBuffer object with the byte order
     * set to the LITTLE_ENDIAN format.
     *
     * @return Returns the new ByteBuffer object.
     */
    public static ByteBuffer wrap(byte[] array) {
        ByteBuffer buffer = ByteBuffer.wrap(array);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer;
    }
}
