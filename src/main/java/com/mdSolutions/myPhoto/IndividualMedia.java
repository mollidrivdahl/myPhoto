package com.mdSolutions.myPhoto;

import lombok.Getter;
import lombok.Setter;

public abstract class IndividualMedia extends MediaItem {

    protected @Getter @Setter boolean isCoverPhoto;

    protected IndividualMedia() {
        super();
        isCoverPhoto = false;
    }

    protected IndividualMedia(String name, Integer id, String relPath, MediaItem nextItem, MediaItem previusItem,
                              Integer parentId, String parentCollectionPath, int levelNum) {
        super(name, id, relPath, nextItem, previusItem, parentId, parentCollectionPath, levelNum);
        isCoverPhoto = false;
    }

}
