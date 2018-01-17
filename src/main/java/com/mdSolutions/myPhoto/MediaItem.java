package com.mdSolutions.myPhoto;

import lombok.Getter;
import lombok.Setter;

import java.awt.image.BufferedImage;

public abstract class MediaItem {

    @Getter @Setter protected String name;
    @Getter @Setter protected int id;
    @Getter @Setter protected String relPath;
    @Getter @Setter protected MediaItem nextItem;
    @Getter @Setter protected MediaItem previusItem;
    @Getter @Setter protected Integer parentId;
    @Getter @Setter protected String parentCollectionPath;
    @Getter @Setter protected int levelNum;
    @Getter @Setter protected boolean isSelected;

    protected MediaItem() { }

    protected MediaItem(String name, int id, String relPath, MediaItem nextItem, MediaItem previusItem,
                        Integer parentId, String parentCollectionPath, int levelNum) {
        this.name = name;
        this.id = id;
        this.relPath = relPath;
        this.nextItem = nextItem;
        this.previusItem = previusItem;
        this.parentId = parentId;
        this.parentCollectionPath = parentCollectionPath;
        this.levelNum = levelNum;
    }

    protected abstract BufferedImage View();

}
