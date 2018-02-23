package com.mdSolutions.myPhoto;

import com.xuggle.xuggler.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class VideoMedia extends IndividualMedia {

    public VideoMedia() {
        super();
    }

    public VideoMedia(String name, Integer id, String relPath, MediaItem nextItem, MediaItem previusItem,
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
            //extract a still frame image from the video
            originalImg = extractThumbnail();

            //creates output thumbnail image
            thumbnail = new BufferedImage(scaledWidth, scaledHeight, originalImg.getType());

            //scales the input image to the output image
            Graphics2D g2d = thumbnail.createGraphics();
            g2d.drawImage(originalImg, 0, 0, scaledWidth, scaledHeight, null);
            g2d.dispose();
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return thumbnail;
    }

    private BufferedImage extractThumbnail() throws NumberFormatException,IOException {

        String filename = relPath;
        BufferedImage javaImage = null;

        if (!IVideoResampler.isSupported(IVideoResampler.Feature.FEATURE_COLORSPACECONVERSION))
            throw new RuntimeException("you must install the GPL version of Xuggler (with IVideoResampler support) for this to work");

        IContainer container = IContainer.make();

        if (container.open(filename, IContainer.Type.READ, null) < 0)
            throw new IllegalArgumentException("could not open file: "
                    + filename);

        int numStreams = container.getNumStreams();

        // and iterate through the streams to find the first video stream
        int videoStreamId = -1;
        IStreamCoder videoCoder = null;
        for (int i = 0; i < numStreams; i++) {
            // find the stream object
            IStream stream = container.getStream(i);
            // get the pre-configured decoder that can decode this stream;
            IStreamCoder coder = stream.getStreamCoder();

            if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
                videoStreamId = i;
                videoCoder = coder;
                break;
            }
        }

        if (videoStreamId == -1)
            throw new RuntimeException(
                    "could not find video stream in container: " + filename);

        if (videoCoder.open() < 0)
            throw new RuntimeException(
                    "could not open video decoder for container: " + filename);

        IVideoResampler resampler = null;

        if (videoCoder.getPixelType() != IPixelFormat.Type.BGR24) {
            resampler = IVideoResampler.make(videoCoder.getWidth(), videoCoder
                    .getHeight(), IPixelFormat.Type.BGR24, videoCoder
                    .getWidth(), videoCoder.getHeight(), videoCoder
                    .getPixelType());
            if (resampler == null)
                throw new RuntimeException(
                        "could not create color space resampler for: "
                                + filename);
        }

        IPacket packet = IPacket.make();

        long timeStampOffset = 1;
        long target = container.getStartTime() + timeStampOffset;
        boolean isFinished = false;

        container.seekKeyFrame(videoStreamId, target, 0);

        while(container.readNextPacket(packet) >= 0 && !isFinished ) {
            if (packet.getStreamIndex() == videoStreamId) {
                IVideoPicture picture = IVideoPicture.make(videoCoder
                        .getPixelType(), videoCoder.getWidth(), videoCoder
                        .getHeight());

                int offset = 0;
                while (offset < packet.getSize()) {
                    int bytesDecoded = videoCoder.decodeVideo(picture, packet,
                            offset);
                    if (bytesDecoded < 0) {
                        System.err.println("WARNING!!! got no data decoding " +
                                "video in one packet");
                    }
                    offset += bytesDecoded;

                    if (picture.isComplete()) {
                        IVideoPicture newPic = picture;

                        if (resampler != null) {

                            newPic = IVideoPicture.make(resampler
                                            .getOutputPixelFormat(), picture.getWidth(),
                                    picture.getHeight());
                            if (resampler.resample(newPic, picture) < 0)
                                throw new RuntimeException(
                                        "could not resample video from: "
                                                + filename);
                        }

                        if (newPic.getPixelType() != IPixelFormat.Type.BGR24)
                            throw new RuntimeException(
                                    "could not decode video as BGR 24 bit data in: "
                                            + filename);

                        javaImage = Utils.videoPictureToImage(newPic);
                        isFinished = true;
                    }
                }
            }
        }

        if (videoCoder != null)
            videoCoder.close();

        if (container != null)
            container.close();

        return javaImage;
    }
}

