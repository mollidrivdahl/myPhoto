package com.mdSolutions.myPhoto;

import lombok.Getter;
import lombok.Setter;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class MediaCollection extends MediaItem {

    @Getter @Setter private String coverPhotoPath;
    @Getter @Setter private ArrayList<MediaItem> listOfChildren;

    public MediaCollection(){ }

    public MediaCollection(String name, int id, String relPath, MediaItem nextItem, MediaItem previusItem,
                           Integer parentId, String parentCollectionPath, int levelNum, String coverPhotoPath) {
        super(name, id, relPath, nextItem, previusItem, parentId, parentCollectionPath, levelNum);
        this.coverPhotoPath = coverPhotoPath;
    }

    //TODO:Implement
    protected BufferedImage View() {
        return null;
    }
}
