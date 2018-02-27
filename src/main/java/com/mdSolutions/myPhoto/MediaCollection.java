package com.mdSolutions.myPhoto;

import lombok.Getter;
import lombok.Setter;

import javax.activity.InvalidActivityException;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import org.apache.commons.io.FileUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class MediaCollection extends MediaItem {

    @Getter @Setter private Integer coverPhotoItem;
    @Getter @Setter private ArrayList<MediaItem> listOfChildren;
    @Getter @Setter private MediaItem headItem;
    @Getter @Setter private MediaItem tailItem;

    public MediaCollection(){
        super();
        coverPhotoItem = 1;
        listOfChildren = new ArrayList<MediaItem>();
        headItem = null;
        tailItem = null;
    }

    public MediaCollection(Integer id)
    {
        super(id);
        coverPhotoItem = 1;
        listOfChildren = new ArrayList<MediaItem>();
        headItem = null;
        tailItem = null;
    }

    public MediaCollection(String name, Integer id, String relPath, MediaItem nextItem, MediaItem previusItem,
                           Integer parentId, String parentCollectionPath, int levelNum, Integer coverPhotoItem) {
        super(name, id, relPath, nextItem, previusItem, parentId, parentCollectionPath, levelNum);
        this.coverPhotoItem = coverPhotoItem;
        this.listOfChildren = new ArrayList<MediaItem>();
        headItem = null;
        tailItem = null;
    }

    @Override
    public BufferedImage view() {
        BufferedImage originalImg;
        BufferedImage coverPhotoImg = null;
        int scaledWidth = 159;  //166 full width before inner border is applied;
        int scaledHeight = 159; //166 full height before inner border is applied;

        try {
            MediaItem coverPhotoMedia;
            String coverPhotoPath = (coverPhotoItem == 1) ? "resources/myPhotoLogo.png" : DbAccess.getInstance().getRelativePathById(coverPhotoItem);
            File coverPhotoFile = new File(coverPhotoPath);

            if ((coverPhotoMedia = MediaItem.getConcreteType(coverPhotoFile)) instanceof VideoMedia)
            {
                coverPhotoMedia.setRelPath(coverPhotoPath);
                return coverPhotoMedia.view(scaledWidth, scaledHeight);
            }
            else
                originalImg = ImageIO.read(coverPhotoFile);

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

    @Override
    public BufferedImage view (int width, int height) {
        BufferedImage originalImg;
        BufferedImage coverPhotoImg = null;
        int scaledWidth = width;
        int scaledHeight = height;

        try {
            MediaItem coverPhotoMedia;
            String coverPhotoPath = DbAccess.getInstance().getRelativePathById(coverPhotoItem);
            File coverPhotoFile = new File(coverPhotoPath);

            if ((coverPhotoMedia = MediaItem.getConcreteType(coverPhotoFile)) instanceof VideoMedia)
            {
                coverPhotoMedia.setRelPath(coverPhotoPath);
                return coverPhotoMedia.view(scaledWidth, scaledHeight);
            }
            else
                originalImg = ImageIO.read(coverPhotoFile);

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

    public void moveMedia(MediaCollection destCollection) throws InvalidActivityException {
        MediaItem travel = headItem;
        MediaItem priorUnselected = null;
        String newDestinationPath;

        //for each selected item of this current collection, check if file by that name already exists in destination file directory
        Stream<MediaItem> selectedMedia = listOfChildren.stream().filter(MediaItem::isSelected);
        for (MediaItem media : (Iterable<MediaItem>) selectedMedia::iterator) {
            if (media instanceof MediaCollection)
                newDestinationPath = destCollection.relPath + media.name + "/";
            else
                newDestinationPath = destCollection.relPath + media.name;

            if (MyPhoto.FileSystemAccess.fileExists(newDestinationPath))
                throw new InvalidActivityException("Cannot move files - at least one file in destination collection has the same name");
        }

        //connect non-selected items, add each selected item to destination collection,
        //& remove each selected item from currentCollection
        while (travel != null) {
            if (travel.isSelected) {
                if (travel == headItem)
                    headItem = null;
                if (travel == tailItem)
                    tailItem = priorUnselected;

                listOfChildren.remove(travel);

                try {
                    //move media appropriately in file system
                    if (travel instanceof MediaCollection)
                        Files.move(new File(travel.relPath).toPath(), new File(destCollection.relPath + travel.name + "/").toPath(), REPLACE_EXISTING);
                    else
                        Files.move(new File(travel.relPath).toPath(), new File(destCollection.relPath + travel.name).toPath(), REPLACE_EXISTING);
                }
                catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }

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

    public void duplicateMedia() {
        ArrayList<MediaItem> duplicatesToAdd = new ArrayList<>();
        Stream<MediaItem> selectedMedia = listOfChildren.stream().filter(MediaItem::isSelected);

        for (MediaItem media : (Iterable<MediaItem>) selectedMedia::iterator) {
            if (media instanceof IndividualMedia) {
                //create and initialize new duplicated instance
                IndividualMedia newMedia = createDuplicate((IndividualMedia) media);

                //add to listOfChildren
                duplicatesToAdd.add(newMedia);

                //if the original media was the tail item, make newMedia the tail item
                if (media == tailItem)
                    tailItem = newMedia;
                //else set previous item of next item of original to new instance
                else
                    media.nextItem.previusItem = newMedia;

                //set next item of original instance to new instance
                media.nextItem = newMedia;

                //create duplicate in file system
                try { MyPhoto.FileSystemAccess.copyForDuplicate(new File(media.relPath), new File(newMedia.relPath)); }
                catch (InvalidActivityException ex) { System.out.println(ex.getMessage()); }
            }
        }

        listOfChildren.addAll(duplicatesToAdd);
    }

    private IndividualMedia createDuplicate(IndividualMedia originalMedia) {
        IndividualMedia newMedia;
        String ext = "";
        String nameWithoutExt = "";
        String pathWithoutExt = "";
        String newName = "";
        String newPath = "";

        //extract extension from name
        try {
            ext = originalMedia.name.substring(originalMedia.name.lastIndexOf(".") + 1);
            nameWithoutExt = originalMedia.name.substring(0, originalMedia.name.lastIndexOf("."));
            pathWithoutExt = originalMedia.relPath.substring(0, originalMedia.relPath.lastIndexOf("."));
        }
        catch (Exception ex) { System.out.println(ex.getMessage()); }

        //create new instance of duplicate media
        if (originalMedia instanceof PhotoMedia) {
            newMedia = new PhotoMedia(nameWithoutExt + " - Copy." + ext, -1, pathWithoutExt + " - Copy." + ext,
                    originalMedia.nextItem, originalMedia, originalMedia.parentId, originalMedia.parentCollectionPath, originalMedia.levelNum);
        }
        else if (originalMedia instanceof VideoMedia) {
            newMedia = new VideoMedia(nameWithoutExt + " - Copy." + ext, -1, pathWithoutExt + " - Copy." + ext,
                    originalMedia.nextItem, originalMedia, originalMedia.parentId, originalMedia.parentCollectionPath, originalMedia.levelNum);
        }
        else {
            newMedia = new UnsupportedMedia(nameWithoutExt + " - Copy." + ext, -1, pathWithoutExt + " - Copy." + ext,
                    originalMedia.nextItem, originalMedia, originalMedia.parentId, originalMedia.parentCollectionPath, originalMedia.levelNum);
        }

        //rename if other copies already exist by same name
        int count = 2;
        while (MyPhoto.FileSystemAccess.fileExists(newMedia.relPath)) {
            newMedia.name = nameWithoutExt + " - Copy (" + count + ")." + ext;
            newMedia.relPath = pathWithoutExt + " - Copy(" + count + ")." + ext;
        }

        return newMedia;
    }

    public ArrayList<MediaItem> deleteMedia() {
        ArrayList<MediaItem> mediaToDelete = new ArrayList<>();
        MediaItem travel = headItem;
        MediaItem priorUnselected = null;

        //connect non-selected items, delete each selected from file system,
        //& remove each selected item from currentCollection
        while (travel != null) {
            if (travel.isSelected) {
                if (travel == headItem)
                    headItem = null;
                if (travel == tailItem)
                    tailItem = priorUnselected;

                listOfChildren.remove(travel);
                mediaToDelete.add(travel);

                try {
                    //delete media from file system
                    if (travel instanceof MediaCollection)
                        FileUtils.deleteDirectory(new File(travel.relPath));
                    else
                        Files.deleteIfExists(new File(travel.relPath).toPath());
                }
                catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }

                travel = travel.nextItem;
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

        return mediaToDelete;
    }
}
