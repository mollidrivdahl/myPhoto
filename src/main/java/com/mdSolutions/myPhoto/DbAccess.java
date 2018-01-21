package com.mdSolutions.myPhoto;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Hashtable;

public class DbAccess {

    private static final String CREATE_TABLE_MEDIA_ITEM = String.format("CREATE TABLE MediaItem (Id INTEGER PRIMARY KEY, Name VARCHAR(256) NOT NULL, RelPath VARCHAR(256) NOT NULL, ParentId INTEGER REFERENCES MediaItem (Id) ON DELETE CASCADE, NextItemId INTEGER REFERENCES MediaItem (Id) ON DELETE CASCADE, PrevItemId INTEGER REFERENCES MediaItem (Id) ON DELETE CASCADE, LevelNum INT NOT NULL );");
    private static final String CREATE_TABLE_COLLECTION = String.format("CREATE TABLE Collection (Id INTEGER PRIMARY KEY REFERENCES MediaItem (Id) ON DELETE CASCADE, CoverPhotoPath VARCHAR(256) NOT NULL);");

    private static DbAccess _instance;
    private Connection dbConnection;
    //TODO: declare some PreparedStatements

    private DbAccess() {
        dbConnection = null;

        //TODO: initialize PreparedStatements to behave as stored procedures
    }

    public static DbAccess getInstance() {
        if (_instance == null) {
            _instance = new DbAccess();
            _instance.establishConnection("myPhotoDb");
        }

        return _instance;
    }

    private Connection establishConnection(String fileName) {

        String url = "jdbc:sqlite:" + fileName;
        boolean hasData = new File(fileName).exists();  //indicates whether db file already exists with saved data

        try {
            dbConnection = DriverManager.getConnection(url);

            if (dbConnection != null) {
                System.out.println("Database connected");

                if (!hasData)
                    setupDb();
            }
        }
        catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        return dbConnection;
    }

    private void setupDb() {
        System.out.println("setup Db");

        Statement stmt = null;

        try {
            stmt = dbConnection.createStatement();
            stmt.executeUpdate(CREATE_TABLE_MEDIA_ITEM);
            stmt.executeUpdate(CREATE_TABLE_COLLECTION);
            stmt.executeUpdate(String.format("INSERT INTO MediaItem (Id, Name, RelPath, ParentId, NextItemId, PrevItemId, LevelNum)" +
                    "VALUES (1, \'myPhotoLibrary\', \'myPhotoLibrary/\', null, null, null, 0);"));
            stmt.executeUpdate(String.format("INSERT INTO Collection (Id, CoverPhotoPath) VALUES (1, \'\');"));
        }
        catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        finally {
                if (stmt != null)
                    try { stmt.close(); } catch (SQLException ex) {}
        }

        //TODO Eventually: Run through directory and auto-import existing media,
        //or ask for confirmation to remove existing media
    }

