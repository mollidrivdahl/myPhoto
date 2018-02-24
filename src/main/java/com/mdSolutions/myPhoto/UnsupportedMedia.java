package com.mdSolutions.myPhoto;

import java.awt.image.BufferedImage;

public class UnsupportedMedia extends IndividualMedia {

    public UnsupportedMedia() {
        super();
    }

    public UnsupportedMedia(String name, Integer id, String relPath, MediaItem nextItem, MediaItem previusItem,
                            Integer parentId, String parentCollectionPath, int levelNum) {
        super(name, id, relPath, nextItem, previusItem, parentId, parentCollectionPath, levelNum);
    }

    @Override
    public BufferedImage view() {
        //TODO: return buffered image of the "unknownMedia.png" resource image
        return null;
    }
}
