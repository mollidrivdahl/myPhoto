package com.mdSolutions.myPhoto.gui;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;


/**
 * Custom JPanel class that is Draggable.
 * This JPanel is listens for its own drags
 * */
public class PanelDraggable extends JPanel implements Transferable,
        DragSourceListener, DragGestureListener {

    //marks this JPanel as the source of the Drag
    private DragSource source;
    //@Getter @Setter private MediaItem mediaItem;
    @Getter @Setter private int index;
    @Getter @Setter private int originalIndex;

    public PanelDraggable(){

        source = new DragSource();
        source.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, this);
        originalIndex = -1;
        index = originalIndex;
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

}//end outer class
