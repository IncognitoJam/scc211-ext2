package com.gitlab.incognitojam.explorer;

import com.gitlab.incognitojam.ext2.Ext2File;
import com.gitlab.incognitojam.ext2.Inode.FileModes;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

public class Icons {
    public static final ImageIcon DIRECTORY = loadImageIcon("folder.png");
    public static final ImageIcon TEXT_FILE = loadImageIcon("text_file.png");
    public static final ImageIcon SHORTCUT = loadImageIcon("shortcut.png");
    public static final ImageIcon UNKNOWN = loadImageIcon("unknown.png");
    public static final ImageIcon GO = loadImageIcon("go.png");
    public static final ImageIcon UP = loadImageIcon("up.png");
    public static final ImageIcon WARNING = loadImageIcon("warning.png");
    public static final ImageIcon HOME = loadImageIcon("home.png");
    public static final ImageIcon IMAGE = loadImageIcon("image.png");

    private static ImageIcon loadImageIcon(String imagePath) {
        return new ImageIcon(getResource(imagePath));
    }

    public static ImageIcon getFileIcon(Ext2File file) {
        short filemode = file.getFileMode();

        if (FileModes.testBitmask(filemode, FileModes.IFREG)) {
            if (file.getMimeType().map(mimeType -> mimeType.startsWith("image/")).orElse(false))
                return IMAGE;
            else
                return TEXT_FILE;
        } else if (FileModes.testBitmask(filemode, FileModes.IFDIR))
            return DIRECTORY;
        else if (FileModes.testBitmask(filemode, FileModes.IFLNK))
            return SHORTCUT;
        else
            return UNKNOWN;
    }

    public static final Image FOLDER_IMAGE = loadImage("folder.png");
    public static final Image TEXT_IMAGE = loadImage("text_file.png");
    public static final Image PROPERTIES_IMAGE = loadImage("properties.png");
    public static final Image IMAGE_IMAGE = loadImage("image.png");

    private static Image loadImage(String imagePath) {
        try {
            return ImageIO.read(getResource(imagePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static URL getResource(String resourcePath) {
        return Icons.class.getClassLoader().getResource(resourcePath);
    }
}