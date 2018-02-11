package com.mdSolutions.myPhoto;

import java.awt.image.BufferedImage;

public class UnsupportedMedia extends IndividualMedia {

    public UnsupportedMedia() {
        super();
    }

    @Override
    public BufferedImage view() {
        return null;
    }
}
