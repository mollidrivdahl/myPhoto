package com.mdSolutions.myPhoto;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class PhotoMedia extends IndividualMedia {

    public PhotoMedia() {
        super();
    }

    //TODO: Implement
    @Override
    public BufferedImage view() {
        BufferedImage originalImg;
        BufferedImage thumbnail = null;
        int scaledWidth = 166;
        int scaledHeight = 166;

        try {
            originalImg = ImageIO.read(new File(relPath));

            // creates output image
            thumbnail = new BufferedImage(scaledWidth, scaledHeight, originalImg.getType());

            // scales the input image to the output image
            Graphics2D g2d = thumbnail.createGraphics();
            g2d.drawImage(originalImg, 0, 0, scaledWidth, scaledHeight, null);
            g2d.dispose();
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return thumbnail;
    }

    //TODO: Implement
    @Override
    public void editInAlternateApp() {

    }
}
