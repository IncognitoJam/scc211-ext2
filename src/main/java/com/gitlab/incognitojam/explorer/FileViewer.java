package com.gitlab.incognitojam.explorer;

import com.gitlab.incognitojam.ext2.ByteUtils;
import com.gitlab.incognitojam.ext2.DateUtils;
import com.gitlab.incognitojam.ext2.Ext2File;
import com.gitlab.incognitojam.ext2.Inode.FileModes;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.util.Arrays;
import java.util.Date;

class FileViewer extends JFrame {
    FileViewer(Ext2File file) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(500, 800);
        setTitle("EXT2 File Viewer: " + file.getFilePath());
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
        TextArea textArea = new TextArea();
        file.seek(0L);
        final long start = System.currentTimeMillis();
        while (file.getPosition() < file.getSize()) {
            byte[] data = file.read(Math.min(1024L, file.getSize() - file.getPosition()));
            boolean empty = true;
            for (byte b : data) {
                if (b > 0) {
                    empty = false;
                    break;
                }
            }
            if (!empty) textArea.append(new String(data));
        }
        final long end = System.currentTimeMillis();
        System.out.printf("Duration: %2.2f seconds\n", (end - start) / 1000.0);
        textArea.setEditable(false);
        pane.add(textArea, BorderLayout.CENTER);

        setVisible(true);
    }
}
