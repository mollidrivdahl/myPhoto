package com.mdSolutions.myPhoto;

import com.mdSolutions.myPhoto.gui.AppGui;
import lombok.Getter;
import lombok.Setter;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;

public class PhotoMedia extends IndividualMedia {

    private enum ORIENTATION { STRAIGHT, RIGHTWARD, UPSIDEDOWN, LEFTWARD }
    private @Getter @Setter ImageIcon image;
    private ORIENTATION curOrientation;

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
            curOrientation = ORIENTATION.STRAIGHT;
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

    public void rotate() {
        BufferedImage originalImg;
        BufferedImage newImg;

        try {
            originalImg = ImageIO.read(new File(relPath));

            //set new orientation and rotate img
            if (curOrientation == ORIENTATION.STRAIGHT) {
                curOrientation = ORIENTATION.RIGHTWARD;
                newImg = Scalr.rotate(originalImg, Scalr.Rotation.CW_90);
            }
            else if (curOrientation == ORIENTATION.RIGHTWARD) {
                curOrientation = ORIENTATION.UPSIDEDOWN;
                newImg = Scalr.rotate(originalImg, Scalr.Rotation.CW_180);
            }
            else if (curOrientation == ORIENTATION.UPSIDEDOWN) {
                curOrientation = ORIENTATION.LEFTWARD;
                newImg = Scalr.rotate(originalImg, Scalr.Rotation.CW_270);
            }
            else {
                curOrientation = ORIENTATION.STRAIGHT;
                newImg = originalImg;
            }

            //setup full size image for local variable, maintaining original aspect-ratio
            Dimension fullSizeAspectRatio = calcScaledDimension(new Dimension(newImg.getWidth(), newImg.getHeight()),
                    new Dimension(AppGui.MAIN_WIDTH, AppGui.MID_HEIGHT - 75));
            BufferedImage tempFullSize = new BufferedImage((int)fullSizeAspectRatio.getWidth(),
                    (int)fullSizeAspectRatio.getHeight(), originalImg.getType());
            Graphics2D g2d = tempFullSize.createGraphics();
            g2d.drawImage(newImg, 0, 0, (int)fullSizeAspectRatio.getWidth(), (int)fullSizeAspectRatio.getHeight(),null);
            g2d.dispose();

            image = new ImageIcon(tempFullSize);
        }
        catch (Exception ex) {
            System.out.println(ex);
        }
    }
}
