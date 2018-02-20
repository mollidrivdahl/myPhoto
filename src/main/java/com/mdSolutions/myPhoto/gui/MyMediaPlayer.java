package com.mdSolutions.myPhoto.gui;

import com.mdSolutions.myPhoto.VideoMedia;
import lombok.Getter;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

import javax.swing.*;
import java.awt.*;

public class MyMediaPlayer {

    private VideoMedia videoMedia;
    private @Getter EmbeddedMediaPlayerComponent mediaPlayerComponent;

    MyMediaPlayer() { }

    MyMediaPlayer(VideoMedia videoMedia) {
        this.videoMedia = videoMedia;
        start();
    }

    public void start() {
        SwingUtilities.invokeLater(() -> {
            new NativeDiscovery().discover();   //TODO: install VLC native library files on target machine with distribution
            mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
            mediaPlayerComponent.setPreferredSize(new Dimension(AppGui.MAIN_WIDTH - 10, AppGui.MID_HEIGHT - AppGui.PLAYBACK_HEIGHT - 10));

            viewVideoInWindow();

            System.out.println(mediaPlayerComponent.getMediaPlayer().playMedia(videoMedia.play()));
        });
    }

    private void viewVideoInWindow() {
        AppGui appInstance = AppGui.getInstance();

        //place photo playback panel into the videoMedia playback panel
        appInstance.getMediaPlaybackPanel().add(appInstance.getVideoPlaybackPanel());

        //place the video into the videoMedia display panel
        appInstance.getMediaDisplayPanel().add(mediaPlayerComponent);

        //remove scrollbars from videoMedia display scroll pane (holds the videoMedia display panel)
        appInstance.getMediaDisplayScrollPane().setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        appInstance.getMediaDisplayScrollPane().setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        //place the entire videoMedia view panel into the center scroll pane's viewport (swapping out the grid view panel)
        appInstance.getCenterScrollPane().setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        appInstance.getCenterScrollPane().setBorder(BorderFactory.createMatteBorder(0,0,0,0, Color.white));
        appInstance.getCenterScrollPane().setViewportView(appInstance.getMediaViewPanel());
    }


}
