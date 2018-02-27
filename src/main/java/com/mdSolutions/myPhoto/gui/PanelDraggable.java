package com.mdSolutions.myPhoto.gui;

import com.mdSolutions.myPhoto.*;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Custom JPanel class that is Draggable.
 * This JPanel is listens for its own drags
 * */
public class PanelDraggable extends JPanel implements Transferable,
        DragSourceListener, DragGestureListener {

    //marks this JPanel as the source of the Drag
    private DragSource source;
    private Border border;
    @Getter @Setter private MediaItem mediaItem;
    @Getter @Setter private int index;
    @Getter @Setter private int originalIndex;

    public PanelDraggable(){

        source = new DragSource();
        source.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, this);
        mediaItem = null;
        originalIndex = -1;
        index = originalIndex;
        setPreferredSize(new Dimension(176, 176));
        setBorder(border = BorderFactory.createMatteBorder(5, 5, 5, 5, AppGui.MY_BLUE));

        addMouseListener(initializeMouseListener());
    }

    public PanelDraggable(MediaItem mediaItem, int index){

        source = new DragSource();
        source.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, this);
        this.mediaItem = mediaItem;
        originalIndex = index;
        this.index = originalIndex;

        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(176, 176));

        if (mediaItem instanceof MediaCollection)
            border = BorderFactory.createMatteBorder(5, 5, 5, 5, AppGui.MY_PURPLE);
        else
            border = BorderFactory.createMatteBorder(5, 5, 5, 5, AppGui.MY_BLUE);

        setBorder(border);
        addMouseListener(initializeMouseListener());
    }

    public void displayImage(BufferedImage image) {
        if (image == null) {
            setBackground(Color.black);
        }
        else {
            ImageIcon icon = new ImageIcon(image);
            JLabel iconLabel = new JLabel();
            iconLabel.setIcon(icon);

            if (mediaItem instanceof MediaCollection)
                iconLabel.setBorder(BorderFactory.createMatteBorder(7,7,0,0, AppGui.MY_PURPLE_DARKER));

            add(iconLabel);
        }
    }

    public void resetBorder() {
        setBorder(border);
    }

    public MouseListener initializeMouseListener () {
        MouseListener mouseListener = new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                if (AppGui.getInstance().isMultiSelect())
                {
                    //toggle selected on single click
                    if (!mediaItem.isSelected()) {
                        mediaItem.setSelected(true);
                        setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, AppGui.MY_GLOW));
                    }
                    else {
                        mediaItem.setSelected(false);
                        setBorder(border);
                    }
                }
                else {
                    //double click only allowed when multi select is disabled
                    if (e.getClickCount() == 2 && !e.isConsumed()) {
                        e.consume();

                        if (mediaItem instanceof MediaCollection)
                            navigateIntoCollection();
                        else if (mediaItem instanceof PhotoMedia) {
                            mediaItem.setSelected(true);
                            viewPhotoInWindow();
                        }
                        else if (mediaItem instanceof VideoMedia) {
                            mediaItem.setSelected(true);
                            new MyMediaPlayer((VideoMedia)mediaItem, AppGui.getInstance().getVideoPlaybackPanel());
                        }
                        else {  //unsupported media item
                            displayUnsupportedPopupMenu(e);
                        }
                    }
                    //right click
                    else if (SwingUtilities.isRightMouseButton(e)) {
                        if (!(mediaItem instanceof MediaCollection)) {
                            displayRClickPopupMenu(e);
                        }
                    }
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

    private void navigateIntoCollection() {
        MyPhoto myPhoto = AppGui.getInstance().getMyPhoto();
        myPhoto.refreshCurrentCollection(mediaItem.getId());
        AppGui.getInstance().populateGridView(myPhoto.getCurrentCollection());
    }

    private void viewPhotoInWindow() {
        AppGui appInstance = AppGui.getInstance();

        //place photo playback panel into the media playback panel
        appInstance.getMediaPlaybackPanel().add(appInstance.getPhotoPlaybackPanel());

        //place the image into the media display panel
        JLabel iconLabel = new JLabel();
        iconLabel.setIcon(((PhotoMedia)mediaItem).getImage());
        appInstance.getMediaDisplayPanel().add(iconLabel);

        //place the entire media view panel into the center scroll pane's viewport (swapping out the grid view panel)
        appInstance.getCenterScrollPane().setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        appInstance.getCenterScrollPane().setBorder(BorderFactory.createMatteBorder(0,0,0,0, Color.white));
        appInstance.getCenterScrollPane().setViewportView(appInstance.getMediaViewPanel());
    }

    private void displayRClickPopupMenu(MouseEvent e) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem item1 = new JMenuItem("Open with Default App");
        JMenuItem item2 = new JMenuItem("Open in Alt App...");
        JMenuItem item3 = new JMenuItem("Set as Cover Photo for...");

        item1.addActionListener(a ->  {
            try {
                Desktop.getDesktop().open(new File(mediaItem.getRelPath()));
            }
            catch (IOException ex) {
                System.out.println(ex);
            }
        });
        item2.addActionListener(a -> AppGui.getInstance().openAltApplication(mediaItem));
        item3.addActionListener(a -> {
            JPopupMenu popup2 = new JPopupMenu();
            ArrayList<MediaCollection> parents = AppGui.getInstance().getMyPhoto().setCoverPhoto(mediaItem);

            for (MediaCollection parent : parents) {
                JMenuItem newItem = new JMenuItem("Collection: " + parent.getName() + ", Level: " + parent.getLevelNum());
                newItem.addActionListener(c -> {
                    parent.setCoverPhotoPath(mediaItem.getRelPath());
                    DbAccess.getInstance().updateCoverPhoto(parent);
                });

                popup2.add(newItem);
            }

            popup2.show(e.getComponent(), e.getX(), e.getY());
        });

        popup.add(item1);
        popup.add(item2);
        popup.add(item3);
        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    private void displayUnsupportedPopupMenu(MouseEvent e) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem item1 = new JMenuItem("Open with Default App");
        JMenuItem item2 = new JMenuItem("Open in Alt App...");

        item1.addActionListener(a ->  {
            try {
                Desktop.getDesktop().open(new File(mediaItem.getRelPath()));
            }
            catch (IOException ex) {
                System.out.println(ex);
            }
        });
        item2.addActionListener(a -> AppGui.getInstance().openAltApplication(mediaItem));

        popup.add(item1);
        popup.add(item2);
        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    //The DataFlavor is a marker to let the DropTarget know how to
    //handle the Transferable
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{new DataFlavor(PanelDraggable.class, "JPanel")};
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return true;
    }

    public Object getTransferData(DataFlavor flavor) {
        return this;
    }

    public void dragEnter(DragSourceDragEvent dsde) { }
    public void dragOver(DragSourceDragEvent dsde) { }
    public void dropActionChanged(DragSourceDragEvent dsde) { }
    public void dragExit(DragSourceEvent dse) { }

    //when the drag finishes,
    public void dragDropEnd(DragSourceDropEvent dsde) {
        //if not multi-select and not a successful drop
        if (!AppGui.getInstance().isMultiSelect() && !dsde.getDropSuccess()) {
            mediaItem.setSelected(false);
            setBorder(border);
        }
    }

    //when a DragGesture is recognized, initiate the Drag
    public void dragGestureRecognized(DragGestureEvent dge) {
        mediaItem.setSelected(true);
        setBorder(BorderFactory.createMatteBorder(5,5,5,5, AppGui.MY_GLOW));
        source.startDrag(dge, DragSource.DefaultCopyDrop, this, this);
    }

}//end outer class
