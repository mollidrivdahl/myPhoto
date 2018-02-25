package com.mdSolutions.myPhoto;

import lombok.Getter;
import lombok.Setter;
import org.sqlite.core.DB;

import javax.activity.InvalidActivityException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.stream.Stream;

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

    public ArrayList<String> importMedia(File[] selectedFiles) {
        //create new collection in root collection for importing the media items into
        currentCollection = DbAccess.getInstance().getRootCollection();
        MediaCollection newCollection = null;
        ArrayList<String> failedImports = new ArrayList<>();

        if ((newCollection = createCollection()) != null) { //TODO: figure out how to handle if this is untrue

            for (File file : selectedFiles) {
                //copy (import) file to new collection's directory in the file system
                try {
                    FileSystemAccess.copyForImport(file, newCollection.getRelPath());

                    //determine concrete type & call constructor for that type
                    MediaItem newMedia = MediaItem.getConcreteType(file);
                    newMedia.setName(file.getName());

                    //add new MediaItem to the new collection
                    newCollection.addMedia(newMedia);
                }
                catch (InvalidActivityException ex) { failedImports.add(ex.getMessage() + "\n"); }
            }
        }

        //append imported media to the new collection in database
        DbAccess.getInstance().appendNewChildMedia(newCollection);

        return failedImports;
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
        boolean allowAction = true;

        //check if any [nested] collections would be moved into level 4 & prevent action
        Stream<MediaItem> selectedMedia = currentCollection.getListOfChildren().stream().filter(MediaItem::isSelected);
        allowAction = DbAccess.getInstance().isNestingAllowed(selectedMedia.toArray());

        if (!allowAction)
            throw new InvalidActivityException("Cannot move media - one or more [nested] collections would be moved down to level 4");

        currentCollection.moveMedia(destCollection);
        DbAccess.getInstance().appendExistingChildMedia(destCollection, true);
        DbAccess.getInstance().updateChildMediaArrangement(currentCollection);

        //reset destination collection
        destCollection.getListOfChildren().clear();
        destCollection.setHeadItem(null);
        destCollection.setTailItem(null);
    }

    public void moveMediaOut() throws InvalidActivityException {
        if (currentCollection.getLevelNum() == 0)
            throw new InvalidActivityException("Cannot move media up - this is the root collection");

        //retrieve currentCollection's parent details (just not the parent's list of children)
        MediaCollection parentCollection = DbAccess.getInstance().getMediaById(currentCollection.getParentId());

        if (parentCollection.getLevelNum() == 0) {    //root collection
            for (MediaItem media : currentCollection.getListOfChildren()) {
                //check if any stand-alone media would be moved up to level 1 & prevent action
                if (media.isSelected() && !(media instanceof MediaCollection))
                    throw new InvalidActivityException("Cannot move media up - one or more individual media items would be moved up to level 1");
            }
        }

        currentCollection.moveMedia(parentCollection);
        DbAccess.getInstance().appendExistingChildMedia(parentCollection, false);
        DbAccess.getInstance().updateChildMediaArrangement(currentCollection);
    }

    public void createDuplicates() throws InvalidActivityException {
        //if any of the selected items are collections, throw error
        if (currentCollection.getListOfChildren().stream()
                .filter(MediaItem::isSelected)
                .anyMatch(i -> i instanceof MediaCollection))
        {
            throw new InvalidActivityException("Cannot duplicate media - one or more items are collections");
        }

        currentCollection.duplicateMedia();
        DbAccess.getInstance().addAndUpdateChildMedia(currentCollection);
        DbAccess.getInstance().refreshCurrentCollection(currentCollection, currentCollection.getId());

        currentCollection.unselectAllChildren();
    }


    public void deleteMedia() {
        ArrayList<MediaItem> mediaToDelete = currentCollection.deleteMedia();
        DbAccess.getInstance().updateChildMediaArrangement(currentCollection);
        DbAccess.getInstance().deleteMedia(mediaToDelete);
    }

    public void copyToFacebook() {
        ArrayList<IndividualMedia> fbMedia = new ArrayList<>();

        currentCollection.getListOfChildren().stream()
                .filter(MediaItem::isSelected)
                .filter(i -> (i instanceof IndividualMedia))
                .forEach(i -> fbMedia.add((IndividualMedia)i));

        FbMediaUploader.getInstance().addMedia(fbMedia);
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
                System.out.println(ex.getMessage());
            }

            return null;    //directory failed to be created
        }

        static void copyForImport(File importedFile, String destDirPath) throws InvalidActivityException {
            try {
                Files.copy(importedFile.toPath(), new File(destDirPath + importedFile.getName()).toPath());
            }
            catch (IOException ex) {
                System.out.println(ex.getMessage());
                throw new InvalidActivityException(importedFile.toPath().toString());
            }
        }

        static void copyForDuplicate(File originalFile, File newFile) throws InvalidActivityException {
            try {
                Files.copy(originalFile.toPath(), newFile.toPath());
            }
            catch (IOException ex) {
                System.out.println(ex.getMessage());
                throw new InvalidActivityException(newFile.toPath().toString());
            }
        }
    }
}
