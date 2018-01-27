package com.mdSolutions.myPhoto;

import lombok.Getter;
import lombok.Setter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.stream.Stream;

public class MediaCollection extends MediaItem {

    @Getter @Setter private String coverPhotoPath;
    @Getter @Setter private ArrayList<MediaItem> listOfChildren;
    @Getter @Setter private MediaItem headItem;
    @Getter @Setter private MediaItem tailItem;

    public MediaCollection(){
        super();
        coverPhotoPath = "";
        listOfChildren = new ArrayList<MediaItem>();
        headItem = null;
        tailItem = null;
    }

    public MediaCollection(Integer id)
    {
        super(id);
        coverPhotoPath = "";
        listOfChildren = new ArrayList<MediaItem>();
        headItem = null;
        tailItem = null;
    }

    public MediaCollection(String name, Integer id, String relPath, MediaItem nextItem, MediaItem previusItem,
                           Integer parentId, String parentCollectionPath, int levelNum, String coverPhotoPath) {
        super(name, id, relPath, nextItem, previusItem, parentId, parentCollectionPath, levelNum);
        this.coverPhotoPath = coverPhotoPath;
        this.listOfChildren = new ArrayList<MediaItem>();
        headItem = null;
        tailItem = null;
    }

    @Override
    public BufferedImage view() {
        BufferedImage originalImg;
        BufferedImage coverPhotoImg = null;
        int scaledWidth = 165;
        int scaledHeight = 150;

        try {
            originalImg = ImageIO.read(new File(coverPhotoPath));

            // creates output image
            coverPhotoImg = new BufferedImage(scaledWidth, scaledHeight, originalImg.getType());

            // scales the input image to the output image
            Graphics2D g2d = coverPhotoImg.createGraphics();
            g2d.drawImage(originalImg, 0, 0, scaledWidth, scaledHeight, null);
            g2d.dispose();
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return coverPhotoImg;
    }

    public void addMedia(MediaItem newMedia) {
        if (listOfChildren.size() == 0) {
            headItem = newMedia;
            newMedia.previusItem = null;
        }
        else
        {
            tailItem.nextItem = newMedia;
            newMedia.previusItem = tailItem;
        }

        tailItem = newMedia;
        newMedia.nextItem = null;
        newMedia.parentId = id;
        newMedia.parentCollectionPath = relPath;

        if (newMedia instanceof MediaCollection)
            newMedia.relPath = relPath + newMedia.name + "/";
        else
            newMedia.relPath = relPath + newMedia.name;

        newMedia.levelNum = levelNum + 1;

        listOfChildren.add(newMedia);
    }

    //newMedia in correct order (from first to last index) upon entering this method
    public void addMedia(ArrayList<MediaItem> newMedia) {
        if (listOfChildren.size() == 0) {
            headItem = newMedia.get(0);
            newMedia.get(0).previusItem = null;
        }
        else
        {
            tailItem.nextItem = newMedia.get(0);
            newMedia.get(0).previusItem = tailItem;
        }

        //TODO: iterate through newMedia connecting pointers and adjusting relPath, parentId, parentCollectionPath, levelNum
        //TODO: last item in newMedia should point to null and become new tailItem
    }

    public void unselectAllChildren() {
        for (MediaItem item: listOfChildren) {
            item.isSelected = false;
        }
    }

    public void organizeManually(MediaItem leftConnection, MediaItem rightConnection) {
        MediaItem travel = headItem;
        MediaItem firstSelected = null;
        MediaItem priorSelected = null;
        MediaItem firstNonSelectedAfterFirstSelected = null;
        MediaItem lastNonSelectedBeforePriorSelected = null;
        MediaItem firstNonSelectedAfterRightConnection = null;
        MediaItem lastNonSelectedBeforeLeftConnection = null;
        boolean passedRightCollection = false;

        while (travel != null) {
            if (travel.isSelected) {
                lastNonSelectedBeforePriorSelected = travel.previusItem;

                if (travel == leftConnection)
                    lastNonSelectedBeforeLeftConnection = travel.previusItem;

                //connect the non-selected items
                if (travel.previusItem != null) {
                    travel.previusItem.nextItem = travel.nextItem;
                    if (travel == tailItem)
                        tailItem = travel.previusItem;
                }
                if (travel.nextItem != null) {
                    travel.nextItem.previusItem = travel.previusItem;
                    if (travel == headItem)
                        headItem = travel.nextItem;
                }

                //connect selected item to the prior selected item
                if (firstSelected == null) {
                    firstSelected = travel;
                    priorSelected = firstSelected;
                }
                else {
                    travel.previusItem = priorSelected;
                    priorSelected.nextItem = travel;
                    priorSelected = travel;
                }
            }
            else if (firstSelected != null && firstNonSelectedAfterFirstSelected == null)
                firstNonSelectedAfterFirstSelected = travel;

            if (travel == rightConnection)
                passedRightCollection = true;
            else if (passedRightCollection && !travel.isSelected && firstNonSelectedAfterRightConnection == null)
                firstNonSelectedAfterRightConnection = travel;

            travel = travel.nextItem;   //next is still the next item in the original order
        }

        //find first non-selected right connection
        if (firstSelected == rightConnection)
            rightConnection = firstNonSelectedAfterFirstSelected;
        else if (rightConnection != null && rightConnection.isSelected)
            rightConnection = firstNonSelectedAfterRightConnection;

        //find last non-selected left connection
        if (priorSelected == leftConnection)
            leftConnection = lastNonSelectedBeforePriorSelected;
        else if (leftConnection != null && leftConnection.isSelected)
            leftConnection = lastNonSelectedBeforeLeftConnection;

        //rearrange selected items between the left and right connections items
        if (firstSelected != null) {
            //if left connection is not the first selected item
            if (firstSelected != leftConnection) {
                firstSelected.previusItem = leftConnection;

                if (leftConnection != null)
                    leftConnection.nextItem = firstSelected;
                else
                    headItem = firstSelected;
            }
            //else left connection is the first selected item
            else {
                if (firstSelected.previusItem == null)
                    headItem = firstSelected;
                else
                    firstSelected.previusItem.nextItem = firstSelected;
            }
        }

        if (priorSelected != null) {
            //if right connection is not the last selected item
            if (priorSelected != rightConnection) {
                priorSelected.nextItem = rightConnection;

                if (rightConnection != null)
                    rightConnection.previusItem = priorSelected;
                else
                    tailItem = priorSelected;
            }
            //else right connection is the last selected item
            else {
                if (priorSelected.nextItem == null)
                    tailItem = priorSelected;
                else
                    priorSelected.nextItem.previusItem = priorSelected;
            }
        }

        //unselect all items
        unselectAllChildren();
    }
}
