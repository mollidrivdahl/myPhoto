package com.mdSolutions.myPhoto;

import lombok.Getter;
import lombok.Setter;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class MediaCollection extends MediaItem {

    @Getter @Setter private String coverPhotoPath;
    @Getter @Setter private ArrayList<MediaItem> listOfChildren;
    @Getter @Setter private MediaItem headItem;
    @Getter @Setter private MediaItem tailItem;

    public MediaCollection(){
        super();
        coverPhotoPath = null;
        listOfChildren = new ArrayList<MediaItem>();
        headItem = null;
        tailItem = null;
    }

    public MediaCollection(String name, Integer id, String relPath, MediaItem nextItem, MediaItem previusItem,
                           Integer parentId, String parentCollectionPath, int levelNum, String coverPhotoPath) {
        super(name, id, relPath, nextItem, previusItem, parentId, parentCollectionPath, levelNum);
        this.coverPhotoPath = coverPhotoPath;
        this.listOfChildren = new ArrayList<MediaItem>();
        headItem = null;    //ADDED
        tailItem = null;    //ADDED
    }

    //TODO:Implement
    @Override
    protected BufferedImage view() {
        return null;
    }

    public void addMedia(MediaItem newMedia) {          //ADDED
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
        newMedia.relPath = relPath + newMedia.name + "/";
        newMedia.levelNum = levelNum + 1;

        listOfChildren.add(newMedia);
    }

    //newMedia in correct order upon entering this method
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
}
