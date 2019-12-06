package com.gitlab.incognitojam.explorer;

import com.gitlab.incognitojam.explorer.FileMenu.FileMenuListener;
import com.gitlab.incognitojam.ext2.DirectoryEntry;
import com.gitlab.incognitojam.ext2.DirectoryEntry.FileTypes;
import com.gitlab.incognitojam.ext2.Ext2File;
import com.gitlab.incognitojam.ext2.Volume;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Ext2Explorer extends JFrame {
    private final Volume volume;
    private Ext2File file;
    private Ext2File selectedFile;

    public Ext2Explorer(Volume volume) throws FileNotFoundException {
        this.volume = volume;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1000, 600);
        setTitle("EXT2 Explorer");
        setIconImage(Icons.FOLDER_IMAGE);

        setLayout(new BorderLayout());
        setFilePath(new Ext2File(volume, "/"));

        setVisible(true);
    }

    public void openFile(Ext2File file) {
        if (file.isDirectory())
            setFilePath(file);
        else if (file.isRegularFile()) {
            if (file.getMimeType().map(mimeType -> mimeType.startsWith("image/")).orElse(false))
                new ImageViewer(file);
            else
                new FileViewer(file);
        }
    }

    private void setFilePath(Ext2File file) {
        this.file = file;
        Container pane = getContentPane();
        pane.removeAll();

        // Create the address bar
        pane.add(new AddressBar(), BorderLayout.PAGE_START);

        // Create the directory viewer
        JScrollPane scrollPane = new JScrollPane(
                new DirectoryViewer(),
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        pane.add(scrollPane, BorderLayout.CENTER);

        // Create the status bar
        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        pane.add(statusPanel, BorderLayout.SOUTH);
        statusPanel.setPreferredSize(new Dimension(pane.getWidth(), 16));
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        List<DirectoryEntry> entries = file.getEntries().stream().filter(entry -> !entry.getLabel().contains(".")).collect(Collectors.toList());
        JLabel statusLabel = new JLabel(String.format(
                "%d directories, %d files\n",
                entries.stream().filter(entry -> entry.getFileType() == FileTypes.DIRECTORY).count(),
                entries.stream().filter(entry -> entry.getFileType() == FileTypes.REGULAR_FILE).count()
        ));
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusPanel.add(statusLabel);

        SwingUtilities.updateComponentTreeUI(this);
    }

    private class AddressBar extends JPanel {
        AddressBar() {
            setSize(300, 32);
            setLayout(new BorderLayout());

            JPanel actionPanel = new JPanel();
            actionPanel.setLayout(new GridLayout(1, 2));
            actionPanel.setSize(64, 32);

            {
                JButton homeButton = new JButton(Icons.HOME);
                homeButton.setSize(32, 32);
                homeButton.addActionListener(event -> updatePath("/"));
                actionPanel.add(homeButton);

                JButton upButton = new JButton(Icons.UP);
                upButton.setSize(32, 32);
                upButton.addActionListener(event -> updatePath(file.getParentDirectory().getFilePath()));
                actionPanel.add(upButton);
                add(actionPanel, BorderLayout.LINE_START);
            }

            JTextField pathField = new JTextField(file.getFilePath());
            pathField.setSize(300 - 64, 32);
            pathField.addActionListener(event -> updatePath(event.getActionCommand()));
            add(pathField, BorderLayout.CENTER);

            JButton goButton = new JButton(Icons.GO);
            goButton.setSize(32, 32);
            goButton.addActionListener(event -> updatePath(pathField.getText()));
            add(goButton, BorderLayout.LINE_END);
        }

        private void updatePath(String filePath) {
            Ext2File file;
            try {
                file = new Ext2File(volume, filePath);
                if (!file.isDirectory()) {
                    /*
                     * If the given file was not a directory, use the parent
                     * directory instead and open the file.
                     */
                    openFile(file);
                    file = file.getParentDirectory();
                }
            } catch (FileNotFoundException e) {
                JOptionPane.showMessageDialog(
                        this,
                        "Cannot navigate to '" + filePath + "' as it does not exist.",
                        "Illegal Path",
                        JOptionPane.ERROR_MESSAGE,
                        Icons.WARNING
                );
                return;
            }
            setFilePath(file);
        }
    }

    private class DirectoryViewer extends JPanel {
        DirectoryViewer() {
            int columns = 3;
            int rows = 1000;
            setSize(100, 3000);
            setLayout(new GridLayout(rows, columns));

            List<DirectoryEntry> entries = file.getEntries();
            entries.sort(Comparator.comparing(DirectoryEntry::getLabel));
            for (DirectoryEntry entry : entries) {
                /*
                 * Skip directory traversal entries. We implement this feature
                 * using JButtons instead.
                 */
                if (entry.getLabel().startsWith("."))
                    continue;

                try {
                    // Instantiate a FileButton for each directory entry.
                    Ext2File child = new Ext2File(file, entry.getLabel());
                    FileButton button = new FileButton(child);
                    add(button);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        class FileButton extends JLabel {
            FileButton(Ext2File file) {
                super(file.getFileName(), Icons.getFileIcon(file), SwingConstants.LEADING);

                setSize(100, 100);
                setBorder(BorderFactory.createDashedBorder(Color.DARK_GRAY));

                addMouseListener(new FileMenuListener(Ext2Explorer.this, file));
                addMouseListener(new MouseListener() {
                    @Override
                    public void mouseClicked(MouseEvent mouseEvent) {
                        if (selectedFile == file) {
                            openFile(file);
                            selectedFile = null;
                        } else
                            selectedFile = file;
                    }

                    @Override
                    public void mousePressed(MouseEvent mouseEvent) {

                    }

                    @Override
                    public void mouseReleased(MouseEvent mouseEvent) {

                    }

                    @Override
                    public void mouseEntered(MouseEvent mouseEvent) {

                    }

                    @Override
                    public void mouseExited(MouseEvent mouseEvent) {

                    }
                });
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Volume volume = new Volume("ext2fs-plus");
        Ext2Explorer explorer = new Ext2Explorer(volume);
    }
}
