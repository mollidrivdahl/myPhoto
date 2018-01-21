package com.mdSolutions.myPhoto;

import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MyPhoto {

    @Getter @Setter MediaCollection currentCollection;

    public MyPhoto() {
        currentCollection = DbAccess.getInstance().getRootCollection();
    }

    public MediaCollection createCollection() {
        //check if new collection would be created inside level 4
        if ((currentCollection.getLevelNum() + 1) >= 4)
            return null;

        String defaultName = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(Calendar.getInstance().getTime());
        String defaultPath = currentCollection.getRelPath() + defaultName + "/";

        MediaCollection newCollection = new MediaCollection(defaultName, -1, defaultPath, null,
                currentCollection.getTailItem(), currentCollection.getId(), currentCollection.getRelPath(),
                currentCollection.getLevelNum() + 1, "resources/myPhotoLogo.png");

        int newId = DbAccess.getInstance().addNewCollection(newCollection);
        newCollection.setId(newId);

        currentCollection.addMedia(newCollection);

        return newCollection;
    }

    public void refreshCurrentCollection(Integer id) {
        //reset values that may [not] be reset in following method call
        currentCollection.setHeadItem(null);
        currentCollection.setTailItem(null);

        DbAccess.getInstance().refreshCurrentCollection(currentCollection, id);

        //reset values that aren't reset in previous method call
        currentCollection.setNextItem(null);
        currentCollection.setPreviusItem(null);
    }
}
