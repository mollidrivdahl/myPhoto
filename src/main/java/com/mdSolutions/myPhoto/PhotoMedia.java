package com.mdSolutions.myPhoto;

import com.mdSolutions.myPhoto.gui.AppGui;
import lombok.Getter;
import lombok.Setter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class PhotoMedia extends IndividualMedia {

    private @Getter @Setter ImageIcon image;

    public PhotoMedia() {
        super();
    }

    @Override
    public BufferedImage view() {
        BufferedImage originalImg;
        BufferedImage thumbnail = null;
        int scaledWidth = 166;
        int scaledHeight = 166;

        try {
            //read and save image to local variable
            originalImg = ImageIO.read(new File(relPath));

            //creates output thumbnail image
            thumbnail = new BufferedImage(scaledWidth, scaledHeight, originalImg.getType());

            //scales the input image to the output image
            Graphics2D g2d = thumbnail.createGraphics();
            g2d.drawImage(originalImg, 0, 0, scaledWidth, scaledHeight, null);
            g2d.dispose();

            //setup full size image for local variable, maintaining original aspect-ratio
            Dimension fullSizeAspectRatio = calcScaledDimension(new Dimension(originalImg.getWidth(), originalImg.getHeight()),
                    new Dimension(AppGui.MAIN_WIDTH, AppGui.MID_HEIGHT - 75));
            BufferedImage tempFullSize = new BufferedImage((int)fullSizeAspectRatio.getWidth(),
                    (int)fullSizeAspectRatio.getHeight(), originalImg.getType());
            g2d = tempFullSize.createGraphics();
            g2d.drawImage(originalImg, 0, 0, (int)fullSizeAspectRatio.getWidth(), (int)fullSizeAspectRatio.getHeight(),null);
            g2d.dispose();
            image = new ImageIcon(tempFullSize);
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

    private Dimension calcScaledDimension(Dimension imgSize, Dimension boundary) {

        int originalWidth = imgSize.width;
        int originalHeight = imgSize.height;
        int boundWidth = boundary.width;
        int boundHeight = boundary.height;
        int newWidth = originalWidth;
        int newHeight = originalHeight;

        // first check if we need to scale width
        if (originalWidth > boundWidth) {
            //scale width to fit
            newWidth = boundWidth;
            //scale height to maintain aspect ratio
            newHeight = (newWidth * originalHeight) / originalWidth;
        }

        // then check if we need to scale even with the new height
        if (newHeight > boundHeight) {
            //scale height to fit instead
            newHeight = boundHeight;
            //scale width to maintain aspect ratio
            newWidth = (newHeight * originalWidth) / originalHeight;
        }

        return new Dimension(newWidth, newHeight);
    }
}
