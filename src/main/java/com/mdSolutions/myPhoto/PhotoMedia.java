package com.mdSolutions.myPhoto;

import com.mdSolutions.myPhoto.gui.AppGui;
import lombok.Getter;
import lombok.Setter;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class PhotoMedia extends IndividualMedia {

    private enum ORIENTATION { STRAIGHT, RIGHTWARD, UPSIDEDOWN, LEFTWARD }
    private @Getter @Setter ImageIcon image;
    private ORIENTATION curOrientation;
    private double curZoomMultiplier;

    public PhotoMedia() {
        super();
    }

    public PhotoMedia(String name, Integer id, String relPath, MediaItem nextItem, MediaItem previusItem,
                      Integer parentId, String parentCollectionPath, int levelNum) {
        super(name, id, relPath, nextItem, previusItem, parentId, parentCollectionPath, levelNum);
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
            //TODO: adjust full size calculation for a re-sized window
            Dimension fullSizeAspectRatio = calcScaledDimension(new Dimension(originalImg.getWidth(), originalImg.getHeight()),
                    new Dimension(AppGui.MAIN_WIDTH - 10, AppGui.MID_HEIGHT - AppGui.PLAYBACK_HEIGHT - 10)); //-10 for scrollbar
            BufferedImage tempFullSize = new BufferedImage((int)fullSizeAspectRatio.getWidth(),
                    (int)fullSizeAspectRatio.getHeight(), originalImg.getType());
            g2d = tempFullSize.createGraphics();
            g2d.drawImage(originalImg, 0, 0, (int)fullSizeAspectRatio.getWidth(), (int)fullSizeAspectRatio.getHeight(),null);
            g2d.dispose();

            image = new ImageIcon(tempFullSize);
            curOrientation = ORIENTATION.STRAIGHT;
            curZoomMultiplier = 1;
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return thumbnail;
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
                    new Dimension(AppGui.MAIN_WIDTH - 10, AppGui.MID_HEIGHT - AppGui.PLAYBACK_HEIGHT - 10)); //-10 for scrollbar
            BufferedImage tempFullSize = new BufferedImage((int)fullSizeAspectRatio.getWidth(),
                    (int)fullSizeAspectRatio.getHeight(), originalImg.getType());
            Graphics2D g2d = tempFullSize.createGraphics();
            g2d.drawImage(newImg, 0, 0, (int)fullSizeAspectRatio.getWidth(), (int)fullSizeAspectRatio.getHeight(),null);
            g2d.dispose();

            image = new ImageIcon(tempFullSize);
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void zoom(int targetMagnitude) {
        //10 = width & height x5, 5 = width & height x2.5, 2 = width & height x1, 1 = width & height x.5

        double magnitudeMultiplier = (double)targetMagnitude / (double)2;  //2 is the original default magnitude

        double originalWidth = image.getIconWidth() / curZoomMultiplier;
        double originalHeight = image.getIconHeight() / curZoomMultiplier;

        double newWidth = originalWidth * magnitudeMultiplier;
        double newHeight = originalHeight * magnitudeMultiplier;

        curZoomMultiplier = magnitudeMultiplier;

        BufferedImage curImg = (BufferedImage) image.getImage();
        BufferedImage zoomedImg = new BufferedImage((int)newWidth, (int)newHeight, curImg.getType());
        Graphics2D g2d = zoomedImg.createGraphics();
        g2d.drawImage(curImg, 0, 0, (int)newWidth, (int)newHeight,null);
        g2d.dispose();

        image = new ImageIcon(zoomedImg);
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
