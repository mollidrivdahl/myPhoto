package com.mdSolutions.myPhoto;

import lombok.Getter;
import lombok.Setter;

public class MyPhoto {

    @Getter @Setter MediaCollection currentCollection;

    public MyPhoto() {
        //TODO: currentCollection = DbAccess.getInstance().getRootCollection();

        //temporary:
        currentCollection = new MediaCollection();
        currentCollection.setName("MyPhotoRoot");
        currentCollection.setId(0);
        currentCollection.setLevelNum(0);
        currentCollection.setRelPath("MyPhotoRoot\\");
        currentCollection.setNextItem(null);
        currentCollection.setPreviusItem(null);
        currentCollection.setParentId(null);
        currentCollection.setParentCollectionPath(null);
        currentCollection.setSelected(false);
        currentCollection.setCoverPhotoPath(null);
        currentCollection.setListOfChildren(null);
    }

    //TODO: implement
    public MediaCollection createCollection() {
        return null;
    }
}
