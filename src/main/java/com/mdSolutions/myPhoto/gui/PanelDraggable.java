package com.mdSolutions.myPhoto.gui;

import com.mdSolutions.myPhoto.MediaCollection;
import com.mdSolutions.myPhoto.MediaItem;
import com.mdSolutions.myPhoto.MyPhoto;
import com.mdSolutions.myPhoto.PhotoMedia;
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
        appInstance.getCenterScrollPane().setBorder(BorderFactory.createMatteBorder(5,5,5,5, AppGui.MY_RED));
        appInstance.getCenterScrollPane().setViewportView(appInstance.getMediaViewPanel());
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
