package com.mdSolutions.myPhoto.gui;

import com.mdSolutions.myPhoto.MediaCollection;
import com.mdSolutions.myPhoto.MediaItem;

import javax.activity.InvalidActivityException;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;

public class MediaItemDroppable extends PanelDroppable {

    private PanelDraggable panelDraggable;

    public MediaItemDroppable(){

        super();

        setPreferredSize(new Dimension(200, 176));
        setBackground(Color.black);
        setLayout(new GridBagLayout());

        //have it utilize a custom transfer handler
        setTransferHandler(new MediaItemDroppable.MediaItemTransferHandler());
    }

    @Override
    public void addPanelDraggable(PanelDraggable panelDraggable) {
        this.panelDraggable = panelDraggable;
        add(panelDraggable);
    }

    @Override
    public PanelDraggable getPanelDraggable() {
        return panelDraggable;
    }


    private void handleReorganization(Point p) {
        MediaItem leftConnection = null;
        MediaItem rightConnection = null;

        //determine left and right (destination) connections
        if (p.getX() < 10) {
            //this drop target is the rightward connection
            rightConnection = panelDraggable.getMediaItem();
            leftConnection = rightConnection.getPreviusItem();
        }
        else if (p.getX() > 190) {
            //this drop target is the leftward connection
            leftConnection = panelDraggable.getMediaItem();
            rightConnection = leftConnection.getNextItem();
        }

        //reorganize the current collection data
        AppGui.getInstance().getMyPhoto().organizeManually(leftConnection, rightConnection);

        //repopulate the grid view
        AppGui.getInstance().populateGridView(AppGui.getInstance().getMyPhoto().getCurrentCollection());
    }

    private void handleMovementInto() {
        MediaItem destCollection = panelDraggable.getMediaItem();

        //if dropped media on a non-collection item
        if (!(destCollection instanceof MediaCollection)) {
            JOptionPane.showMessageDialog(null,
                    "Cannot move media - destination item must be a collection");
            return;
        }
        else if (destCollection.isSelected()) {
            JOptionPane.showMessageDialog(null,
                    "Cannot move media - destination collection cannot be selected");
            return;
        }

        try {
            AppGui.getInstance().getMyPhoto().moveMediaIn((MediaCollection)panelDraggable.getMediaItem());

            //repopulate the grid view
            AppGui.getInstance().populateGridView(AppGui.getInstance().getMyPhoto().getCurrentCollection());
        }
        catch (InvalidActivityException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }

    class MediaItemTransferHandler extends MyTransferHandler {
        //execute desired operations with imported data upon dropping
        @Override
        public boolean importData(JComponent comp, Transferable t, Point p) {
            PanelDraggable destPanel = (PanelDraggable)t;   //Note: This is NOT the original instance that was dragged and dropped

            //dropped in the "reorganize" zone
            if (p.getX() < 10 || p.getX() > 190) {
                System.out.println("reorganize zone");
                handleReorganization(p);
            }
            //dropped in the "move media into collection" zone
            else if (panelDraggable.getMediaItem() instanceof MediaCollection) {
                System.out.println("move into collection zone");
                handleMovementInto();
            }

            //TODO: find way to uncheck the multi-select JCheckBox also to uncomment following line
            //AppGui.getInstance().setMultiSelect(false);

            return true;
        }
    }
}
