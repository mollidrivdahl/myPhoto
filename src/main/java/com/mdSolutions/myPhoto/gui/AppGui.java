package com.mdSolutions.myPhoto.gui;

import com.mdSolutions.myPhoto.MediaCollection;
import com.mdSolutions.myPhoto.MediaItem;
import com.mdSolutions.myPhoto.MyPhoto;
import com.mdSolutions.myPhoto.PhotoMedia;
import lombok.Getter;
import lombok.Setter;

import javax.activity.InvalidActivityException;
import javax.imageio.plugins.jpeg.JPEGHuffmanTable;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.stream.Stream;

public class AppGui {

    public static final int WIN_WIDTH = 1525;
    public static final int WIN_HEIGHT = 900;
    public static final int TITLE_HEIGHT = WIN_HEIGHT / 25;
    public static final int WIN_BORDER_THICKNESS = WIN_HEIGHT / 40;
    public static final int MID_HEIGHT = WIN_HEIGHT - TITLE_HEIGHT - WIN_BORDER_THICKNESS;
    public static final int MENU_WIDTH = WIN_WIDTH / 6;
    public static final int MAIN_WIDTH = WIN_WIDTH - MENU_WIDTH - WIN_BORDER_THICKNESS;
    public static final Color MY_PURPLE = new Color(119, 21, 165);
    public static final Color MY_RED = new Color(242, 33, 65);
    public static final Color MY_BLUE = new Color(3, 147, 155);
    public static final Color MY_GLOW = new Color(203, 205, 145);


    private static AppGui _instance;
    @Getter @Setter private boolean isMultiSelect;
    @Getter @Setter private MyPhoto myPhoto;
    @Getter @Setter private JPanel windowPanel;
    @Getter @Setter private JPanel topPanel;
    @Getter @Setter private JPanel menuPanel;       //left panel
    @Getter @Setter private JPanel gridViewPanel;   //center panel - for containing all MediaItems, nested within centerScrollPane's JViewport
    @Getter @Setter private JScrollPane centerScrollPane;
    @Getter @Setter private JPanel rightPanel;
    @Getter @Setter private JPanel bottomPanel;
    @Getter @Setter private JPanel mediaViewPanel;  //center panel replacement - for viewing & playback of the photos/videos
    @Getter @Setter private JPanel mediaDisplayPanel;   //viewing of photos/videos
    @Getter @Setter private JPanel mediaPlaybackPanel;  //playback of photos/videos
    @Getter @Setter private JPanel photoPlaybackPanel;
    @Getter @Setter private JPanel videoPlaybackPanel;

    public AppGui() {
        myPhoto = new MyPhoto();
        isMultiSelect = false;
        createPanels();
        initializePanels();
    }

    public static AppGui getInstance() {
        if (_instance == null)
            _instance = new AppGui();

        return _instance;
    }

