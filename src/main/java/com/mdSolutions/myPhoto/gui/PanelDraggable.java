package com.mdSolutions.myPhoto.gui;

import com.mdSolutions.myPhoto.MediaCollection;
import com.mdSolutions.myPhoto.MediaItem;
import com.mdSolutions.myPhoto.MyPhoto;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
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
        setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, AppGui.MY_BLUE));

        addMouseListener(initializeMouseListener());
    }

    public PanelDraggable(MediaItem mediaItem, int index){

        source = new DragSource();
        source.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, this);
        this.mediaItem = mediaItem;
        originalIndex = index;
        this.index = originalIndex;

        setPreferredSize(new Dimension(176, 176));
        setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, AppGui.MY_BLUE));

        addMouseListener(initializeMouseListener());
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
        //TODO: always mark as unselected once drop ends? Or do that elsewhere?
    }

    //when a DragGesture is recognized, initiate the Drag
    public void dragGestureRecognized(DragGestureEvent dge) {
        source.startDrag(dge, DragSource.DefaultCopyDrop, this, this);
    }

    public void displayImage(BufferedImage image) {
        ImageIcon icon = new ImageIcon(image);
        JLabel iconLabel = new JLabel();
        iconLabel.setIcon(icon);
        add(iconLabel);
    }

    public MouseListener initializeMouseListener () {
        MouseListener mouseListener = new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                //TODO: mark as selected on single click

                //double click
                if (e.getClickCount() == 2 && !e.isConsumed()) {
                    e.consume();

                    if (mediaItem instanceof MediaCollection)
                        navigateIntoCollection();
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

}//end outer class
