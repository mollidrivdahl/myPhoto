package com.mdSolutions.myPhoto.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.IOException;

public abstract class PanelDroppable extends JPanel implements DropTargetListener {

    protected DropTarget target;

    protected PanelDroppable() {
        //mark this a DropTarget
        target = new DropTarget(this,this);
    }

    public void dragEnter(DropTargetDragEvent dtde) {}
    public void dragOver(DropTargetDragEvent dtde) {}
    public void dropActionChanged(DropTargetDragEvent dtde) {}
    public void dragExit(DropTargetEvent dte) {}

    //This is what happens when a Drop occurs
    public void drop(DropTargetDropEvent dtde) {
        //try {
            Point loc = dtde.getLocation();                 //get the Point where the drop occurred
            Transferable t = dtde.getTransferable();        //get Transfer data
            DataFlavor[] d = t.getTransferDataFlavors();    //get the Data flavors transferred with the Transferable

            //PanelDraggable tempDraggable = (PanelDraggable)t.getTransferData(d[0]);

            //and if the DataFlavors match for the PanelDroppable
            if(getTransferHandler().canImport(this, d)){

                //then import the Draggable JComponent
                ((MyTransferHandler)getTransferHandler()).importData(this, null, loc);
            }
            else return;

        /*} catch (UnsupportedFlavorException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        finally{ dtde.dropComplete(true); }*/
    }

    protected abstract class MyTransferHandler extends TransferHandler{

        //tests for a valid PanelDraggable DataFlavor
        public boolean canImport(JComponent c, DataFlavor[] f){
            DataFlavor temp = new DataFlavor(PanelDraggable.class, "JButton");
            for(DataFlavor d:f){
                if(d.equals(temp))
                    return true;

            }
            return false;
        }

        public boolean importData(JComponent comp, Transferable t, Point p) {
            System.out.println("Default execution of importData method");
            return true;
        }
    }
}

