package com.mdSolutions.myPhoto.gui;

import com.mdSolutions.myPhoto.DbAccess;
import com.mdSolutions.myPhoto.MediaCollection;
import com.mdSolutions.myPhoto.MediaItem;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;

public class GridCell extends JPanel {

    private GridBagConstraints c = null;
    private PanelDroppable dropPanel;
    private String prevName;
    private String newName;
    private boolean isSaving = false;

    public GridCell() {
        setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        setBackground(Color.black);
    }

    public void addDropZone(PanelDroppable dropPanel) {
        this.dropPanel = dropPanel;
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        add(dropPanel, c);
    }

    public void addItemName(String name) {
        JTextField text = new JTextField(name);
        text.setPreferredSize(new Dimension(176, 25));

        JButton btnSaveName = new JButton("\u2713");
        btnSaveName.setPreferredSize(new Dimension(18, 25));
        btnSaveName.setMargin(new Insets(0,0,0,0));
        btnSaveName.setVisible(false);

        text.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (!isSaving) {
                    text.setPreferredSize(new Dimension(148, 25));
                    btnSaveName.setVisible(true);
                    prevName = text.getText();
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (!isSaving) {
                    btnSaveName.setVisible(false);
                    text.setPreferredSize(new Dimension(176, 25));
                    text.setText(prevName);
                }
            }
        });
        btnSaveName.addActionListener(e -> {
            btnSaveName.setVisible(false);
            text.setPreferredSize(new Dimension(176, 25));
            prevName = text.getText();  //now new name

            MediaItem item = ((MediaItemDroppable)dropPanel).getPanelDraggable().getMediaItem();
            File mediaFile = new File(item.getRelPath());
            item.setName(text.getText());

            if (item instanceof MediaCollection)
                item.setRelPath(item.getParentCollectionPath() + item.getName() + "/");
            else
                item.setRelPath(item.getParentCollectionPath() + item.getName());

            DbAccess.getInstance().UpdateMediaNameAndPath(item);
            System.out.println(mediaFile.renameTo(new File(item.getRelPath())));
            isSaving = false;
        }); //TODO: add error checking to avoid renaming the extension type
        btnSaveName.addChangeListener(e -> {
            ButtonModel btnModel = ((AbstractButton)e.getSource()).getModel();

            if (btnModel.isPressed()) {
                isSaving = true;
                btnModel.setPressed(false); //After this line, program jumps to execution of btn click, then comes back
            }
        }); //change listener fires before action listener

        c.gridx = 0; c.gridy = 1; c.gridwidth = 1; c.insets = new Insets(10,12,0,0);  //top padding
        add(text, c);

        c.gridx = 1; c.gridy = 1; c.gridwidth = 1; c.insets.set(0, 0 ,0,0);
        add(btnSaveName, c);
    }

    public PanelDroppable getDropZonePanel() {
        return this.dropPanel;
    }
}
