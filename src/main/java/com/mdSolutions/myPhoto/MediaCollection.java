package com.mdSolutions.myPhoto;

import lombok.Getter;
import lombok.Setter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

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
//    public void addMedia(ArrayList<MediaItem> newMedia) {
//        if (listOfChildren.size() == 0) {
//            headItem = newMedia.get(0);
//            newMedia.get(0).previusItem = null;
//        }
//        else
//        {
//            tailItem.nextItem = newMedia.get(0);
//            newMedia.get(0).previusItem = tailItem;
//        }
//
//        //TODO: iterate through newMedia connecting pointers and adjusting relPath, parentId, parentCollectionPath, levelNum
//        //TODO: last item in newMedia should point to null and become new tailItem
//    }

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

    public void organizeAutomatically(MyPhoto.AUTO_ORGANIZE_BY format) {
        ArrayList<MediaItem> tempReorder = null;
        Comparator<MediaItem> compareName = Comparator.comparing(MediaItem::getName);

        switch (format){
            case NAME_ASCENDING:
            {
                tempReorder = new ArrayList<>(listOfChildren);
                tempReorder.sort(compareName);
                clearChildrenAndReorder(tempReorder, true);
                break;
            }
            case NAME_DESCENDING:
            {
                tempReorder = new ArrayList<>(listOfChildren);
                tempReorder.sort(compareName);
                clearChildrenAndReorder(tempReorder, false);
                break;
            }
            case COLLECTIONS_FIRST:
            {
                //ordered collections -> photos -> videos -> unsupported
                tempReorder = new ArrayList<>();
                listOfChildren.stream().filter(i -> i instanceof MediaCollection).forEach(tempReorder::add);
                listOfChildren.stream().filter(i -> i instanceof PhotoMedia).forEach(tempReorder::add);
                listOfChildren.stream().filter(i -> i instanceof VideoMedia).forEach(tempReorder::add);
                listOfChildren.stream().filter(i -> i instanceof UnsupportedMedia).forEach(tempReorder::add);

                clearChildrenAndReorder(tempReorder, true);
                break;
            }
            case COLLECTIONS_LAST:
            {
                //ordered photos -> videos -> unsupported -> collections
                tempReorder = new ArrayList<>();
                listOfChildren.stream().filter(i -> i instanceof PhotoMedia).forEach(tempReorder::add);
                listOfChildren.stream().filter(i -> i instanceof VideoMedia).forEach(tempReorder::add);
                listOfChildren.stream().filter(i -> i instanceof UnsupportedMedia).forEach(tempReorder::add);
                listOfChildren.stream().filter(i -> i instanceof MediaCollection).forEach(tempReorder::add);

                clearChildrenAndReorder(tempReorder, true);
                break;
            }
        }
    }

    private void clearChildrenAndReorder(ArrayList<MediaItem> sortedItems, boolean isForwardSort) {
        listOfChildren.clear();
        headItem = null;
        tailItem = null;

        //iterate in order, adding items to listOfChildren and connecting next and previous items
        if (isForwardSort) {
            for (int i = 0; i < sortedItems.size(); i++)
            {
                MediaItem curMedia = sortedItems.get(i);
                System.out.println(curMedia.getName());

                if (i == 0) {
                    headItem = curMedia;
                    curMedia.previusItem = null;
                    curMedia.nextItem = (i != sortedItems.size() - 1) ? sortedItems.get(i + 1) : null;
                }
                else if (i == sortedItems.size() - 1) {
                    tailItem = curMedia;
                    curMedia.nextItem = null;
                    curMedia.previusItem = (i != 0) ? sortedItems.get(i - 1) : null;
                }
                else {
                    curMedia.nextItem = sortedItems.get(i + 1);
                    curMedia.previusItem = sortedItems.get(i - 1);
                }

                listOfChildren.add(curMedia);
            }
        }
        //iterate in backwards order, adding items to listOfChildren and connecting next and previous items
        else {
            for (int i = sortedItems.size() - 1; i >= 0; i--) {
                MediaItem curMedia = sortedItems.get(i);
                System.out.println(sortedItems.get(i).getName());

                if (i == sortedItems.size() - 1) {
                    headItem = curMedia;
                    curMedia.previusItem = null;
                    curMedia.nextItem = (i != 0) ? sortedItems.get(i - 1) : null;
                }
                else if (i == 0) {
                    tailItem = curMedia;
                    curMedia.nextItem = null;
                    curMedia.previusItem = (i != sortedItems.size() - 1) ? sortedItems.get(i + 1) : null;
                }
                else {
                    curMedia.nextItem = sortedItems.get(i - 1);
                    curMedia.previusItem = sortedItems.get(i + 1);
                }

                listOfChildren.add(curMedia);
            }
        }
    }

    public void moveMedia(MediaCollection destCollection) {
        MediaItem travel = headItem;
        MediaItem priorUnselected = null;

        //connect non-selected items, add each selected item to destination collection,
        //& remove each selected item from currentCollection
        while (travel != null) {
            if (travel.isSelected) {
                if (travel == headItem)
                    headItem = null;
                if (travel == tailItem)
                    tailItem = priorUnselected;

                listOfChildren.remove(travel);

                MediaItem nextInList = travel.nextItem;
                destCollection.addMedia(travel);

                travel = nextInList;
            }
            else {
                if (priorUnselected == null)
                    headItem = travel;
                else
                    priorUnselected.nextItem = travel;

                travel.previusItem = priorUnselected;
                priorUnselected = travel;

                travel = travel.nextItem;
            }
        }

        if (priorUnselected != null)
            priorUnselected.nextItem = null;

        tailItem = priorUnselected;

        unselectAllChildren();
    }
}
