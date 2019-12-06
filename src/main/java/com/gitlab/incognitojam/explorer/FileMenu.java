package com.gitlab.incognitojam.explorer;

import com.gitlab.incognitojam.ext2.Ext2File;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FileMenu extends JPopupMenu {
    FileMenu(Ext2Explorer explorer, Ext2File file) {
        add(new AbstractAction(file.isDirectory() ? "Explore" : "Open") {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                explorer.openFile(file);
            }
        });
        if (file.isRegularFile()) add(new AbstractAction("View Hexdump") {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                new HexViewer(file);
            }
        });
        add(new AbstractAction("Properties") {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                new FileProperties(file);
            }
        });
    }

    public static class FileMenuListener extends MouseAdapter {
        private final Ext2Explorer explorer;
        private final Ext2File file;

        FileMenuListener(Ext2Explorer explorer, Ext2File file) {
            this.explorer = explorer;
            this.file = file;
        }

        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger())
                doPop(e);
        }

        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger())
                doPop(e);
        }

        private void doPop(MouseEvent e) {
            FileMenu menu = new FileMenu(explorer, file);
            menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }
}
