package com.gitlab.incognitojam.explorer;

import com.gitlab.incognitojam.ext2.ByteUtils;
import com.gitlab.incognitojam.ext2.DateUtils;
import com.gitlab.incognitojam.ext2.Ext2File;
import com.gitlab.incognitojam.ext2.Inode.FileModes;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.util.Date;

class ImageViewer extends JFrame {
    ImageViewer(Ext2File file) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(500, 600);
        setIconImage(Icons.IMAGE_IMAGE);

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

        // create the image label
        byte[] data = file.read(0L, file.getSize());
        Image image = Toolkit.getDefaultToolkit().createImage(data);
        ImageIcon icon = new ImageIcon(image);
        JLabel photo = new JLabel(icon, JLabel.CENTER);
        pane.add(photo, BorderLayout.CENTER);

        setTitle("EXT2 Image Viewer: " + file.getFilePath() + " (" + icon.getIconWidth() + "x" + icon.getIconHeight() + ")");
        setVisible(true);
    }
}
