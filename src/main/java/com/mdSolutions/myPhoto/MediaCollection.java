package com.mdSolutions.myPhoto;

import lombok.Getter;
import lombok.Setter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

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
}
