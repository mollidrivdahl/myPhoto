package com.mdSolutions.myPhoto;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.net.URL;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.media.*;
import javafx.stage.Stage;

import javax.swing.*;

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

    public String play() {
        return relPath;
    }
}

