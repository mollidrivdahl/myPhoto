package com.mdSolutions.myPhoto;

public abstract class IndividualMedia extends MediaItem {

    protected IndividualMedia() {
        super();
    }

    protected IndividualMedia(String name, Integer id, String relPath, MediaItem nextItem, MediaItem previusItem,
                              Integer parentId, String parentCollectionPath, int levelNum) {
        super(name, id, relPath, nextItem, previusItem, parentId, parentCollectionPath, levelNum);
    }

}
