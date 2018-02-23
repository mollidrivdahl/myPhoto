package com.mdSolutions.myPhoto.gui;

import com.mdSolutions.myPhoto.VideoMedia;
import lombok.Getter;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class MyMediaPlayer {

    private VideoMedia videoMedia;
    private JPanel playbackPanel;
    private @Getter EmbeddedMediaPlayerComponent mediaPlayerComponent;
    private boolean isReplay;
    private JSlider sliderSeek;
    private JLabel labelCurTime;
    private JLabel labelFullLength;

    MyMediaPlayer() { }

    MyMediaPlayer(VideoMedia videoMedia, JPanel videoPlaybackPanel) {
        this.videoMedia = videoMedia;
        playbackPanel = videoPlaybackPanel;
        isReplay = false;
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

            isReplay = false;
            mediaPlayerComponent.getMediaPlayer().playMedia(videoMedia.getRelPath());
        });
    }

    private void addMediaPlayerEventListener() {
        JFrame mediaPlayerDialogFrame = new JFrame();
        JDialog mediaPlayerDialog = new JDialog(mediaPlayerDialogFrame, true);
        JLabel loadingLabel = new JLabel("Opening video...please wait");
        JLabel replayLabel = new JLabel("Restarting video...please wait");
        JLabel errorLabel = new JLabel("Something went wrong");

        loadingLabel.setPreferredSize(new Dimension(200, 50));
        errorLabel.setPreferredSize(new Dimension(200, 50));

        mediaPlayerDialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        mediaPlayerComponent.getMediaPlayer().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void opening(MediaPlayer mediaPlayer) {
                SwingUtilities.invokeLater(() -> {
                    if (!isReplay) {
                        mediaPlayerDialog.setContentPane(loadingLabel);
                        mediaPlayerDialog.setLocationRelativeTo(null);
                        mediaPlayerDialog.pack();
                        mediaPlayerDialog.setVisible(true);
                        isReplay = true;
                    }
                    else {
                        mediaPlayerDialog.setContentPane(replayLabel);
                        mediaPlayerDialog.setLocationRelativeTo(null);
                        mediaPlayerDialog.pack();
                        mediaPlayerDialog.setVisible(true);
                    }
                });
            }

            @Override
            public void playing(MediaPlayer mediaPlayer) {
                SwingUtilities.invokeLater(() -> {
                    mediaPlayerDialog.setVisible(false);
                    labelFullLength.setText(convertTime(mediaPlayerComponent.getMediaPlayer().getLength()));
                });
            }

            @Override
            public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
                SwingUtilities.invokeLater(() -> {
                    labelCurTime.setText(convertTime(mediaPlayer.getTime()));
                    sliderSeek.setValue(Math.round(newPosition * 100));
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

        JButton btnPlay = new JButton("Play");
        btnPlay.addActionListener(e -> mediaPlayerComponent.getMediaPlayer().play());

        JButton btnPause = new JButton("Pause / Resume");
        btnPause.addActionListener(e -> mediaPlayerComponent.getMediaPlayer().pause());

        sliderSeek = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        sliderSeek.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (sliderSeek.getValue() / 100 < 1) {
                    mediaPlayerComponent.getMediaPlayer().setPosition((float) sliderSeek.getValue() / 100);
                    labelCurTime.setText(convertTime(mediaPlayerComponent.getMediaPlayer().getTime()));
                }
            }
        });

        labelCurTime = new JLabel("00:00:00");
        labelFullLength = new JLabel("00:00:00");

        playbackPanel.add(btnReturn);
        playbackPanel.add(btnPlay);
        playbackPanel.add(btnPause);
        playbackPanel.add(labelCurTime);
        playbackPanel.add(sliderSeek);
        playbackPanel.add(labelFullLength);
    }

    private String convertTime(long curTime) {
        double decimalSeconds = (double)curTime/1000;   //curTime is in milliseconds
        int totalSeconds = (int)decimalSeconds;
        int displaySeconds = totalSeconds % 60;
        int totalMinutes = totalSeconds/60;
        int totalHours = totalMinutes/60;
        String strDisplaySeconds = Integer.toString(displaySeconds);
        String strDisplayMinutes = Integer.toString(totalMinutes);
        String strDisplayHours = Integer.toString(totalHours);

        if (displaySeconds < 10)
            strDisplaySeconds = "0" + Integer.toString(displaySeconds);

        if (totalMinutes < 10)
            strDisplayMinutes = "0" + Integer.toString(totalMinutes);

        if (totalHours < 10)
            strDisplayHours = "0" + Integer.toString(totalHours);

        return strDisplayHours + ":" + strDisplayMinutes + ":" + strDisplaySeconds;
    }
}
