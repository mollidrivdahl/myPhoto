package com.mdSolutions.myPhoto.gui;

import javax.swing.*;
import java.awt.*;

public class GridCell extends JPanel {

    private GridBagConstraints c = null;
    private PanelDroppable dropPanel;

    public GridCell() {
        setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        setBackground(Color.BLACK);
    }

    public void addDropZone(PanelDroppable dropPanel) {
        this.dropPanel = dropPanel;
        c.gridx = 0; c.gridy = 0;
        add(dropPanel, c);
    }

    public void addItemName(String name) {
        c.gridx = 0; c.gridy = 1; c.insets = new Insets(10,0,0,0);  //top padding
        JTextField text = new JTextField(name);
        text.setPreferredSize(new Dimension(176, 25));
        add(text, c);
    }

    public PanelDroppable getDropZonePanel() {
        return this.dropPanel;
    }
}
