package com.mdSolutions.myPhoto;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.ArrayList;

public class FbMediaUploader {

    private static final String PHOTO_SHARE_FOLDER_PATH = "fbPhotoShareFolder";
    private static final String VIDEO_SHARE_FOLDER_PATH = "fbVideoShareFolder";

    private static FbMediaUploader _instance;
    private @Getter @Setter ArrayList<PhotoMedia> photos;
    private @Getter @Setter ArrayList<VideoMedia> videos;

    private FbMediaUploader() {
        if (!MyPhoto.FileSystemAccess.fileExists(PHOTO_SHARE_FOLDER_PATH)) {
            File photoShareFolder = MyPhoto.FileSystemAccess.createDirectory(PHOTO_SHARE_FOLDER_PATH);

            if (photoShareFolder != null)
                photoShareFolder.deleteOnExit();
        }

        if (!MyPhoto.FileSystemAccess.fileExists(VIDEO_SHARE_FOLDER_PATH)) {
            File videoShareFolder = MyPhoto.FileSystemAccess.createDirectory(VIDEO_SHARE_FOLDER_PATH);

            if (videoShareFolder != null)
                videoShareFolder.deleteOnExit();
        }

        photos = new ArrayList<>();
        videos = new ArrayList<>();
    }

    public static FbMediaUploader getInstance() {
        if (_instance == null) {
            _instance = new FbMediaUploader();
        }

        return _instance;
    }

    public void addMedia(ArrayList<IndividualMedia> selectedMedia) {
        for (IndividualMedia media : selectedMedia) {
            if (media instanceof PhotoMedia) {
                PhotoMedia newPhoto = new PhotoMedia(media.getName(), media.getId(), PHOTO_SHARE_FOLDER_PATH + "/" + media.getName(),
                        null, null, null, null, -1);

                photos.add(newPhoto);

                MyPhoto.FileSystemAccess.copyForImport(new File(media.getRelPath()), PHOTO_SHARE_FOLDER_PATH + "/");

                File newMediaFile = new File(newPhoto.getRelPath());
                newMediaFile.deleteOnExit();
            }
            else if (media instanceof VideoMedia) {
                VideoMedia newVideo = new VideoMedia(media.getName(), media.getId(), VIDEO_SHARE_FOLDER_PATH + "/" + media.getName(),
                        null, null, null, null, -1);

                videos.add(newVideo);

                MyPhoto.FileSystemAccess.copyForImport(new File(media.getRelPath()), VIDEO_SHARE_FOLDER_PATH + "/");

                File newMediaFile = new File(newVideo.getRelPath());
                newMediaFile.deleteOnExit();
            }
        }
    }

    public void removeMedia(IndividualMedia media) {
        if (!new File(media.getRelPath()).delete())
            System.out.println("Failed to delete media from a FB Share Folder");

        if (media instanceof PhotoMedia)
            photos.remove(media);
        else if (media instanceof VideoMedia)
            videos.remove(media);
    }
}
