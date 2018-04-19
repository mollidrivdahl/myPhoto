package com.mdSolutions.myPhoto.gui;

import com.mdSolutions.myPhoto.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FbShareFolderDroppable extends PanelDroppable {

    public FbShareFolderDroppable(){

        super();

        setPreferredSize(new Dimension(100, 100));
        setBackground(Color.DARK_GRAY);
        displayImage();

        //have it utilize a custom transfer handler
        setTransferHandler(new FbShareFolderDroppable.MediaItemTransferHandler());
        addMouseListener(initializeMouseListener());
    }

    private void displayImage() {
        try {
            BufferedImage image = ImageIO.read(new File("resources/fbImage.png"));

            // creates output image
            BufferedImage outImage = new BufferedImage(100, 100, image.getType());

            // scales the input image to the output image
            Graphics2D g2d = outImage.createGraphics();
            g2d.drawImage(image, 0, 0, 100, 100, null);
            g2d.dispose();

            ImageIcon icon = new ImageIcon(outImage);
            JLabel iconLabel = new JLabel();
            iconLabel.setIcon(icon);

            add(iconLabel);
        }
        catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void copyMediaToFbShareFolder() {
        AppGui.getInstance().getMyPhoto().copyToFacebook();

        AppGui.getInstance().getMyPhoto().getCurrentCollection().unselectAllChildren();
        for (Component gridCell : AppGui.getInstance().getGridViewPanel().getComponents()) {
            ((MediaItemDroppable)((GridCell) gridCell).getDropZonePanel()).getPanelDraggable().resetBorder();
        }
    }

    public MouseListener initializeMouseListener () {
        MouseListener mouseListener = new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                //double click
                if (e.getClickCount() == 2 && !e.isConsumed()) {
                    e.consume();

                    displayRClickPopupMenu(e);
                }
                //right click
                else if (SwingUtilities.isRightMouseButton(e)) {
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
        JMenuItem item1 = new JMenuItem("View FB Photo Share-Folder");
        JMenuItem item2 = new JMenuItem("View FB Video Share-Folder");

        item1.addActionListener(a -> displayPhotoShareFolder());
        item2.addActionListener(a -> displayVideoShareFolder());

        popup.add(item1);
        popup.add(item2);
        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    private void displayPhotoShareFolder() {
        final MouseAdapter mouseAdapter = new MouseAdapter() {};
        RootPaneContainer root = (RootPaneContainer)App.frame.getRootPane().getTopLevelAncestor();
        root.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        root.getGlassPane().addMouseListener(mouseAdapter);
        root.getGlassPane().setVisible(true);

        AppGui guiInstance = AppGui.getInstance();

        //load fb grid display panel with photos
        ArrayList<IndividualMedia> photoMedia = new ArrayList<>(FbMediaUploader.getInstance().getPhotos());
        populateFbGridDisplay(guiInstance, photoMedia);

        //place the entire fb view panel into the center scroll pane's viewport (swapping out the grid view panel)
        guiInstance.getCenterScrollPane().setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        guiInstance.getCenterScrollPane().setBorder(BorderFactory.createMatteBorder(0,0,0,0, Color.white));
        guiInstance.getCenterScrollPane().setViewportView(guiInstance.getFbViewPanel());

        FbMediaUploader.getInstance().setUploadType(FbMediaUploader.MEDIA_TYPE.PHOTOS);

        root.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        root.getGlassPane().addMouseListener(mouseAdapter);
        root.getGlassPane().setVisible(false);
    }

    private void displayVideoShareFolder() {
        final MouseAdapter mouseAdapter = new MouseAdapter() {};
        RootPaneContainer root = (RootPaneContainer)App.frame.getRootPane().getTopLevelAncestor();
        root.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        root.getGlassPane().addMouseListener(mouseAdapter);
        root.getGlassPane().setVisible(true);

        AppGui guiInstance = AppGui.getInstance();

        //load fb grid display panel with photos
        ArrayList<IndividualMedia> videoMedia = new ArrayList<>(FbMediaUploader.getInstance().getVideos());
        populateFbGridDisplay(guiInstance, videoMedia);

        //place the entire fb view panel into the center scroll pane's viewport (swapping out the grid view panel)
        guiInstance.getCenterScrollPane().setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        guiInstance.getCenterScrollPane().setBorder(BorderFactory.createMatteBorder(0,0,0,0, Color.white));
        guiInstance.getCenterScrollPane().setViewportView(guiInstance.getFbViewPanel());

        FbMediaUploader.getInstance().setUploadType(FbMediaUploader.MEDIA_TYPE.VIDEOS);

        root.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        root.getGlassPane().addMouseListener(mouseAdapter);
        root.getGlassPane().setVisible(false);
    }

    public void populateFbGridDisplay(AppGui guiInstance, ArrayList<IndividualMedia> mediaItems) {
        JPanel fbGridDisplayPanel = guiInstance.getFbGridDisplayPanel();
        fbGridDisplayPanel.removeAll();

        for (IndividualMedia media : mediaItems) {
            appendToFbGridDisplay(fbGridDisplayPanel, media);
        }

        fbGridDisplayPanel.revalidate();
        fbGridDisplayPanel.repaint();
    }

    public void appendToFbGridDisplay(JPanel fbGridDisplayPanel, IndividualMedia media) {

        FbGridCell gridCell = new FbGridCell();

        gridCell.addMediaItem(media);
        gridCell.addItemName(media.getName());

        //add grid cell to grid view
        fbGridDisplayPanel.add(gridCell);
    }

    class MediaItemTransferHandler extends MyTransferHandler {
        //execute desired operations with imported data upon dropping
        @Override
        public boolean importData(JComponent comp, Transferable t, Point p) {

            final MouseAdapter mouseAdapter = new MouseAdapter() {};
            RootPaneContainer root = (RootPaneContainer)App.frame.getRootPane().getTopLevelAncestor();
            root.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            root.getGlassPane().addMouseListener(mouseAdapter);
            root.getGlassPane().setVisible(true);

            copyMediaToFbShareFolder();

            root.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            root.getGlassPane().addMouseListener(mouseAdapter);
            root.getGlassPane().setVisible(false);

            return true;
        }
    }
}