    private void createPanels() {
        windowPanel = new JPanel(new BorderLayout());
        windowPanel.setPreferredSize(new Dimension(WIN_WIDTH, WIN_HEIGHT));

        topPanel = new JPanel();
        topPanel.setBackground(Color.gray);
        topPanel.setPreferredSize(new Dimension(WIN_WIDTH, TITLE_HEIGHT));

        menuPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 30));
        menuPanel.setBackground(Color.gray);
        menuPanel.setPreferredSize(new Dimension(MENU_WIDTH, MID_HEIGHT));

        gridViewPanel = new JPanel(new MyFlowLayout(MyFlowLayout.LEADING, 0, 40));
        gridViewPanel.setBackground(Color.black);
        gridViewPanel.setSize(new Dimension(MAIN_WIDTH, MID_HEIGHT));

        centerScrollPane = new JScrollPane(gridViewPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);    //displays gridViewPanel within centerScrollPane's JViewport
        centerScrollPane.setBorder(BorderFactory.createMatteBorder(5,5,5,5, Color.white));

        rightPanel = new JPanel();
        rightPanel.setBackground(Color.gray);
        rightPanel.setPreferredSize(new Dimension(WIN_BORDER_THICKNESS, MID_HEIGHT));

        bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.gray);
        bottomPanel.setPreferredSize(new Dimension(WIN_WIDTH, WIN_BORDER_THICKNESS));

        mediaViewPanel = new JPanel(new GridBagLayout());
        mediaViewPanel.setBackground(Color.black);
        mediaViewPanel.setPreferredSize(new Dimension(MAIN_WIDTH, MID_HEIGHT));
        GridBagConstraints c = new GridBagConstraints();

        mediaDisplayPanel = new JPanel(new GridBagLayout());
        mediaDisplayPanel.setBackground(Color.black);
        mediaDisplayPanel.setPreferredSize(new Dimension(AppGui.MAIN_WIDTH, AppGui.MID_HEIGHT - 75));
        c.gridx = 0; c.gridy = 0;
        mediaViewPanel.add(mediaDisplayPanel, c);

        mediaPlaybackPanel = new JPanel();
        mediaPlaybackPanel.setBackground(Color.black);
        mediaPlaybackPanel.setBorder(BorderFactory.createMatteBorder(3,0,0,0, MY_RED));
        mediaPlaybackPanel.setPreferredSize(new Dimension(MAIN_WIDTH, 75));
        c.gridx = 0; c.gridy = 1;
        mediaViewPanel.add(mediaPlaybackPanel, c);

        windowPanel.add(topPanel, BorderLayout.PAGE_START);
        windowPanel.add(menuPanel, BorderLayout.LINE_START);
        windowPanel.add(centerScrollPane, BorderLayout.CENTER);
        windowPanel.add(rightPanel, BorderLayout.LINE_END);
        windowPanel.add(bottomPanel, BorderLayout.PAGE_END);
    }

    private void initializePanels() {
        //--add "title" to top panel
        JLabel titleLabel = new JLabel("myPhoto");
        titleLabel.setForeground(Color.white);
        titleLabel.setFont(new Font("sansserif", Font.BOLD, 24));
        topPanel.add(titleLabel);

        //--add "buttons" and "drop locations" to left menu panel
        initializeMenuPanel();

        //--setup "viewing/playback" features of photo & video playback panels
        initializePlaybackPanels();

        //--add root collection's "media items" to center panel
        populateGridView(myPhoto.getCurrentCollection());
    }

    private void initializeMenuPanel() {
        //"create collection" button
        JButton btnCreateCollection = new JButton();
        btnCreateCollection.setText("<html>Create<br/>Collection</html>");
        btnCreateCollection.setPreferredSize(new Dimension(100, 50));
        btnCreateCollection.addActionListener(e -> {
            MediaCollection newCollection;

            //if collection doesn't already exist and not creating within level 4
            if ((newCollection = myPhoto.createCollection()) != null) {
                appendToGridView(newCollection, myPhoto.getCurrentCollection().getListOfChildren().size());
                gridViewPanel.revalidate();
                gridViewPanel.repaint();
            }
        });

        //"navigate up a level" button
        JButton btnNavigateUp = new JButton();
        btnNavigateUp.setText("\u21b0 Go Back Up");
        btnNavigateUp.setPreferredSize(new Dimension(125, 25));
        btnNavigateUp.addActionListener(e -> {
            if (myPhoto.getCurrentCollection().getId() != 1) {
                myPhoto.refreshCurrentCollection(myPhoto.getCurrentCollection().getParentId());
                populateGridView(myPhoto.getCurrentCollection());
            }
        });

        //"import media" button
        JButton btnImport = new JButton();
        btnImport.setText("<html>Import<br/>Media</html>");
        btnImport.setPreferredSize(new Dimension(100, 37));
        btnImport.addActionListener(e -> {
            //TODO: change to importView

            //TODO: provide option for either the browse file system OR drag n drop method of import
            browseFileSystem();
            populateGridView(myPhoto.getCurrentCollection());

            //TODO: change back to gridView of root collection by clicking diff button
        });

        //"multi select" checkbox
        JCheckBox checkMultiSelect = new JCheckBox();
        checkMultiSelect.setText("Multi Select");
        checkMultiSelect.setPreferredSize(new Dimension(100, 25));
        checkMultiSelect.addActionListener(e -> {
            if (!isMultiSelect){
                isMultiSelect = true;
            }
            else {
                isMultiSelect = false;
                myPhoto.getCurrentCollection().unselectAllChildren();
                for (Component gridCell : gridViewPanel.getComponents()) {
                    ((GridCell)gridCell).getDropZonePanel().getPanelDraggable().resetBorder();
                }
            }
        });

        //"organize automatically" label, dropdown combo box, and go button
        JLabel labelAutoOrganize = new JLabel("<html>Auto<br/>Organize:</html>");
        labelAutoOrganize.setPreferredSize(new Dimension(55, 40));
        String[] organizationTypes = {"Name Ascending", "Name Descending", "Collections First", "Collections Last"};
        JComboBox<String> cbAutoOrganize = new JComboBox<>(organizationTypes);
        JButton btnAutoOrganize = new JButton("Go");
        btnAutoOrganize.setPreferredSize(new Dimension(30, 25));
        btnAutoOrganize.setMargin(new Insets(2,2,2,2));
        btnAutoOrganize.addActionListener(e -> {
            int confirmResult = JOptionPane.showConfirmDialog(null,
                    "Are you sure you want to re-organize automatically? All manual organization changes will be removed.",
                    "Warning", JOptionPane.YES_NO_OPTION );

            if (confirmResult == JOptionPane.YES_OPTION) {
                if (cbAutoOrganize.getSelectedIndex() == 0)
                    myPhoto.organizeAutomatically(MyPhoto.AUTO_ORGANIZE_BY.NAME_ASCENDING);
                else if (cbAutoOrganize.getSelectedIndex() == 1)
                    myPhoto.organizeAutomatically(MyPhoto.AUTO_ORGANIZE_BY.NAME_DESCENDING);
                else if (cbAutoOrganize.getSelectedIndex() == 2)
                    myPhoto.organizeAutomatically(MyPhoto.AUTO_ORGANIZE_BY.COLLECTIONS_FIRST);
                else
                    myPhoto.organizeAutomatically(MyPhoto.AUTO_ORGANIZE_BY.COLLECTIONS_LAST);

                populateGridView(myPhoto.getCurrentCollection());
            }
        });

        //"move media out of collection" button
        JButton btnMoveUp = new JButton();
        btnMoveUp.setText("Move Media Up");
        btnMoveUp.setPreferredSize(new Dimension(150, 25));
        btnMoveUp.addActionListener(e -> {
            try {
                myPhoto.moveMediaOut();
                populateGridView(myPhoto.getCurrentCollection());
            }
            catch (InvalidActivityException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage());
            }
        });

        menuPanel.add(btnCreateCollection);
        menuPanel.add(btnNavigateUp);
        menuPanel.add(btnImport);
        menuPanel.add(checkMultiSelect);
        menuPanel.add(labelAutoOrganize);
        menuPanel.add(cbAutoOrganize);
        menuPanel.add(btnAutoOrganize);
        menuPanel.add(btnMoveUp);
    }

    private void initializePlaybackPanels() {
        photoPlaybackPanel = new JPanel();
        videoPlaybackPanel = new JPanel();

        JButton btnGoBack = new JButton("<-- Go Back");
        btnGoBack.addActionListener(e -> {
            //remove the image and viewing playback features from corresponding panels
            mediaDisplayPanel.removeAll();
            mediaPlaybackPanel.remove(photoPlaybackPanel);
            myPhoto.getCurrentCollection().unselectAllChildren();

            //swap the grid view panel back into the center scroll pane
            centerScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            centerScrollPane.setBorder(BorderFactory.createMatteBorder(5,5,5,5, Color.white));
            centerScrollPane.setViewportView(gridViewPanel);
        });

        JButton btnRotateCW = new JButton("Rotate Clockwise");
        btnRotateCW.addActionListener(e -> {
            Stream<MediaItem> selectedMedia = myPhoto.getCurrentCollection().getListOfChildren().stream().filter(MediaItem::isSelected);
            PhotoMedia media = (PhotoMedia)selectedMedia.toArray()[0];

            media.rotate();
            ((JLabel)mediaDisplayPanel.getComponent(0)).setIcon(media.getImage());
        });

        //TODO: add photo rotate and zoom features

        photoPlaybackPanel.setBackground(Color.blue);
        photoPlaybackPanel.add(btnGoBack);
        photoPlaybackPanel.add(btnRotateCW);

        //TODO: add video playback features
    }

    public void populateGridView(MediaCollection gridViewCollection) {
        gridViewPanel.removeAll();

        //iterate over media items from head to tail
        MediaItem travel = gridViewCollection.getHeadItem();
        int index = 0;

        while (travel != null) {
            appendToGridView(travel, index);
            index++;
            travel = travel.getNextItem();
        }

        gridViewPanel.revalidate();
        gridViewPanel.repaint();
    }

    public void appendToGridView(MediaItem addedMediaItem, int index) {
        //create drop zone for media items
        MediaItemDroppable dropZone = new MediaItemDroppable();

        //create draggable media item
        PanelDraggable mediaItemPanel = new PanelDraggable(addedMediaItem, index);

        //add thumbnail/cover photo to draggable media item
        mediaItemPanel.displayImage(addedMediaItem.view());

        //add draggable media item into center of drop zone
        dropZone.addPanelDraggable(mediaItemPanel);

        //add drop zone (& draggable media item) to the grid cell
        GridCell gridCell = new GridCell();
        gridCell.addDropZone(dropZone);

        //add name of media item to grid cell
        gridCell.addItemName(addedMediaItem.getName());

        //add grid cell to grid view
        gridViewPanel.add(gridCell);
    }

    public void browseFileSystem() {

        JFrame explorerFrame = new JFrame();
        JFileChooser c = new JFileChooser();

        c.setFileSelectionMode(JFileChooser.FILES_ONLY);    //default, unnecessary
        c.setMultiSelectionEnabled(true);
        int userChoice = c.showDialog(explorerFrame, "Import...");

        if (userChoice == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = c.getSelectedFiles();

            for (int i = 0; i < selectedFiles.length; i++) {
                System.out.println(selectedFiles[i].getAbsolutePath());
            }

            //copy files into myPhoto directory and follow import procedures
            myPhoto.importMedia(selectedFiles);
        }
        else if (userChoice == JFileChooser.CANCEL_OPTION) { } //do nothing for now
        else {} //JFileChooser.ERROR_OPTION, do nothing for now
    }
}