package com.mdSolutions.myPhoto.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.IOException;

public class PanelDroppable extends JPanel implements DropTargetListener {

    private DropTarget target;

    //initialize the JTable with the data
    public PanelDroppable(/*int r, int c, Object[][] data*/){
        //mark this a DropTarget
        target = new DropTarget(this,this);

        //have it utilize a custom transfer handler
        setTransferHandler(new MyTransferHandler());
    }

    public void dragEnter(DropTargetDragEvent dtde) {}
    public void dragOver(DropTargetDragEvent dtde) {}
    public void dropActionChanged(DropTargetDragEvent dtde) {}
    public void dragExit(DropTargetEvent dte) {}

    //This is what happens when a Drop occurs
    public void drop(DropTargetDropEvent dtde) {
        try {
            System.out.println("drop");
            //get the Point where the drop occurred
            Point loc = dtde.getLocation();

            //get Transfer data
            Transferable t = dtde.getTransferable();

            //get the Data flavors transferred with the Transferable
            DataFlavor[] d = t.getTransferDataFlavors();

            PanelDraggable tempPanel = (PanelDraggable)t.getTransferData(d[0]);

            //and if the DataFlavors match for the PanelDroppable 
            if(getTransferHandler().canImport(this, d)){

                //then import the Draggable JComponent and repaint() the JPanel
                ((MyTransferHandler)getTransferHandler()).importData(this, tempPanel, loc);
                repaint();
            }
            else return;

        } catch (UnsupportedFlavorException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        finally{ dtde.dropComplete(true); }
    }

    class MyTransferHandler extends TransferHandler{

        //tests for a valid PanelDraggable DataFlavor
        public boolean canImport(JComponent c, DataFlavor[] f){
            DataFlavor temp = new DataFlavor(PanelDraggable.class, "JButton");
            for(DataFlavor d:f){
                if(d.equals(temp))
                    return true;

            }
            return false;
        }

        //add the data into the destination PanelDroppable
        public boolean importData(JComponent comp, Transferable t, Point p){
            try {
                System.out.println("importData");
                PanelDraggable destPanel = (PanelDraggable)t.getTransferData(new DataFlavor(PanelDraggable.class, "JPanel"));
                System.out.println(destPanel.getIndex());
                System.out.println(p.x + " " + p.y);
                System.out.println(comp.getPreferredSize().width);

            } catch (UnsupportedFlavorException ex) {

            } catch (IOException ex) {

            }
            return true;
        }

    }
}
