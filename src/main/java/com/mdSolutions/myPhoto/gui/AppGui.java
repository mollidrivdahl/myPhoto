package com.mdSolutions.myPhoto.gui;

import com.mdSolutions.myPhoto.MediaCollection;
import com.mdSolutions.myPhoto.MediaItem;
import com.mdSolutions.myPhoto.MyPhoto;
import lombok.Getter;
import lombok.Setter;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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


    private static AppGui _instance;
    @Getter @Setter private MyPhoto myPhoto;
    @Getter @Setter private JPanel windowPanel;
    @Getter @Setter private JPanel topPanel;
    @Getter @Setter private JPanel menuPanel;       //left panel
    @Getter @Setter private JPanel gridViewPanel;   //center panel - for containing all MediaItems, nested within centerScrollPane's JViewport
    @Getter @Setter private JScrollPane centerScrollPane;
    @Getter @Setter private JPanel rightPanel;
    @Getter @Setter private JPanel bottomPanel;

    public AppGui() {
        myPhoto = new MyPhoto();
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

    private void initializePanels() {
        //--add "title" to top panel
        JLabel titleLabel = new JLabel("myPhoto");
        titleLabel.setForeground(Color.white);
        titleLabel.setFont(new Font("sansserif", Font.BOLD, 24));
        topPanel.add(titleLabel);

        //--add "buttons" and "drop locations" to left menu panel
        initializeMenuPanel();

        //--add root collection's "media items" to center panel
        populateGridView(myPhoto.getCurrentCollection());
    }

    private void initializeMenuPanel() {
        //"create collection" button
        JButton btnCreateCollection = new JButton();
        btnCreateCollection.setText("<html>Create<br/>Collection</html>");
        btnCreateCollection.setPreferredSize(new Dimension(100, 50));
        btnCreateCollection.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MediaCollection newCollection;

                if ((newCollection = myPhoto.createCollection()) != null) {
                    appendToGridView(newCollection, myPhoto.getCurrentCollection().getListOfChildren().size());
                    gridViewPanel.revalidate();
                    gridViewPanel.repaint();
                }
            }
        });

        //"navigate up a level" button
        JButton btnNavigateUp = new JButton();
        btnNavigateUp.setText("\u21b0 Go Back Up");
        btnNavigateUp.setPreferredSize(new Dimension(125, 25));
        btnNavigateUp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (myPhoto.getCurrentCollection().getId() != 1) {
                    myPhoto.refreshCurrentCollection(myPhoto.getCurrentCollection().getParentId());
                    populateGridView(myPhoto.getCurrentCollection());
                }
            }
        });

        menuPanel.add(btnCreateCollection);
        menuPanel.add(btnNavigateUp);
    }

    public void populateGridView(MediaCollection gridViewCollection) {
        gridViewPanel.removeAll();

        //TODO: change from in-order iteration to iterating from head to tail
        for (int i = 0; i < gridViewCollection.getListOfChildren().size(); i++) {
            appendToGridView(gridViewCollection.getListOfChildren().get(i), i);
        }

        gridViewPanel.revalidate();
        gridViewPanel.repaint();
    }

    public void appendToGridView(MediaItem addedMediaItem, int index) {
        boolean isCollection = addedMediaItem instanceof MediaCollection;

        //create drop zone for media items
        MediaItemDroppable dropZone = new MediaItemDroppable(isCollection);

        //create draggable media item
        PanelDraggable mediaItemPanel = new PanelDraggable(addedMediaItem, index);

        //add thumbnail/cover photo to draggable media item
        mediaItemPanel.displayImage(addedMediaItem.view());

        //add draggable media item into center of drop zone
        dropZone.add(mediaItemPanel);

        //add drop zone (& draggable media item) to the grid cell
        GridCell gridCell = new GridCell();
        gridCell.addDropZone(dropZone);

        //add name of media item to grid cell
        gridCell.addItemName(addedMediaItem.getName());

        //add grid cell to grid view
        gridViewPanel.add(gridCell);
    }
}