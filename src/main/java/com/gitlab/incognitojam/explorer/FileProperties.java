package com.gitlab.incognitojam.explorer;

import com.gitlab.incognitojam.explorer.FileProperties.FilePropertiesCategory.FilePropertyValueGenerator;
import com.gitlab.incognitojam.ext2.ByteUtils;
import com.gitlab.incognitojam.ext2.Ext2File;
import com.gitlab.incognitojam.ext2.Inode.FileModes;

import javax.swing.*;
import java.awt.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class FileProperties extends JFrame {
    static class FilePropertiesCategory {
        final String label;
        final FilePropertiesCategoryCondition condition;
        final List<SimpleEntry<String, FilePropertyValueGenerator>> properties;

        FilePropertiesCategory(String label, FilePropertiesCategoryCondition condition) {
            this.label = label;
            this.condition = condition;
            this.properties = new ArrayList<>();
        }

        FilePropertiesCategory(String label) {
            this(label, file -> true);
        }

        private void addProperty(String name, FilePropertyValueGenerator generator) {
            properties.add(new SimpleEntry<>(name, generator));
        }

        private void addSpacer() {
            addProperty("", file -> "");
        }

        interface FilePropertyValueGenerator {
            String getPropertyValue(Ext2File file);
        }

        interface FilePropertiesCategoryCondition {
            boolean testFile(Ext2File file);
        }
    }

    private static final List<FilePropertiesCategory> categories = new ArrayList<>();

    static {
        FilePropertiesCategory basicProperties = new FilePropertiesCategory("Basic");
        basicProperties.addProperty("Name", Ext2File::getFileName);
        basicProperties.addProperty("Type", file -> FileModes.parseFileTypeHumanReadable(file.getFileMode()));
        basicProperties.addProperty("Size", file -> String.format(
                "%s (%s bytes)",
                ByteUtils.formatHumanReadable(file.getSize()),
                file.getSize()
        ));
        basicProperties.addSpacer();
        basicProperties.addProperty("Containing Folder", file -> file.getParentDirectory().getFilePath());
        basicProperties.addSpacer();
        basicProperties.addProperty("Creation time", file -> new Date(file.getCreationTime()).toString());
        basicProperties.addProperty("Accessed time", file -> new Date(file.getLastAccessTime()).toString());
        basicProperties.addProperty("Modified time", file -> new Date(file.getLastModifiedTime()).toString());
        basicProperties.addSpacer();
        basicProperties.addProperty("Permissions", file -> FileModes.toString(file.getFileMode()));
        categories.add(basicProperties);

        FilePropertiesCategory imageProperties = new FilePropertiesCategory(
                "Image",
                file -> file.getMimeType().map(mimeType -> mimeType.startsWith("image/")).orElse(false)
        );
        imageProperties.addProperty("Image Type", file -> file.getMimeType().map(mimeType -> mimeType.substring("image/".length())).orElse("Unknown"));
        imageProperties.addProperty("Width", file -> {
            byte[] data = file.read(0L, file.getSize());
            Image image = Toolkit.getDefaultToolkit().createImage(data);
            ImageIcon icon = new ImageIcon(image);
            return String.valueOf(icon.getIconWidth());
        });
        imageProperties.addProperty("Height", file -> {
            byte[] data = file.read(0L, file.getSize());
            Image image = Toolkit.getDefaultToolkit().createImage(data);
            ImageIcon icon = new ImageIcon(image);
            return String.valueOf(icon.getIconWidth());
        });
        categories.add(imageProperties);
    }

    private Ext2File file;

    FileProperties(Ext2File file) {
        this.file = file;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(420, 300);
        setTitle(file.getFilePath() + " Properties");
        setIconImage(Icons.PROPERTIES_IMAGE);
        populateProperties();
        setVisible(true);
    }

    private void populateProperties() {
        JPanel contentPanel = new JPanel();
        List<FilePropertiesCategory> categories = new ArrayList<>(FileProperties.categories)
                .stream()
                .filter(category -> category.condition.testFile(file))
                .collect(Collectors.toList());
        contentPanel.setLayout(new GridLayout(categories.size() + 1, 1));

        JLabel icon = new JLabel(Icons.getFileIcon(file));
        icon.setMaximumSize(new Dimension(64, 64));
        contentPanel.add(icon);

        // Iterate over the categories which match this file.
        for (FilePropertiesCategory category : categories) {
            JPanel categoryPanel = new JPanel();
            categoryPanel.setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();

            // Add JLabels for each property and value
            for (SimpleEntry<String, FilePropertyValueGenerator> entry : category.properties) {
                String value = entry.getValue().getPropertyValue(file);
                JLabel propertyLabel = new JLabel(entry.getKey().length() > 0 ? entry.getKey() + ": " : "\u200F");
                JLabel propertyValue = new JLabel(value);
                propertyLabel.setLabelFor(propertyValue);

                // add the property label
                constraints.gridwidth = GridBagConstraints.RELATIVE;
                constraints.fill = GridBagConstraints.NONE;
                constraints.weightx = 0.0;
                categoryPanel.add(propertyLabel, constraints);

                // add the property value label
                constraints.gridwidth = GridBagConstraints.REMAINDER;
                constraints.fill = GridBagConstraints.HORIZONTAL;
                constraints.weightx = 1.0;
                categoryPanel.add(propertyValue, constraints);
            }

            // Add a border with a label
            categoryPanel.setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createTitledBorder(category.label),
                            BorderFactory.createEmptyBorder(5, 5, 5, 5)
                    )
            );

            contentPanel.add(categoryPanel);
        }

        add(new JScrollPane(contentPanel));
    }
}
