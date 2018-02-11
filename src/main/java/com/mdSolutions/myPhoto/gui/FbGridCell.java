package com.mdSolutions.myPhoto.gui;

import com.mdSolutions.myPhoto.FbMediaUploader;
import com.mdSolutions.myPhoto.IndividualMedia;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import static javax.swing.SwingConstants.CENTER;

public class FbGridCell extends JPanel {

    private @Getter IndividualMedia mediaItem = null;
    private GridBagConstraints c = null;

    public FbGridCell() {
        setLayout(new GridBagLayout());
        c = new GridBagConstraints();

        setPreferredSize(new Dimension(200, 200));
        setBackground(Color.black);

        addMouseListener(initializeMouseListener());
    }

    public void addMediaItem(IndividualMedia media) {
        mediaItem = media;
        c.gridx = 0; c.gridy = 0;

        BufferedImage mediaIcon = media.view();

        if (mediaIcon != null) {
            ImageIcon icon = new ImageIcon(mediaIcon);
            JLabel iconLabel = new JLabel();
            iconLabel.setIcon(icon);
            iconLabel.setBorder(BorderFactory.createMatteBorder(1,1,1,1, Color.white));

            add(iconLabel, c);
        }
        else {
            JPanel panelImage = new JPanel();
            panelImage.setBackground(Color.black);
            panelImage.setPreferredSize(new Dimension(166, 166));
            panelImage.setBorder(BorderFactory.createMatteBorder(1,1,1,1, Color.white));

            add(panelImage, c);
        }

    }

    public void addItemName(String name) {
        c.gridx = 0; c.gridy = 1; c.insets = new Insets(10,0,0,0);  //top padding
        JLabel text = new JLabel(name);
        text.setPreferredSize(new Dimension(176, 24));
        text.setForeground(Color.white);
        text.setHorizontalAlignment(CENTER);
        add(text, c);
    }

    public MouseListener initializeMouseListener () {
        MouseListener mouseListener = new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                //right click
                if (SwingUtilities.isRightMouseButton(e)) {
                    displayRClickPopupMenu(e);
                }
            }

            public void mousePressed(MouseEvent e) {

            }

            public void mouseReleased(MouseEvent e) {

            }

            public void mouseEntered(MouseEvent e) {

            }

            public void mouseExited(MouseEvent e) {

            }
        };

        return mouseListener;
    }

    private void displayRClickPopupMenu(MouseEvent e) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem item1 = new JMenuItem("Remove");

        item1.addActionListener(a -> {
            FbMediaUploader.getInstance().removeMedia(mediaItem);
            AppGui.getInstance().getFbGridDisplayPanel().remove(this);
            AppGui.getInstance().getFbGridDisplayPanel().revalidate();
            AppGui.getInstance().getFbGridDisplayPanel().repaint();
        });

        popup.add(item1);
        popup.show(e.getComponent(), e.getX(), e.getY());
    }
}