    public void closeConnection() {         //ADDED
        try {
            if (dbConnection != null) {
                dbConnection.close();
                System.out.println("Database closed");
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public MediaCollection getRootCollection() {
        MediaCollection rootCollection = new MediaCollection();
        refreshCurrentCollection(rootCollection, 1);    //root collection is always id = 1

        //old code, replaced by refreshCurrentCollection()
        /*Statement query = null;
        String queryStr = String.format("SELECT * FROM MediaItem JOIN Collection ON MediaItem.Id = Collection.Id WHERE LevelNum = 0;");

        try {
            query = dbConnection.createStatement();
            ResultSet rs = query.executeQuery(queryStr);

            while (rs.next()) { //should only loop once (only one root collection)
                rootCollection.setId((Integer)rs.getObject("Id"));
                rootCollection.setName(rs.getString("Name"));
                rootCollection.setRelPath(rs.getString("RelPath"));
                rootCollection.setParentId((Integer)rs.getObject("ParentId"));
                rootCollection.setLevelNum(rs.getInt("LevelNum"));
                rootCollection.setCoverPhotoPath(rs.getString("CoverPhotoPath"));
            }

            //query for all the children of this root collection to update it's listOfChildren, headItem, & tailItem
            queryStr = String.format("SELECT * FROM MediaItem LEFT JOIN Collection ON MediaItem.Id = Collection.Id WHERE ParentId = 1;");
            rs = query.executeQuery(queryStr);
            Hashtable<Integer, MediaItem> tempItems = new Hashtable<Integer, MediaItem>();

            while (rs.next()) { //loops for each child media of root collection
                Integer curItemId = (Integer)rs.getObject("Id");
                Integer prevItemId = (Integer)rs.getObject("PrevItemId");
                Integer nextItemId = (Integer)rs.getObject("NextItemId");
                MediaItem curMedia = tempItems.get(curItemId);
                MediaItem prevMedia = null;
                MediaItem nextMedia = null;

                if (curMedia == null) {
                    //TODO: determine concrete type (don't assume it's a MediaCollection) (Add type indicator to database)
                    curMedia = new MediaCollection(curItemId);
                    tempItems.put(curItemId, curMedia);
                }

                if (prevItemId != null && ((prevMedia = tempItems.get(prevItemId)) == null) ) {
                    //TODO: determine concrete type (don't assume it's a MediaCollection)
                    prevMedia = new MediaCollection(prevItemId);
                    tempItems.put(prevItemId, prevMedia);
                }

                if (nextItemId != null && ((nextMedia = tempItems.get(nextItemId)) == null) ) {
                    //TODO: determine concrete type (don't assume it's a MediaCollection)
                    nextMedia = new MediaCollection(nextItemId);
                    tempItems.put(nextItemId, nextMedia);
                }

                curMedia.setName(rs.getString("Name"));
                curMedia.setRelPath(rs.getString("RelPath"));
                curMedia.setParentId((Integer)rs.getObject("ParentId"));
                curMedia.setParentCollectionPath(rootCollection.getRelPath());
                curMedia.setLevelNum(rs.getInt("LevelNum"));

                if (curMedia instanceof MediaCollection)
                    ((MediaCollection)curMedia).setCoverPhotoPath(rs.getString("CoverPhotoPath"));

                if (prevItemId == null) {
                    rootCollection.setHeadItem(curMedia);   //should be hit exactly once
                    curMedia.setPreviusItem(null);
                }
                else
                    curMedia.setPreviusItem(prevMedia);

                if (nextItemId == null) {
                    rootCollection.setTailItem(curMedia);   //should be hit exactly once
                    curMedia.setNextItem(null);
                }
                else
                    curMedia.setNextItem(nextMedia);
            }

            //copy media items from the temp hashtable to the rootCollection's listOfChildren
            rootCollection.setListOfChildren(new ArrayList<MediaItem>(tempItems.values()));
        }
        catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        finally {
            if (query != null)
                try { query.close(); } catch (SQLException ex) {}
        }*/

        return rootCollection;
    }

    public int addNewCollection(MediaCollection newCollection) {
        Statement stmt = null;
        int newId = -1;
        MediaItem prevItem = newCollection.getPreviusItem();
        String prevItemId = (prevItem != null) ? prevItem.getId().toString() : "null";

        try {
            stmt = dbConnection.createStatement();

            //insert new collection into db
            stmt.executeUpdate(String.format("INSERT INTO MediaItem(Name, RelPath, ParentId, NextItemId, PrevItemId, LevelNum)" +
                    "VALUES(\'" + newCollection.getName() + "\',\'" + newCollection.getRelPath() + "\'," + newCollection.getParentId() +
                    ", null ," + prevItemId + "," + newCollection.getLevelNum() + ");"));

            //retrieve value for id of newly inserted collection
            newId = stmt.getGeneratedKeys().getInt(1);
            System.out.println("** rowId = " + newId + "**");

            //insert new collection into referencing table in db
            stmt.executeUpdate(String.format("INSERT INTO Collection(Id, CoverPhotoPath)" +
                    "VALUES(" + newId + ",\'" + newCollection.getCoverPhotoPath() + "\');"));

            //update previous item in parent collection to point to new collection as its next item
            if (!prevItemId.equals("null"))
                stmt.executeUpdate(String.format("UPDATE MediaItem SET NextItemId = " + newId + " WHERE Id = " + prevItemId + ";"));

            /*ResultSet details = stmt.executeQuery(String.format("SELECT * FROM MediaItem"));
            while (details.next()) {
                System.out.println(details.getObject("Id"));
                System.out.println(details.getString("Name"));
                System.out.println(details.getString("RelPath"));
                System.out.println(details.getObject("ParentId"));
                System.out.println(details.getObject("NextItemId"));
                System.out.println(details.getObject("PrevItemId"));
                System.out.println(details.getInt("LevelNum"));

                System.out.println("------------------");
            }*/
        }
        catch (SQLException ex) {
            System.out.println(ex.getMessage());

            //TODO: rollback changes?
        }
        finally {
            if (stmt != null)
                try { stmt.close(); } catch (SQLException ex) {}
        }

        return newId;
    }

    public void refreshCurrentCollection(MediaCollection currentCollection, Integer id) {
        Statement query = null;
        String queryStr = String.format("SELECT * FROM MediaItem JOIN Collection ON MediaItem.Id = Collection.Id WHERE MediaItem.Id = " + id + ";");

        try {
            query = dbConnection.createStatement();
            ResultSet rs = query.executeQuery(queryStr);

            while (rs.next()) { //should only loop once (only one root collection)
                currentCollection.setId((Integer)rs.getObject("Id"));
                currentCollection.setName(rs.getString("Name"));
                currentCollection.setRelPath(rs.getString("RelPath"));
                currentCollection.setParentId((Integer)rs.getObject("ParentId"));
                currentCollection.setLevelNum(rs.getInt("LevelNum"));
                currentCollection.setCoverPhotoPath(rs.getString("CoverPhotoPath"));
            }

            //query for parent collection path
            if (currentCollection.getParentId() != null) {
                queryStr = String.format("SELECT RelPath FROM MediaItem WHERE Id = " + currentCollection.getParentId() + ";");
                rs = query.executeQuery(queryStr);
                currentCollection.setParentCollectionPath(rs.getString("RelPath"));
            }

            //query for all the children of this root collection to update it's listOfChildren, headItem, & tailItem
            queryStr = String.format("SELECT * FROM MediaItem LEFT JOIN Collection ON MediaItem.Id = Collection.Id WHERE ParentId = " + id + ";");
            rs = query.executeQuery(queryStr);
            Hashtable<Integer, MediaItem> tempItems = new Hashtable<Integer, MediaItem>();

            while (rs.next()) { //loops for each child media of root collection
                Integer curItemId = (Integer)rs.getObject("Id");
                Integer prevItemId = (Integer)rs.getObject("PrevItemId");
                Integer nextItemId = (Integer)rs.getObject("NextItemId");
                MediaItem curMedia = tempItems.get(curItemId);
                MediaItem prevMedia = null;
                MediaItem nextMedia = null;

                if (curMedia == null) {
                    //TODO: determine concrete type (don't assume it's a MediaCollection) (Add type indicator to database)
                    curMedia = new MediaCollection(curItemId);
                    tempItems.put(curItemId, curMedia);
                }

                if (prevItemId != null && ((prevMedia = tempItems.get(prevItemId)) == null) ) {
                    //TODO: determine concrete type (don't assume it's a MediaCollection)
                    prevMedia = new MediaCollection(prevItemId);
                    tempItems.put(prevItemId, prevMedia);
                }

                if (nextItemId != null && ((nextMedia = tempItems.get(nextItemId)) == null) ) {
                    //TODO: determine concrete type (don't assume it's a MediaCollection)
                    nextMedia = new MediaCollection(nextItemId);
                    tempItems.put(nextItemId, nextMedia);
                }

                curMedia.setName(rs.getString("Name"));
                curMedia.setRelPath(rs.getString("RelPath"));
                curMedia.setParentId((Integer)rs.getObject("ParentId"));
                curMedia.setParentCollectionPath(currentCollection.getRelPath());
                curMedia.setLevelNum(rs.getInt("LevelNum"));

                if (curMedia instanceof MediaCollection)
                    ((MediaCollection)curMedia).setCoverPhotoPath(rs.getString("CoverPhotoPath"));

                if (prevItemId == null) {
                    currentCollection.setHeadItem(curMedia);   //should be hit exactly once
                    curMedia.setPreviusItem(null);
                }
                else
                    curMedia.setPreviusItem(prevMedia);

                if (nextItemId == null) {
                    currentCollection.setTailItem(curMedia);   //should be hit exactly once
                    curMedia.setNextItem(null);
                }
                else
                    curMedia.setNextItem(nextMedia);
            }

            //copy media items from the temp hashtable to the currentCollections's listOfChildren
            currentCollection.setListOfChildren(new ArrayList<MediaItem>(tempItems.values()));
        }
        catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        finally {
            if (query != null)
                try { query.close(); } catch (SQLException ex) {}
        }
    }
}