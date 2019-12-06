package com.gitlab.incognitojam.explorer;

import com.gitlab.incognitojam.ext2.ByteUtils;
import com.gitlab.incognitojam.ext2.DateUtils;
import com.gitlab.incognitojam.ext2.Ext2File;
import com.gitlab.incognitojam.ext2.Inode.FileModes;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.util.Date;

class HexViewer extends JFrame {
    HexViewer(Ext2File file) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(550, 800);
        setTitle("EXT2 Hex Viewer: " + file.getFilePath());
        setIconImage(Icons.TEXT_IMAGE);

        setLayout(new BorderLayout());
        Container pane = getContentPane();

        // create the status bar
        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        pane.add(statusPanel, BorderLayout.SOUTH);
        statusPanel.setPreferredSize(new Dimension(pane.getWidth(), 16));
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        JLabel statusLabel = new JLabel(String.format(
                "%s %d %d %d %s %s %s\n",
                FileModes.toString(file.getFileMode()),
                file.getHardLinksCount(),
                file.getUnixUid(),
                file.getUnixGid(),
                ByteUtils.formatHumanReadable(file.getSize()),
                DateUtils.formatDirectoryListingDate(new Date(file.getLastModifiedTime() * 1000)),
                file.getFileName()
        ));
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusPanel.add(statusLabel);

        // create the text area contents
        ColourTextPane textPane = new ColourTextPane();
        textPane.setEditable(false);
        textPane.setFont(new Font("Noto Mono", Font.PLAIN, 11));
        pane.add(new JScrollPane(textPane), BorderLayout.CENTER);

        if (file.getSize() < Integer.MAX_VALUE) {
            final byte[] data = file.read(0L, file.getSize());
            final String hexDump = ByteUtils.formatHexBytes(data);
            textPane.addMessage(hexDump);
        } else textPane.addMessage("File too large to produce hexdump.");

        setVisible(true);
    }

    private static class ColourTextPane extends JTextPane {
        private static final String COLOUR_NULL = "\033[0;90m"; // black bright
        private static final String COLOUR_ASCII_WHITESPACE = "\033[0;32m"; // green
        private static final String COLOUR_ASCII_PRINTABLE = "\033[0;36m"; // cyan
        private static final String COLOUR_OTHER = "\033[0;35m"; // purple
        private static final String COLOUR_RESET = "\033[0m"; // reset

        private SimpleAttributeSet attributes = new SimpleAttributeSet();

        public ColourTextPane() {
            StyleConstants.setForeground(attributes, Color.WHITE);
            StyleConstants.setBackground(attributes, Color.BLACK);
            super.setBackground(Color.BLACK);
        }

        private void setColour(Color colour) {
            StyleConstants.setForeground(attributes, colour);
        }

        private void append(String message) {
            int length = getDocument().getLength();
            try {
                getDocument().insertString(length, message, attributes);
            } catch (BadLocationException exception) {
                exception.printStackTrace();
            }
        }

        public void addMessage(String message) {
            final String escapeBeginning = "\033[0";
            final String escapeEnd = "m";

            while (true) {
                // If the message has no length we don't need to add anything.
                if (message.length() == 0)
                    return;

                int index = message.indexOf(escapeBeginning);
                if (index < 0) {
                    /*
                     * If there are no colour codes in the given message, append
                     * string without changing the colour.
                     */
                    append(message);
                    return;
                }

                if (index > 0) {
                    /*
                     * The colour code does not start at the first character so
                     * append all of the text in the message up to this index.
                     */
                    append(message.substring(0, index));
                    message = message.substring(index);
                }

                // Find the end of the colour code.
                int endIndex = message.indexOf(escapeEnd);
                if (endIndex < 0) {
                    /*
                     * The message ends part way through the colour code,
                     * so just skip these characters.
                     */
                    message = message.substring(escapeBeginning.length());
                } else {
                    // Extract the colour code and change the foreground colour.
                    setColour(parseColour(message.substring(0, endIndex + 1)));
                    message = message.substring(endIndex + 1);
                }
            }
        }

        private Color parseColour(String consoleColour) {
            switch (consoleColour) {
                case COLOUR_NULL:
                    return Color.DARK_GRAY;
                case COLOUR_ASCII_WHITESPACE:
                    return Color.GREEN;
                case COLOUR_ASCII_PRINTABLE:
                    return Color.CYAN;
                case COLOUR_OTHER:
                    return Color.MAGENTA;
                case COLOUR_RESET:
                default:
                    return Color.WHITE;
            }
        }
    }
}
