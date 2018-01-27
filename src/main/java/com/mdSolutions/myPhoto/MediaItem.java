package com.mdSolutions.myPhoto;

import lombok.Getter;
import lombok.Setter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

public abstract class MediaItem implements Serializable{

    //TODO: update supported types
    static final String[] supportedPhotoTypes = new String[] { "jpg", "jpeg", "png" };
    static final String[] supportedVideoTypes = new String[] { "mp4", "wav" };

    @Getter @Setter protected String name;
    @Getter @Setter protected Integer id;
    @Getter @Setter protected String relPath;
    @Getter @Setter protected MediaItem nextItem;
    @Getter @Setter protected MediaItem previusItem;
    @Getter @Setter protected Integer parentId;
    @Getter @Setter protected String parentCollectionPath;
    @Getter @Setter protected int levelNum;
    @Getter @Setter protected boolean isSelected;

    protected MediaItem() {
        this.name = "";
        this.id = -1;
        this.relPath = "";
        this.nextItem = null;
        this.previusItem = null;
        this.parentId = null;
        this.parentCollectionPath = null;
        this.levelNum = -1;
        this.isSelected = false;
    }

    protected MediaItem(Integer id) {
        this.name = "";
        this.id = id;
        this.relPath = "";
        this.nextItem = null;
        this.previusItem = null;
        this.parentId = null;
        this.parentCollectionPath = null;
        this.levelNum = -1;
        this.isSelected = false;
    }

    protected MediaItem(String name, Integer id, String relPath, MediaItem nextItem, MediaItem previusItem,
                        Integer parentId, String parentCollectionPath, int levelNum) {
        this.name = name;
        this.id = id;
        this.relPath = relPath;
        this.nextItem = nextItem;
        this.previusItem = previusItem;
        this.parentId = parentId;
        this.parentCollectionPath = parentCollectionPath;
        this.levelNum = levelNum;
        this.isSelected = false;
    }

    public abstract BufferedImage view();

    static MediaItem getConcreteType(String typeIndicator) {
        if (typeIndicator.equals("Photo"))
            return new PhotoMedia();
        else if (typeIndicator.equals("Video"))
            return new VideoMedia();
        else if (typeIndicator.equals("Unsupported"))
            return new UnsupportedMedia();
        else
            return new MediaCollection();
    }

    //TODO: implement
    static MediaItem getConcreteType(File file) {
        String name = file.getName();
        String ext;

        //extract extension from name
        try {
             ext = name.substring(name.lastIndexOf(".") + 1);
        }
        catch (Exception e) {
            ext = null;
        }

        //check extension against supported types
        if (ext != null) {
            if (Arrays.asList(supportedPhotoTypes).contains(ext))
                return new PhotoMedia();
            else if (Arrays.asList(supportedVideoTypes).contains(ext))
                return new VideoMedia();
        }

        return new UnsupportedMedia();
    }

    static String getConcreteType(MediaItem media) {
        if (media instanceof MediaCollection)
            return "Collection";
        else if (media instanceof PhotoMedia)
            return "Photo";
        else if (media instanceof VideoMedia)
            return "Video";
        else
            return "Unsupported";
    }
}
