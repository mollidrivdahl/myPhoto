package com.mdSolutions.myPhoto.gui;

import lombok.Getter;
import lombok.Setter;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;

public class MediaItemDroppable extends PanelDroppable {

    @Getter @Setter private boolean isCollection;   //TODO: Find better way to determine the drop zone type

    public MediaItemDroppable(){

        super();
        this.isCollection = false;

        setPreferredSize(new Dimension(200, 176));
        setBackground(AppGui.MY_PURPLE);
        setLayout(new GridBagLayout());

        //have it utilize a custom transfer handler
        setTransferHandler(new MediaItemDroppable.MediaItemTransferHandler());
    }

    public MediaItemDroppable(boolean isCollection){

        super();
        this.isCollection = isCollection;

        setPreferredSize(new Dimension(200, 176));
        setBackground(Color.black);
        setLayout(new GridBagLayout());

        //have it utilize a custom transfer handler
        setTransferHandler(new MediaItemDroppable.MediaItemTransferHandler());
    }

    class MediaItemTransferHandler extends MyTransferHandler {
        //execute desired operations with imported data upon dropping
        @Override
        public boolean importData(JComponent comp, Transferable t, Point p) {
            System.out.println("media item importData");

            PanelDraggable destPanel = (PanelDraggable)t;   //Note: This is NOT the original instance that was dragged and dropped

            //TODO: get drop location and either reorganize items or move into collection
            System.out.println(p.getLocation());


            //will refresh the ui for all the changes on any component (apparently)
            //revalidate();
            //repaint();

            return true;
        }
    }
}
