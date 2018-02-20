package com.mdSolutions.myPhoto;

import com.restfb.BinaryAttachment;
import com.restfb.Connection;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.Album;
import com.restfb.types.GraphResponse;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FbMediaUploader {

    static final String[] uploadablePhotoTypes = new String[] { "jpg", "jpeg", "png", "gif", "bmp", "wbmp", "tiff", "tif" };
    static final String[] uploadableVideoTypes = new String[] { "3g2", "3gp", "3gpp", "asf", "avi", "dat", "divx", "dv", "f4v",
                                                                "flv", "m2ts", "m4v", "mkv", "mod", "mov", "mp4", "mpe", "mpeg",
                                                                "mpeg4", "mpg", "mts", "nsv", "ogm", "ogv", "qt", "tod", "ts",
                                                                "vob", "wmv" };
    public enum MEDIA_TYPE {PHOTOS, VIDEOS}

    private static final String PHOTO_SHARE_FOLDER_PATH = "fbPhotoShareFolder";
    private static final String VIDEO_SHARE_FOLDER_PATH = "fbVideoShareFolder";
    public static final String APP_ID = "1979842148933269";
    public static final String APP_SECRET = "7fc9288403e0bce512dae74594bfc8b5";
    public static final String APP_STATE = "stateMyPhoto723";   //made up

    private static FbMediaUploader _instance;
    private @Getter @Setter ArrayList<PhotoMedia> photos;
    private @Getter @Setter ArrayList<VideoMedia> videos;
    private @Getter @Setter MEDIA_TYPE uploadType;
    private @Getter @Setter String appCode;
    private @Getter @Setter String accessToken;
    private @Getter @Setter String userId;
    private @Getter @Setter FacebookClient fbClient;
    private @Getter @Setter boolean loggedIn;

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
        uploadType = null;
    }

    public static FbMediaUploader getInstance() {
        if (_instance == null) {
            _instance = new FbMediaUploader();
        }

        return _instance;
    }

    public void addMedia(ArrayList<IndividualMedia> selectedMedia) {
        for (IndividualMedia media : selectedMedia) {
            //only add media if it is supported for upload to Facebook
            if (isUploadable(media)) {
                //treat as a photo
                if (isExtPhotoType(media)) {
                    PhotoMedia newPhoto = new PhotoMedia(media.getName(), media.getId(), PHOTO_SHARE_FOLDER_PATH + "/" + media.getName(),
                            null, null, null, null, -1);

                    photos.add(newPhoto);

                    MyPhoto.FileSystemAccess.copyForImport(new File(media.getRelPath()), PHOTO_SHARE_FOLDER_PATH + "/");

                    File newMediaFile = new File(newPhoto.getRelPath());
                    newMediaFile.deleteOnExit();
                }
                //treat as a video
                else  {
                    VideoMedia newVideo = new VideoMedia(media.getName(), media.getId(), VIDEO_SHARE_FOLDER_PATH + "/" + media.getName(),
                            null, null, null, null, -1);

                    videos.add(newVideo);

                    MyPhoto.FileSystemAccess.copyForImport(new File(media.getRelPath()), VIDEO_SHARE_FOLDER_PATH + "/");

                    File newMediaFile = new File(newVideo.getRelPath());
                    newMediaFile.deleteOnExit();
                }
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

    public void uploadPhotos(String albumName, String message) {
        System.out.println(albumName);
        System.out.println(message);
        System.out.println("Upload Photos");

        FacebookClient client = FbMediaUploader.getInstance().getFbClient();

        //fetch list of existing album ids
        Connection<Album> existingAlbums = client.fetchConnection("me/albums", Album.class);
        Boolean albumExists = false;
        String albumId = "";

        //check whether desired destination album already exists
        for (List<Album> albumPage : existingAlbums) {
            for (Album album : albumPage) {
                if (album.getName().equals(albumName)) {
                    albumExists = true;
                    albumId = album.getId();
                    break;
                }
            }

            if (albumExists)
                break;
        }

        //create new album if album does not already exist
        if (!albumExists) {
            GraphResponse publishAlbumResponse = client.publish("me/albums", GraphResponse.class, Parameter.with("name", albumName));
            albumId = publishAlbumResponse.getId();

            System.out.println("Published album ID: " + publishAlbumResponse.getId());
        }

        //publish photos to facebook
        for (PhotoMedia photo : FbMediaUploader.getInstance().getPhotos()) {
            try {
                File photoFile = new File(photo.getRelPath());
                FileInputStream inputStream = new FileInputStream(photoFile);

                GraphResponse publishPhotoResponse = client.publish(albumId + "/photos", GraphResponse.class,
                        BinaryAttachment.with(photo.getName(), inputStream),
                        Parameter.with("message", message));

                if (!publishPhotoResponse.isSuccess())
                    System.out.println("Unsuccessful publish of photo: " + photo.getName());    //TODO: notify user of failure
                else
                    System.out.println("Published photo ID: " + publishPhotoResponse.getId());
            }
            catch (FileNotFoundException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public void uploadVideos(String message) {
        System.out.println(message);
        System.out.println("Upload Videos");

        FacebookClient client = FbMediaUploader.getInstance().getFbClient();

        //publish videos to facebook
        for (VideoMedia video : FbMediaUploader.getInstance().getVideos()) {
            try {
                File videoFile = new File(video.getRelPath());
                FileInputStream inputStream = new FileInputStream(videoFile);

                GraphResponse publishVideoResponse = client.publish("me/videos", GraphResponse.class,
                        BinaryAttachment.with(video.getName(), inputStream),
                        Parameter.with("description", message));

                if (!publishVideoResponse.isSuccess())
                    System.out.println("Unsuccessful publish of video: " + video.getName());    //TODO: notify user of failure
                else
                    System.out.println("Published video ID: " + publishVideoResponse.getId());
            }
            catch (FileNotFoundException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    private boolean isUploadable(IndividualMedia media) {
        String name = media.getName();
        String ext;

        //extract extension from name
        try {
            ext = name.substring(name.lastIndexOf(".") + 1);
        }
        catch (Exception e) {
            ext = null;
        }

        //check extension against uploadable types
        if (ext != null) {
            if (containsCaseInsensitive(ext, Arrays.asList(uploadablePhotoTypes)))
                return true;
            else if (containsCaseInsensitive(ext, Arrays.asList(uploadableVideoTypes)))
                return true;
        }

        return false;
    }

    private boolean isExtPhotoType(IndividualMedia media) {
        String name = media.getName();
        String ext;

        //extract extension from name
        try {
            ext = name.substring(name.lastIndexOf(".") + 1);
        }
        catch (Exception e) {
            ext = null;
        }

        //check extension against uploadable types
        if (ext != null && containsCaseInsensitive(ext, Arrays.asList(uploadablePhotoTypes)))
            return true;

        return false;
    }

    private boolean containsCaseInsensitive(String s, List<String> l){
        return l.stream().anyMatch(x -> x.equalsIgnoreCase(s));
    }
}
