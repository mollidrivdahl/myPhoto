package com.mdSolutions.myPhoto.gui;

import com.mdSolutions.myPhoto.*;
import lombok.Getter;
import lombok.Setter;

import javax.activity.InvalidActivityException;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.List;

public class AppGui {

    public static final int WIN_WIDTH = 1525;
    public static final int WIN_HEIGHT = 900;
    public static final int TITLE_HEIGHT = WIN_HEIGHT / 25;
    public static final int WIN_BORDER_THICKNESS = WIN_HEIGHT / 40;
    public static final int MID_HEIGHT = WIN_HEIGHT - TITLE_HEIGHT - WIN_BORDER_THICKNESS;
    public static final int MENU_WIDTH = WIN_WIDTH / 6;
    public static final int MAIN_WIDTH = WIN_WIDTH - MENU_WIDTH - WIN_BORDER_THICKNESS;
    public static final int PLAYBACK_HEIGHT = 50;
    public static final int FB_ACTIONS_HEIGHT = 40;
    public static final Color MY_PURPLE = new Color(119, 21, 165);
    public static final Color MY_PURPLE_LIGHTER = new Color(143, 22, 200);
    public static final Color MY_PURPLE_DARKER = new Color(83, 24, 130);
    public static final Color MY_RED = new Color(242, 33, 65);
    public static final Color MY_BLUE = new Color(3, 147, 155);
    public static final Color MY_GREEN = new Color(56, 182, 46);
    public static final Color MY_ORANGE = new Color(212, 154, 0);
    public static final Color MY_GLOW = new Color(203, 205, 145);


    private static AppGui _instance;
    @Getter @Setter private boolean isMultiSelect;
    @Getter @Setter private MyPhoto myPhoto;
    @Getter @Setter private FbActionsModal fbDialog;

    @Getter @Setter private JPanel windowPanel;
    @Getter @Setter private JPanel topPanel;
    @Getter @Setter private JPanel menuPanel;       //left panel
    @Getter @Setter private JPanel gridViewPanel;   //center panel - for containing all MediaItems, nested within centerScrollPane's JViewport
    @Getter @Setter private JScrollPane centerScrollPane;
    @Getter @Setter private JPanel rightPanel;
    @Getter @Setter private JPanel bottomPanel;

    @Getter @Setter private JPanel importViewPanel; //center panel replacement - for importing medi

    @Getter @Setter private JPanel mediaViewPanel;  //center panel replacement - for viewing & playback of the photos/videos
    @Getter @Setter private JPanel mediaDisplayPanel;   //viewing of photos/videos
    @Getter @Setter private JScrollPane mediaDisplayScrollPane;
    @Getter @Setter private JPanel mediaPlaybackPanel;  //playback of photos/videos
    @Getter @Setter private JPanel photoPlaybackPanel;
    @Getter @Setter private JPanel videoPlaybackPanel;

    @Getter @Setter private JPanel fbViewPanel; //center panel replacement - for viewing fb photo or video share folders
    @Getter @Setter private JPanel fbGridDisplayPanel;  //grid viewing of fb photos or videos
    @Getter @Setter private JScrollPane fbDisplayScrollPane;
    @Getter @Setter private JPanel fbUploadPanel;   //fb login & upload action

    public AppGui() {
        myPhoto = new MyPhoto();
        isMultiSelect = false;
        fbDialog = null;

        createStartupPanels();
        createMediaDisplayPanels();
        createFbDisplayPanels();
        initializePanels();
    }

    public static AppGui getInstance() {
        if (_instance == null)
            _instance = new AppGui();

        return _instance;
    }

    private void createStartupPanels() {
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

        windowPanel.add(topPanel, BorderLayout.PAGE_START);
        windowPanel.add(menuPanel, BorderLayout.LINE_START);
        windowPanel.add(centerScrollPane, BorderLayout.CENTER);
        windowPanel.add(rightPanel, BorderLayout.LINE_END);
        windowPanel.add(bottomPanel, BorderLayout.PAGE_END);
    }

