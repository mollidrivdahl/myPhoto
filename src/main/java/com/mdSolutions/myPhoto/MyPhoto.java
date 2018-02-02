package com.mdSolutions.myPhoto;

import lombok.Getter;
import lombok.Setter;

import javax.activity.InvalidActivityException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MyPhoto {

    public enum AUTO_ORGANIZE_BY { NAME_ASCENDING, NAME_DESCENDING, COLLECTIONS_FIRST, COLLECTIONS_LAST}
    @Getter @Setter MediaCollection currentCollection;

    public MyPhoto() {
        currentCollection = DbAccess.getInstance().getRootCollection();
    }

    public MediaCollection createCollection() {
        //check if new collection would be created inside level 4
        if ((currentCollection.getLevelNum() + 1) >= 4)
            return null;

        String defaultName = new SimpleDateFormat("EEE-dd-MMM-yyyy-HH.mm.ss.SSS").format(Calendar.getInstance().getTime());
        String defaultPath = currentCollection.getRelPath() + defaultName + "/";
        MediaCollection newCollection = null;

        //create collection's directory in parent collection path location in file system
        if (!FileSystemAccess.fileExists(defaultPath) && FileSystemAccess.createDirectory(defaultPath) != null) {

            newCollection = new MediaCollection(defaultName, -1, defaultPath, null,
                    currentCollection.getTailItem(), currentCollection.getId(), currentCollection.getRelPath(),
                    currentCollection.getLevelNum() + 1, "resources/myPhotoLogo.png");

            int newId = DbAccess.getInstance().addNewCollection(newCollection);
            newCollection.setId(newId);

            currentCollection.addMedia(newCollection);
        }

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

    public void importMedia(File[] selectedFiles) {
        //create new collection in root collection for importing the media items into
        currentCollection = DbAccess.getInstance().getRootCollection();
        MediaCollection newCollection = null;

        if ((newCollection = createCollection()) != null) { //TODO: figure out how to handle if this is untrue

            for (File file : selectedFiles) {
                //copy (import) file to new collection's directory in the file system
                FileSystemAccess.copyForImport(file, newCollection.getRelPath());   //TODO: handle copying failing for some files

                //determine concrete type & call constructor for that type
                MediaItem newMedia = MediaItem.getConcreteType(file);
                newMedia.setName(file.getName());

                //add new MediaItem to the new collection
                newCollection.addMedia(newMedia);
            }
        }

        //append imported media to the new collection in database
        DbAccess.getInstance().appendNewChildMedia(newCollection);
    }

    public void organizeManually(MediaItem leftConnection, MediaItem rightConnection) {
        currentCollection.organizeManually(leftConnection, rightConnection);
        DbAccess.getInstance().updateChildMediaArrangement(currentCollection);
    }

    public void organizeAutomatically(AUTO_ORGANIZE_BY format) {
        currentCollection.organizeAutomatically(format);
        DbAccess.getInstance().updateChildMediaArrangement(currentCollection);
    }

    public void moveMediaIn(MediaCollection destCollection) throws InvalidActivityException {
        boolean preventAction = false;

        //TODO: check if any [nested] collections would be moved into level 4 & prevent action


        if (preventAction)
            throw new InvalidActivityException("Cannot move media - one or more [nested] collections would be moved down to level 4");

        currentCollection.moveMedia(destCollection);
        //DbAccess.getInstance().appendExistingChildMedia(destCollection);
        //DbAccess.getInstance().updateChildMediaArrangement(currentCollection);
    }

    public void moveMediaOut() throws InvalidActivityException {
        if (currentCollection.getLevelNum() == 0)
            throw new InvalidActivityException("Cannot move media up - this is the root collection");

        //retrieve currentCollection's parent details (just not the parent's list of children)
        MediaCollection parentCollection = DbAccess.getInstance().getMediaById(currentCollection.getParentId());

        currentCollection.moveMedia(parentCollection);
        //DbAccess.getInstance().appendExistingChildMedia(destCollection);
        //DbAccess.getInstance().updateChildMediaArrangement(currentCollection);
    }

    /**
     * For non-gui related file system interaction
     */
    static class FileSystemAccess {

        static boolean fileExists(String path) {
            return new File(path).exists();
        }

        static File createDirectory(String newPath) {
            File newDir = new File(newPath);    //newPath = path+dirName

            try {
                if (newDir.mkdir())
                    return newDir;
            }
            catch(SecurityException ex){
                System.out.println(ex);
            }

            return null;    //directory failed to be created
        }

        static void copyForImport(File importedFile, String destDirPath) {
            try {
                Files.copy(importedFile.toPath(), new File(destDirPath + importedFile.getName()).toPath());
            }
            catch (IOException ex) {
                System.out.println(ex);
            }
        }
    }
}
