package com.mdSolutions.myPhoto.gui;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;


/**
 * Custom JPanel class that is Draggable.
 * This JPanel is Transferable (so it can be Dragged),
 * And listens for its own drags
 * */
public class PanelDraggable extends JPanel implements Transferable,
        DragSourceListener, DragGestureListener {

    //marks this JPanel as the source of the Drag
    private DragSource source;
    private TransferHandler t;
    private int index;

    public int getIndex() { return index; }
    public void setIndex(int val) { index = val; }

    public PanelDraggable(){
        //The TransferHandler returns a new PanelDraggable
        //to be transferred in the Drag
        t = new TransferHandler(){

            public Transferable createTransferable(JComponent c){
                return new PanelDraggable();
            }
        };
        setTransferHandler(t);

        //The Drag will copy the PanelDraggable rather than moving it
        source = new DragSource();
        source.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);
        index = -1;
    }

    //The DataFlavor is a marker to let the DropTarget know how to
    //handle the Transferable
    public DataFlavor[] getTransferDataFlavors() {
        System.out.println("getTransferDataFlavors");
        return new DataFlavor[]{new DataFlavor(PanelDraggable.class, "JPanel")};
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        System.out.println("isDataFlavorSupported");
        return true;
    }

    public Object getTransferData(DataFlavor flavor) {
        System.out.println("getTransferData");
        return this;
    }

    public void dragEnter(DragSourceDragEvent dsde) { System.out.println("dragEnter"); }
    public void dragOver(DragSourceDragEvent dsde) { System.out.println("dragOver"); }
    public void dropActionChanged(DragSourceDragEvent dsde) { System.out.println("dropActionChanged"); }
    public void dragExit(DragSourceEvent dse) { System.out.println("dragExit"); }

    //when the drag finishes,
    public void dragDropEnd(DragSourceDropEvent dsde) {
        System.out.println("dragDropEnd");

    }

    //when a DragGesture is recognized, initiate the Drag
    public void dragGestureRecognized(DragGestureEvent dge) {
        System.out.println("dragGestureRecognized");
        source.startDrag(dge, DragSource.DefaultMoveDrop, this, this);
    }

}//end outer class