    private void createMediaDisplayPanels() {
        mediaViewPanel = new JPanel(new GridBagLayout());
        mediaViewPanel.setBackground(Color.black);
        mediaViewPanel.setPreferredSize(new Dimension(MAIN_WIDTH, MID_HEIGHT));
        GridBagConstraints c = new GridBagConstraints();

        mediaDisplayPanel = new JPanel(new GridBagLayout());
        mediaDisplayPanel.setBackground(Color.black);
        c.gridx = 0; c.gridy = 0; c.fill = GridBagConstraints.BOTH; c.weightx = 1; c.weighty = 1;

        mediaDisplayScrollPane = new JScrollPane(mediaDisplayPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mediaDisplayScrollPane.setPreferredSize(new Dimension(AppGui.MAIN_WIDTH, AppGui.MID_HEIGHT - PLAYBACK_HEIGHT));
        mediaDisplayScrollPane.setBorder(BorderFactory.createMatteBorder(5,5,3,5, MY_RED));

        mediaViewPanel.add(mediaDisplayScrollPane, c);

        mediaPlaybackPanel = new JPanel();
        mediaPlaybackPanel.setBorder(BorderFactory.createMatteBorder(0,5,5,5, MY_RED));
        mediaPlaybackPanel.setPreferredSize(new Dimension(MAIN_WIDTH, PLAYBACK_HEIGHT));
        c.gridx = 0; c.gridy = 1;

        mediaViewPanel.add(mediaPlaybackPanel, c);
    }

    private void createFbDisplayPanels() {
        fbViewPanel = new JPanel(new GridBagLayout());
        fbViewPanel.setBackground(Color.black);
        fbViewPanel.setPreferredSize(new Dimension(MAIN_WIDTH, MID_HEIGHT));
        GridBagConstraints c = new GridBagConstraints();

        fbGridDisplayPanel = new JPanel(new MyFlowLayout(MyFlowLayout.LEADING, 0, 40));
        fbGridDisplayPanel.setBackground(Color.black);
        fbGridDisplayPanel.setSize(new Dimension(AppGui.MAIN_WIDTH, AppGui.MID_HEIGHT - FB_ACTIONS_HEIGHT));
        c.gridx = 0; c.gridy = 0; c.fill = GridBagConstraints.BOTH; c.weightx = 1; c.weighty = 1;

        fbDisplayScrollPane = new JScrollPane(fbGridDisplayPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        fbDisplayScrollPane.setBorder(BorderFactory.createMatteBorder(5,5,3,5, MY_GREEN));
        fbDisplayScrollPane.setPreferredSize(new Dimension(AppGui.MAIN_WIDTH, AppGui.MID_HEIGHT - FB_ACTIONS_HEIGHT));

        fbViewPanel.add(fbDisplayScrollPane, c);

        fbUploadPanel = new JPanel();
        fbUploadPanel.setBorder(BorderFactory.createMatteBorder(0,5,5,5, MY_GREEN));
        fbUploadPanel.setPreferredSize(new Dimension(MAIN_WIDTH, FB_ACTIONS_HEIGHT));
        c.gridx = 0; c.gridy = 1;

        fbViewPanel.add(fbUploadPanel, c);
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

        //--setup "fb login/upload" features of fb upload panel
        initializeFbUploadPanel();

        //--add root collection's "media items" to center panel
        populateGridView(myPhoto.getCurrentCollection());
    }

    private void initializeMenuPanel() {
        //TODO: Deactivate certain buttons in certain displays

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
            //setup import view
            importViewPanel = new JPanel(new BorderLayout(30, 30));
            importViewPanel.setBackground(Color.black);
            importViewPanel.setPreferredSize(new Dimension(MAIN_WIDTH - 10, MID_HEIGHT - 10));

            //create center panel
            JPanel centerPanelOfLayout = new JPanel(new GridBagLayout());
            centerPanelOfLayout.setBackground(Color.black);
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(50,0,50,0);

            //add button to browse file system
            JButton btnBrowseFileSystem = new JButton("Browse File System...");
            btnBrowseFileSystem.setPreferredSize(new Dimension(200, 100));
            btnBrowseFileSystem.addActionListener(a -> browseFileSystem() );
            c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1; c.gridheight = 1;
            centerPanelOfLayout.add(btnBrowseFileSystem, c);

            //add title to center-center
            JLabel labelImport = new JLabel("\u2191 \u0009 Choose Import Method \u0009 \u2193");
            labelImport.setFont(new Font("Import Title", 1, 20));
            labelImport.setForeground(Color.white);
            c.gridx = 0; c.gridy = 1; c.gridheight = 1;
            centerPanelOfLayout.add(labelImport, c);

            //add drop zone to drag n drop files into app
            JPanel bottomPanelOfCenterPanel = new JPanel(new GridBagLayout());
            bottomPanelOfCenterPanel.setBackground(Color.black);
            GridBagConstraints c2 = new GridBagConstraints();

            JLabel labelDropArea = new JLabel("Drop Files Here:");
            labelDropArea.setForeground(Color.white);
            c2.gridx = 0; c2.gridy = 0; c2.weightx = 1; c2.weighty = 1; c2.gridheight = 1;
            bottomPanelOfCenterPanel.add(labelDropArea, c2);

            JList dropAreaImport = new JList(new DefaultListModel());
            TransferHandler handler =   new TransferHandler() {

                @Override
                public boolean canImport(TransferHandler.TransferSupport info) {
                    // we only import FileList
                    return info.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
                }

                @Override
                public boolean importData(TransferHandler.TransferSupport info) {
                    if (!info.isDrop()) {
                        return false;
                    }

                    // Check for FileList flavor
                    if (!info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        System.out.println("List doesn't accept a drop of this type.");
                        return false;
                    }

                    // Get the fileList that is being dropped.
                    Transferable t = info.getTransferable();
                    List<File> data;
                    try {
                        data = (List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);
                    }
                    catch (Exception e) { return false; }
                    DefaultListModel model = (DefaultListModel) dropAreaImport.getModel();
                    for (File file : data) {
                        model.addElement(file);
                    }
                    return true;
                }
            };
            dropAreaImport.setDragEnabled(true);
            dropAreaImport.setSize(new Dimension(800, 800));
            dropAreaImport.setTransferHandler(handler);
            JScrollPane dropAreaScrollPane = new JScrollPane(dropAreaImport, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            c2.gridx = 0; c2.gridy = 1;
            bottomPanelOfCenterPanel.add(dropAreaScrollPane, c2);

            //add button to import the media files in the drop zone
            JButton btnImportDroppedFiles = new JButton("Import Dropped Files");
            btnImportDroppedFiles.setPreferredSize(new Dimension(160, 25));
            btnImportDroppedFiles.addActionListener(a -> importDroppedFiles(dropAreaImport));
            c2.gridx = 0; c2.gridy = 2;
            bottomPanelOfCenterPanel.add(btnImportDroppedFiles, c2);

            c.gridx = 0; c.gridy = 2; c.gridheight = 3;
            centerPanelOfLayout.add(bottomPanelOfCenterPanel, c);

            importViewPanel.add(centerPanelOfLayout, BorderLayout.CENTER);

            //create left panel
            JPanel leftPanelOfLayout = new JPanel(new GridBagLayout());
            leftPanelOfLayout.setPreferredSize(new Dimension(300, MID_HEIGHT - 10));
            leftPanelOfLayout.setBackground(Color.black);

            //add button to change back to gridView of root collection
            JButton btnGridView = new JButton("<-- Go Back");
            btnGridView.setPreferredSize(new Dimension(150, 25));
            btnGridView.addActionListener(a -> {
                populateGridView(myPhoto.getCurrentCollection());   //current collection already changed to root collection
                centerScrollPane.setBorder(BorderFactory.createMatteBorder(5,5,5,5,Color.white));
                centerScrollPane.setViewportView(gridViewPanel);
            });
            leftPanelOfLayout.add(btnGridView);
            importViewPanel.add(leftPanelOfLayout, BorderLayout.LINE_START);

            //change to importView
            centerScrollPane.setBorder(BorderFactory.createMatteBorder(5,5,5,5,MY_ORANGE));
            centerScrollPane.setViewportView(importViewPanel);
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
                    ((MediaItemDroppable)((GridCell)gridCell).getDropZonePanel()).getPanelDraggable().resetBorder();
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

        FbShareFolderDroppable fbShareFolder = new FbShareFolderDroppable();

        //"duplicate media" button
        JButton btnDuplicate = new JButton();
        btnDuplicate.setText("Duplicate Media");
        btnDuplicate.addActionListener(e -> {
            try {
                myPhoto.createDuplicates();
                populateGridView(myPhoto.getCurrentCollection());
            }
            catch (InvalidActivityException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage());
            }
        });

        //"delete media" button
        JButton btnDelete = new JButton();
        btnDelete.setText("Delete Media");
        btnDelete.addActionListener(e -> {
            int confirmResult = JOptionPane.showConfirmDialog(null,
                    "Are you sure you want to delete media? Any nested media will also deleted.",
                    "Warning", JOptionPane.YES_NO_OPTION );

            if (confirmResult == JOptionPane.YES_OPTION) {
                myPhoto.deleteMedia();
                populateGridView(myPhoto.getCurrentCollection());
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
        menuPanel.add(btnDuplicate);
        menuPanel.add(btnDelete);
        menuPanel.add(fbShareFolder);
    }

    private void initializePlaybackPanels() {
        photoPlaybackPanel = new JPanel();
        videoPlaybackPanel = new JPanel();  //updated later within MyMediaPlayer

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

        JSlider sliderZoom = new JSlider(JSlider.HORIZONTAL, 1, 10, 2);
        sliderZoom.addChangeListener(e -> {
            JSlider source = (JSlider)e.getSource();

            if (!source.getValueIsAdjusting()) {
                Stream<MediaItem> selectedMedia = myPhoto.getCurrentCollection().getListOfChildren().stream().filter(MediaItem::isSelected);
                PhotoMedia media = (PhotoMedia) selectedMedia.toArray()[0];

                media.zoom(sliderZoom.getValue());
                ((JLabel) mediaDisplayPanel.getComponent(0)).setIcon(media.getImage());
            }
        });

        photoPlaybackPanel.add(btnGoBack);
        photoPlaybackPanel.add(btnRotateCW);
        photoPlaybackPanel.add(sliderZoom);
    }

    private void initializeFbUploadPanel() {
        JButton btnGoBack = new JButton("<-- Go Back");
        btnGoBack.addActionListener(e -> {
            //swap the grid view panel back into the center scroll pane
            centerScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            centerScrollPane.setBorder(BorderFactory.createMatteBorder(5,5,5,5, Color.white));
            centerScrollPane.setViewportView(gridViewPanel);

            FbMediaUploader.getInstance().setUploadType(null);
        });

        JButton btnUploadMedia = new JButton("Upload Media");
        btnUploadMedia.setBackground(MY_GREEN);
        btnUploadMedia.addActionListener(e -> {
            if (fbDialog == null)
                fbDialog = new FbActionsModal();

            if (fbGridDisplayPanel.getComponents().length > 0)
                fbDialog.display();
            else
                JOptionPane.showMessageDialog(null, "Must have at least one item to upload");
        });

        fbUploadPanel.add(btnGoBack);
        fbUploadPanel.add(btnUploadMedia);
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

    private void browseFileSystem() {

        JFrame explorerFrame = new JFrame();
        String userHomeDir = System.getProperty("user.home");
        JFileChooser c = new JFileChooser(userHomeDir + "\\Pictures");

        c.setFileSelectionMode(JFileChooser.FILES_ONLY);    //default, unnecessary
        c.setMultiSelectionEnabled(true);
        int userChoice = c.showDialog(explorerFrame, "Import...");

        if (userChoice == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = c.getSelectedFiles();

            for (int i = 0; i < selectedFiles.length; i++) {
                System.out.println(selectedFiles[i].getAbsolutePath());
            }

            //copy files into myPhoto directory and follow import procedures
            ArrayList<String> failedImports = myPhoto.importMedia(selectedFiles);    //returns list of files that failed import

            if (failedImports.size() > 0)
                JOptionPane.showMessageDialog(null, "Failed to import at least one of each of the following files:\n "
                        + failedImports.toString());
        }
        else if (userChoice == JFileChooser.CANCEL_OPTION) { } //do nothing for now
        else {} //JFileChooser.ERROR_OPTION, do nothing for now
    }

    private void importDroppedFiles(JList fileList) {
        DefaultListModel fileListModel = (DefaultListModel) fileList.getModel();
        ArrayList<File> droppedFiles = new ArrayList<>();

        if (fileListModel.getSize() > 0) {
            for (int i = 0; i < fileListModel.getSize(); i++) {
                File file = new File(fileListModel.getElementAt(i).toString());
                droppedFiles.add(file);
            }

            //copy files into myPhoto directory and follow import procedures
            ArrayList<String> failedImports = myPhoto.importMedia(droppedFiles.toArray(new File[]{}));    //returns list of files that failed import

            if (failedImports.size() > 0)
                JOptionPane.showMessageDialog(null, "Failed to import at least one of each of the following files:\n "
                        + failedImports.toString());

            fileListModel.clear();
        }
    }

    public void openAltApplication(MediaItem media) {

        JFrame frame = new JFrame();
        JFileChooser c = new JFileChooser("C:\\");

        FileFilter filter = new FileNameExtensionFilter("exe", "exe");
        c.setFileFilter(filter);
        c.setMultiSelectionEnabled(false);
        int userChoice = c.showDialog(frame, "Open With...");

        if (userChoice == JFileChooser.APPROVE_OPTION) {
            String filename = (c.getSelectedFile().getName());
            String dir = (c.getCurrentDirectory().toString());

            Runtime runtime = Runtime.getRuntime();     //getting Runtime object

            try
            {
                String program = dir + "/" + filename;

                runtime.exec(new String[] { program , media.getRelPath() });

            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }

        }
        else if (userChoice == JFileChooser.CANCEL_OPTION) { }  //do nothing for now
        else {} //JFileChoose.ERROR_OPTION, do nothing for now
    }
}