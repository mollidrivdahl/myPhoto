package com.mdSolutions.myPhoto;

import java.awt.image.BufferedImage;

public class VideoMedia extends IndividualMedia {

    public VideoMedia() {
        super();
    }

    public VideoMedia(String name, Integer id, String relPath, MediaItem nextItem, MediaItem previusItem,
                      Integer parentId, String parentCollectionPath, int levelNum) {
        super(name, id, relPath, nextItem, previusItem, parentId, parentCollectionPath, levelNum);
    }

    //TODO: Implement
    @Override
    public BufferedImage view() {
        return null;
    }

}

