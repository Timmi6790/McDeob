package com.shanebeestudios.mcdeop.app.components;

import com.shanebeestudios.mcdeop.VersionManager;
import com.shanebeestudios.mcdeop.launchermeta.data.version.Version;
import java.awt.*;
import java.util.Collection;
import javax.swing.*;

public class VersionBox extends JComboBox<Version> {
    public VersionBox() {
        this.setRenderer(new VersionRenderer());

        for (final Version version : this.getVersions()) {
            this.addItem(version);
        }

        this.setSelectedIndex(0);
        this.setBackground(Color.lightGray);
    }

    private Collection<Version> getVersions() {
        return VersionManager.getInstance().getVersions();
    }

    private static class VersionRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                final JList list,
                final Object value,
                final int index,
                final boolean isSelected,
                final boolean cellHasFocus) {
            final JLabel label =
                    (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            final Version version = (Version) value;
            label.setText(version.getId());
            return label;
        }
    }
}