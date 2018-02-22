package com.mdSolutions.myPhoto.gui;

import com.mdSolutions.myPhoto.VideoMedia;
import lombok.Getter;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;

import javax.swing.*;
import java.awt.*;

public class MyMediaPlayer {

    private VideoMedia videoMedia;
    private JPanel playbackPanel;
    private @Getter EmbeddedMediaPlayerComponent mediaPlayerComponent;

    MyMediaPlayer() { }

    MyMediaPlayer(VideoMedia videoMedia, JPanel videoPlaybackPanel) {
        this.videoMedia = videoMedia;
        playbackPanel = videoPlaybackPanel;
        start();
    }

    public void start() {
        SwingUtilities.invokeLater(() -> {
            new NativeDiscovery().discover();   //TODO: install VLC native library files on target machine with distribution
            mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
            mediaPlayerComponent.setPreferredSize(new Dimension(AppGui.MAIN_WIDTH - 10, AppGui.MID_HEIGHT - AppGui.PLAYBACK_HEIGHT - 10));

            viewVideoInWindow();
            addButtonsToPlaybackPanel();
            addMediaPlayerEventListener();

            mediaPlayerComponent.getMediaPlayer().playMedia(videoMedia.play());
        });
    }

    private void addMediaPlayerEventListener() {
        JFrame mediaPlayerDialogFrame = new JFrame();
        JDialog mediaPlayerDialog = new JDialog(mediaPlayerDialogFrame, true);
        JLabel loadingLabel = new JLabel("Opening video...please wait");
        JLabel errorLabel = new JLabel("Something went wrong");

        loadingLabel.setPreferredSize(new Dimension(200, 50));
        errorLabel.setPreferredSize(new Dimension(200, 50));

        mediaPlayerDialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        mediaPlayerComponent.getMediaPlayer().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void opening(MediaPlayer mediaPlayer) {
                SwingUtilities.invokeLater(() -> {
                    System.out.println("Opening video...please wait");
                    mediaPlayerDialog.setContentPane(loadingLabel);
                    mediaPlayerDialog.setLocationRelativeTo(null);
                    mediaPlayerDialog.pack();
                    mediaPlayerDialog.setVisible(true);
                });
            }

            @Override
            public void playing(MediaPlayer mediaPlayer) {
                SwingUtilities.invokeLater(() -> {
                    System.out.println("Video started playing");
                    mediaPlayerDialog.setVisible(false);
                });
            }

            @Override
            public void error(MediaPlayer mediaPlayer) {
                SwingUtilities.invokeLater(() -> {
                    System.out.println("Failed to play video");
                    mediaPlayerDialog.setContentPane(errorLabel);
                    mediaPlayerDialog.setLocationRelativeTo(null);
                    mediaPlayerDialog.pack();
                    mediaPlayerDialog.setVisible(true);
                });
            }
        });
    }

    private void viewVideoInWindow() {
        AppGui appGui = AppGui.getInstance();

        //place photo playback panel into the videoMedia playback panel
        appGui.getMediaPlaybackPanel().add(appGui.getVideoPlaybackPanel());

        //place the video into the videoMedia display panel
        appGui.getMediaDisplayPanel().add(mediaPlayerComponent);

        //remove scrollbars from videoMedia display scroll pane (holds the videoMedia display panel)
        appGui.getMediaDisplayScrollPane().setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        appGui.getMediaDisplayScrollPane().setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        //place the entire videoMedia view panel into the center scroll pane's viewport (swapping out the grid view panel)
        appGui.getCenterScrollPane().setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        appGui.getCenterScrollPane().setBorder(BorderFactory.createMatteBorder(0,0,0,0, Color.white));
        appGui.getCenterScrollPane().setViewportView(appGui.getMediaViewPanel());
    }

    private void addButtonsToPlaybackPanel() {
        JButton btnReturn = new JButton("<-- Go Back");
        btnReturn.addActionListener(e -> {
            AppGui appGui = AppGui.getInstance();

            //release media player resources
            mediaPlayerComponent.release();

            //remove the playback buttons from playback panel
            playbackPanel.removeAll();

            //remove the image and viewing playback features from corresponding panels
            appGui.getMediaDisplayPanel().removeAll();
            appGui.getMediaDisplayPanel().remove(playbackPanel);
            appGui.getMyPhoto().getCurrentCollection().unselectAllChildren();

            //replace scrollbar as needed to media display scroll pane
            appGui.getMediaDisplayScrollPane().setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            appGui.getMediaDisplayScrollPane().setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

            //swap the grid view panel back into the center scroll pane
            appGui.getCenterScrollPane().setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            appGui.getCenterScrollPane().setBorder(BorderFactory.createMatteBorder(5,5,5,5, Color.white));
            appGui.getCenterScrollPane().setViewportView(appGui.getGridViewPanel());
        });

        JButton btnPlay = new JButton("Play / Resume");
        btnPlay.addActionListener(e -> mediaPlayerComponent.getMediaPlayer().play());

        JButton btnPause = new JButton("Pause");
        btnPause.addActionListener(e -> mediaPlayerComponent.getMediaPlayer().pause());

        /*

        rewindButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mediaPlayerComponent.getMediaPlayer().skip(-10000);
            }
        });

        skipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mediaPlayerComponent.getMediaPlayer().skip(10000);
            }
        });

        */

        playbackPanel.add(btnReturn);
        playbackPanel.add(btnPlay);
        playbackPanel.add(btnPause);
    }
}
