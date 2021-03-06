package com.mdSolutions.myPhoto;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.stream.Stream;

public class DbAccess {

    private static final String CREATE_TABLE_MEDIA_ITEM = String.format("CREATE TABLE MediaItem (Id INTEGER PRIMARY KEY, Name VARCHAR(256) NOT NULL, RelPath VARCHAR(256) NOT NULL, ParentId INTEGER REFERENCES MediaItem (Id), NextItemId INTEGER REFERENCES MediaItem (Id), PrevItemId INTEGER REFERENCES MediaItem (Id), LevelNum INT NOT NULL, MediaType VARCHAR(12) NOT NULL CHECK (MediaType IN ('Photo', 'Video', 'Unsupported', 'Collection')) );");
    private static final String CREATE_TABLE_COLLECTION = String.format("CREATE TABLE Collection (Id INTEGER PRIMARY KEY REFERENCES MediaItem (Id), CoverPhotoItem INTEGER REFERENCES MediaItem (Id) NOT NULL);");

    private static DbAccess _instance;
    private Connection dbConnection;

    private DbAccess() {
        dbConnection = null;

        //if time permits
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
            stmt.executeUpdate(String.format("INSERT INTO MediaItem (Id, Name, RelPath, ParentId, NextItemId, PrevItemId, LevelNum, MediaType)" +
                    "VALUES (1, \'myPhotoLibrary\', \'myPhotoLibrary/\', null, null, null, 0, \'Collection\');"));
            stmt.executeUpdate(String.format("INSERT INTO Collection (Id, CoverPhotoItem) VALUES (1, 1);"));

            //add 'myPhotoLibrary' directory to project folder, if doesn't exist
            if (!MyPhoto.FileSystemAccess.fileExists("myPhotoLibrary"))
                MyPhoto.FileSystemAccess.createDirectory("myPhotoLibrary");
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

    public void closeConnection() {
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

        return rootCollection;
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
                currentCollection.setCoverPhotoItem((Integer)rs.getObject("CoverPhotoItem"));
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
                    //determine concrete type
                    curMedia = MediaItem.getConcreteType(rs.getString("MediaType"));
                    curMedia.setId(curItemId);

                    //curMedia = new MediaCollection(curItemId);
                    tempItems.put(curItemId, curMedia);
                }

                if (prevItemId != null && ((prevMedia = tempItems.get(prevItemId)) == null) ) {
                    //determine concrete type (don't assume it's a MediaCollection)
                    Statement newQuery = dbConnection.createStatement();;
                    ResultSet prevRs = newQuery.executeQuery(String.format("SELECT MediaType FROM MediaItem WHERE Id = " + prevItemId + ";"));
                    prevMedia = MediaItem.getConcreteType(prevRs.getString("MediaType"));
                    prevMedia.setId(prevItemId);

                    tempItems.put(prevItemId, prevMedia);
                }

                if (nextItemId != null && ((nextMedia = tempItems.get(nextItemId)) == null) ) {
                    //determine concrete type (don't assume it's a MediaCollection)
                    Statement newQuery = dbConnection.createStatement();;
                    ResultSet nextRs = newQuery.executeQuery(String.format("SELECT MediaType FROM MediaItem WHERE Id = " + nextItemId + ";"));
                    nextMedia = MediaItem.getConcreteType(nextRs.getString("MediaType"));
                    nextMedia.setId(nextItemId);

                    tempItems.put(nextItemId, nextMedia);
                }

                curMedia.setName(rs.getString("Name"));
                curMedia.setRelPath(rs.getString("RelPath"));
                curMedia.setParentId((Integer)rs.getObject("ParentId"));
                curMedia.setParentCollectionPath(currentCollection.getRelPath());
                curMedia.setLevelNum(rs.getInt("LevelNum"));

                if (curMedia instanceof MediaCollection)
                    ((MediaCollection)curMedia).setCoverPhotoItem((Integer)rs.getObject("CoverPhotoItem"));

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

            //find cover photo item of current collection (if applicable) and set isCoverPhoto to true
            if (tempItems.containsKey(currentCollection.getCoverPhotoItem()))
                ((IndividualMedia) tempItems.get(currentCollection.getCoverPhotoItem())).setCoverPhoto(true);

            //copy media items from the temp hashtable to the currentCollections's listOfChildren
            currentCollection.setListOfChildren(new ArrayList<>(tempItems.values()));
        }
        catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        finally {
            if (query != null)
                try { query.close(); } catch (SQLException ex) {}
        }
    }

    //Note: Gets details about the media item except for: children, head, tail, next, or previous
    public MediaCollection getMediaById(Integer id) {
        MediaCollection requestedCollection = new MediaCollection();

        Statement query = null;
        String queryStr = String.format("SELECT * FROM MediaItem JOIN Collection ON MediaItem.Id = Collection.Id WHERE MediaItem.Id = " + id + ";");

        try {
            query = dbConnection.createStatement();
            ResultSet rs = query.executeQuery(queryStr);

            while (rs.next()) { //should only loop once (only one collection with requested id)
                requestedCollection.setId((Integer)rs.getObject("Id"));
                requestedCollection.setName(rs.getString("Name"));
                requestedCollection.setRelPath(rs.getString("RelPath"));
                requestedCollection.setParentId((Integer)rs.getObject("ParentId"));
                requestedCollection.setLevelNum(rs.getInt("LevelNum"));
                requestedCollection.setCoverPhotoItem((Integer)rs.getObject("CoverPhotoItem"));
            }

            //query for parent collection path
            if (requestedCollection.getParentId() != null) {
                queryStr = String.format("SELECT RelPath FROM MediaItem WHERE Id = " + requestedCollection.getParentId() + ";");
                rs = query.executeQuery(queryStr);
                requestedCollection.setParentCollectionPath(rs.getString("RelPath"));
            }
        }
        catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        finally {
            if (query != null)
                try { query.close(); } catch (SQLException ex) {}
        }

        return requestedCollection;
    }

    //Only adds the collection to the database, not the child media of the collection
    //Child media should be added via appendNewChildMedia()
    public int addNewCollection(MediaCollection newCollection) {
        Statement stmt = null;
        int newId = -1;
        MediaItem prevItem = newCollection.getPreviusItem();
        String prevItemId = (prevItem != null) ? prevItem.getId().toString() : "null";

        try {
            stmt = dbConnection.createStatement();

            //insert new collection into db
            stmt.executeUpdate(String.format("INSERT INTO MediaItem(Name, RelPath, ParentId, NextItemId, PrevItemId, LevelNum, MediaType)" +
                    "VALUES(\'" + newCollection.getName() + "\',\'" + newCollection.getRelPath() + "\'," + newCollection.getParentId() +
                    ", null ," + prevItemId + "," + newCollection.getLevelNum() + ", \'Collection\' );"));

            //retrieve value for id of newly inserted collection
            newId = stmt.getGeneratedKeys().getInt(1);

            //insert new collection into referencing table in db
            stmt.executeUpdate(String.format("INSERT INTO Collection(Id, CoverPhotoItem)" +
                    "VALUES(" + newId + "," + newCollection.getCoverPhotoItem() + ");"));

            //update previous item in parent collection to point to new collection as its next item
            if (!prevItemId.equals("null"))
                stmt.executeUpdate(String.format("UPDATE MediaItem SET NextItemId = " + newId + " WHERE Id = " + prevItemId + ";"));

//            ResultSet details = stmt.executeQuery(String.format("SELECT * FROM MediaItem"));
//            while (details.next()) {
//                System.out.println(details.getObject("Id"));
//                System.out.println(details.getString("Name"));
//                System.out.println(details.getString("RelPath"));
//                System.out.println(details.getObject("ParentId"));
//                System.out.println(details.getObject("NextItemId"));
//                System.out.println(details.getObject("PrevItemId"));
//                System.out.println(details.getInt("LevelNum"));
//
//                System.out.println("------------------");
//            }
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

    //as opposed to appendExistingChildMedia, where there aren't insert operations but rather updates
    public void appendNewChildMedia(MediaCollection updatedCollection) {
        Statement stmt = null;
        Integer newId = -1;
        Integer firstPrevId = null;

        if (updatedCollection.getHeadItem() == null)
            return;

        try {
            //get tail item of collection from database, if exists
            stmt = dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery(String.format("SELECT Id FROM MediaItem WHERE ParentId = " + updatedCollection.getId() + " AND NextItemId is null;"));
            if (rs.next()) {
                firstPrevId = ((Integer)rs.getObject("Id"));
            }

            //**add head mediaItem to database with db tail id (or null) as its prevId and null as its nextId**
            MediaItem head = updatedCollection.getHeadItem();

            if (head instanceof MediaCollection) {
                if (firstPrevId == null)
                    head.setPreviusItem(null);
                else
                    head.setPreviusItem(new MediaCollection(firstPrevId));

                //insert new collection into db
                newId = addNewCollection((MediaCollection)head);
            }
            else {
                //insert new media item into db
                String prevItemId = (firstPrevId != null) ? firstPrevId.toString() : "null";
                String type = MediaItem.getConcreteType(head);

                stmt.executeUpdate(String.format("INSERT INTO MediaItem(Name, RelPath, ParentId, NextItemId, PrevItemId, LevelNum, MediaType)" +
                        "VALUES(\'" + head.getName() + "\',\'" + head.getRelPath() + "\'," + head.getParentId() +
                        ", null ," + prevItemId + "," + head.getLevelNum() + ", \'" + type + "\' );"));

                //retrieve value for id of newly inserted media item
                newId = stmt.getGeneratedKeys().getInt(1);

                //update previous item in parent collection to point to new item as its next item
                if (firstPrevId != null)
                    stmt.executeUpdate(String.format("UPDATE MediaItem SET NextItemId = " + newId + " WHERE Id = " + firstPrevId + ";"));
            }

            //**continue looping from head+1 to tail**
            String prevItemId = newId.toString();
            MediaItem travel = head.getNextItem();

            while (travel != null) {
                if (travel instanceof MediaCollection)
                    newId = addNewCollection((MediaCollection)travel);
                else {
                    //add to db with nextId always null
                    String type = MediaItem.getConcreteType(travel);

                    stmt.executeUpdate(String.format("INSERT INTO MediaItem(Name, RelPath, ParentId, NextItemId, PrevItemId, LevelNum, MediaType)" +
                            "VALUES(\'" + travel.getName() + "\',\'" + travel.getRelPath() + "\'," + travel.getParentId() +
                            ", null ," + prevItemId + "," + travel.getLevelNum() + ", \'" + type + "\' );"));

                    //retrieve value for id of newly inserted media item
                    newId = stmt.getGeneratedKeys().getInt(1);

                    //update previous item in parent collection to point to new item as its next item
                    stmt.executeUpdate(String.format("UPDATE MediaItem SET NextItemId = " + newId + " WHERE Id = " + prevItemId + ";"));

                    prevItemId = newId.toString();
                    travel = travel.getNextItem();
                }
            }

        }
        catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void updateChildMediaArrangement(MediaCollection currentCollection) {
        //update next and previous id's of each item in collection, from head to tail
        MediaItem travel = currentCollection.getHeadItem();
        String nextId = "null";
        String prevId = "null";
        Statement stmt = null;

        try {
            stmt = dbConnection.createStatement();

            while (travel != null) {
                nextId = travel.getNextItem() == null ? "null" : travel.getNextItem().getId().toString();
                prevId = travel.getPreviusItem() == null ? "null" : travel.getPreviusItem().getId().toString();

                stmt.executeUpdate(String.format("UPDATE MediaItem SET NextItemId = " + nextId + " , PrevItemId = "
                        + prevId + " WHERE Id = " + travel.getId() + ";"));

                travel = travel.getNextItem();
            }
        }
        catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

    }

    //as opposed to appendNewChildMedia, where there aren't update operations but rather inserts
    public void appendExistingChildMedia(MediaCollection destCollection, Integer originalParentId, boolean isMoveDown) {
        Statement stmt;
        Integer firstPrevId = null;
        ArrayList<MediaCollection> nestedCollections = new ArrayList<>();

        if (destCollection.getHeadItem() == null)
            return;

        try {
            stmt = dbConnection.createStatement();

            //get tail item of collection from database, if exists
            ResultSet rs = stmt.executeQuery(String.format("SELECT Id FROM MediaItem WHERE ParentId = " + destCollection.getId() + " AND NextItemId is null;"));
            if (rs.next()) {
                firstPrevId = ((Integer)rs.getObject("Id"));
            }

            MediaItem travel = destCollection.getHeadItem();

            while (travel != null) {
                //connect list of appended items to back of existing items in collection
                if (travel == destCollection.getHeadItem() && firstPrevId != null)
                {
                    stmt.executeUpdate(String.format("UPDATE MediaItem SET NextItemId = " + travel.getId() + " WHERE Id = " + firstPrevId + ";"));
                    stmt.executeUpdate(String.format("UPDATE MediaItem SET PrevItemId = " + firstPrevId + " WHERE Id = " + travel.getId() + ";"));
                }
                else if (travel == destCollection.getHeadItem() && firstPrevId == null)
                    stmt.executeUpdate(String.format("UPDATE MediaItem SET PrevItemId = null WHERE Id = " + travel.getId() + ";"));
                //list of appended items is first of items in collection
                else
                    stmt.executeUpdate(String.format("UPDATE MediaItem SET PrevItemId = " + travel.getPreviusItem().getId() + " WHERE Id = " + travel.getId() + ";"));

                //connect to the next media item
                if (travel.getNextItem() != null)
                    stmt.executeUpdate(String.format("UPDATE MediaItem SET NextItemId = " + travel.getNextItem().getId() + " WHERE Id = " + travel.getId() + ";"));
                else
                    stmt.executeUpdate(String.format("UPDATE MediaItem SET NextItemId = null WHERE Id = " + travel.getId() + ";"));

                stmt.executeUpdate(String.format("UPDATE MediaItem SET RelPath = \'" + travel.getRelPath() + "\' , ParentId = " + travel.getParentId() +
                        " , LevelNum = " + travel.getLevelNum() + " WHERE Id = " + travel.getId() + ";"));

                if (travel instanceof MediaCollection)
                    nestedCollections.add((MediaCollection) travel);
                else if (((IndividualMedia)travel).isCoverPhoto())
                    updateCoverPhoto(originalParentId, 1);  //reset parent collection cover photo id to be 1

                travel = travel.getNextItem();
            }

            //update info for all nested collections
            for (MediaCollection collection : nestedCollections) {
                updateChildMediaDetailsRecursive(collection.getId(), collection.getRelPath(), collection.getLevelNum(), isMoveDown);
            }
        }
        catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

    }

    private void updateChildMediaDetailsRecursive(Integer collectionId, String collectionPath, int collectionLevel, boolean isMoveDown) {
        Statement stmtQuery;
        Statement stmtUpdate;
        Hashtable<Integer, String> nestedCollectionDetails = new Hashtable<>();

        Integer childId;
        String childName;
        String newChildPath;
        String childMediaType;

        try {
            stmtQuery = dbConnection.createStatement();
            stmtUpdate = dbConnection.createStatement();

            //retrieve all children from database
            ResultSet rs = stmtQuery.executeQuery(String.format("SELECT Id, Name, MediaType FROM MediaItem WHERE ParentId = " + collectionId + ";"));
            while (rs.next()) {
                childId = (Integer)rs.getObject("Id");
                childName = rs.getString("Name");
                childMediaType = rs.getString("MediaType");

                if (childMediaType.equals("Collection")) {
                    newChildPath = collectionPath + childName + "/";
                    nestedCollectionDetails.put(childId, newChildPath);
                }
                else
                    newChildPath = collectionPath + childName;

                //update all children
                if (isMoveDown)
                    stmtUpdate.executeUpdate(String.format("UPDATE MediaItem SET RelPath = \'" + newChildPath + "\' , LevelNum = "
                        + (collectionLevel + 1) + " WHERE Id = " + childId + ";"));
                else
                    stmtUpdate.executeUpdate(String.format("UPDATE MediaItem SET RelPath = \'" + newChildPath + "\' , LevelNum = "
                        + (collectionLevel - 1) + " WHERE Id = " + childId + ";"));
            }

            //update info for all nested collections
            nestedCollectionDetails.forEach((id, path) -> {
                if (isMoveDown)
                    updateChildMediaDetailsRecursive(id, path, collectionLevel + 1, isMoveDown);
                else
                    updateChildMediaDetailsRecursive(id, path, collectionLevel - 1, isMoveDown);
            });
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public boolean isNestingAllowed(Object[] nestedMedia) {
        Statement stmt;
        ArrayList<Integer> nestedIds = new ArrayList<>();

        try {
            stmt = dbConnection.createStatement();

            for (int i = 0; i < nestedMedia.length; i++) {
                if (nestedMedia[i] instanceof MediaCollection) {
                    if (((MediaCollection)nestedMedia[i]).levelNum == 3)   //implies collection would be moved down to level 4
                        return false;

                    //get all collection items whose parentId is this item's id
                    ResultSet rs = stmt.executeQuery(String.format("SELECT MediaItem.Id, LevelNum FROM MediaItem JOIN Collection ON MediaItem.Id = Collection.Id WHERE ParentId = " + ((MediaCollection)nestedMedia[i]).getId() + ";"));
                    while (rs.next()) {
                        if (rs.getInt("LevelNum") == 3)
                            return false;

                        nestedIds.add((Integer)rs.getObject("Id"));
                    }

                    //check child collections for nesting
                    if (!isNestingAllowedRecursive(nestedIds))
                        return false;

                    nestedIds.clear();
                }
            }
        }
        catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        return true;
    }

    private boolean isNestingAllowedRecursive(ArrayList<Integer> nestedIds) {
        Statement stmt;
        ArrayList<Integer> newNestedIds = new ArrayList<>();

        try {
            stmt = dbConnection.createStatement();

            for (int i = 0; i < nestedIds.size(); i++) {
                //get all collection items whose parentId is this item's id
                ResultSet rs = stmt.executeQuery(String.format("SELECT MediaItem.Id, LevelNum FROM MediaItem JOIN Collection ON MediaItem.Id = Collection.Id WHERE ParentId = " + nestedIds.get(i) + ";"));
                while (rs.next()) {
                    if (rs.getInt("LevelNum") == 3)    //implies this collection would be moved down to level 4
                        return false;

                    newNestedIds.add((Integer)rs.getObject("Id"));
                }

                //check child collections for nesting
                if (!isNestingAllowedRecursive(newNestedIds))
                    return false;

                newNestedIds.clear();
            }
        }
        catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        return true;
    }

    public void UpdateMediaNameAndPath(MediaItem media) {
        Statement stmt;

        try {
            stmt = dbConnection.createStatement();
            stmt.executeUpdate(String.format("UPDATE MediaItem SET Name = \'" + media.getName() + "\' , RelPath = \'"
                    + media.getRelPath() + "\' WHERE Id = " + media.getId() + ";"));

            if (media instanceof MediaCollection)
                updateChildMediaDetailsRecursive(media.getId(), media.getRelPath());
        }
        catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void updateChildMediaDetailsRecursive(Integer collectionId, String collectionPath) {
        Statement stmtQuery;
        Statement stmtUpdate;
        Hashtable<Integer, String> nestedCollectionDetails = new Hashtable<>();

        Integer childId;
        String childName;
        String newChildPath;
        String childMediaType;

        try {
            stmtQuery = dbConnection.createStatement();
            stmtUpdate = dbConnection.createStatement();

            //retrieve all children from database
            ResultSet rs = stmtQuery.executeQuery(String.format("SELECT Id, Name, MediaType FROM MediaItem WHERE ParentId = " + collectionId + ";"));
            while (rs.next()) {
                childId = (Integer)rs.getObject("Id");
                childName = rs.getString("Name");
                childMediaType = rs.getString("MediaType");

                if (childMediaType.equals("Collection")) {
                    newChildPath = collectionPath + childName + "/";
                    nestedCollectionDetails.put(childId, newChildPath);
                }
                else
                    newChildPath = collectionPath + childName;

                //update all children
                stmtUpdate.executeUpdate(String.format("UPDATE MediaItem SET RelPath = \'" + newChildPath + "\' WHERE Id = " + childId + ";"));
            }

            //update info for all nested collections
            nestedCollectionDetails.forEach(this::updateChildMediaDetailsRecursive);
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void addAndUpdateChildMedia(MediaCollection currentCollection) {
        Statement stmt;
        MediaItem newMedia;
        int newId = -1;
        String mediaType;
        String strNextItemId;

        try {
            stmt = dbConnection.createStatement();

            //iterate over the selected "originalMedia" items -> the "newMedia" is always the next item of the original
            Stream<MediaItem> selectedMedia = currentCollection.getListOfChildren().stream().filter(MediaItem::isSelected);
            for (MediaItem originalMedia : (Iterable<MediaItem>) selectedMedia::iterator) {
                newMedia = originalMedia.nextItem;
                mediaType = MediaItem.getConcreteType(newMedia);

                //insert new media into database
                if (newMedia.getNextItem() == null)
                    strNextItemId = "null";
                else
                    strNextItemId = newMedia.getNextItem().getId().toString();

                stmt.executeUpdate(String.format("INSERT INTO MediaItem(Name, RelPath, ParentId, NextItemId, PrevItemId, LevelNum, MediaType)" +
                        "VALUES(\'" + newMedia.getName() + "\',\'" + newMedia.getRelPath() + "\'," + newMedia.getParentId() +
                        "," + strNextItemId + "," + originalMedia.getId() + "," + newMedia.getLevelNum() + ", \'" + mediaType + "\' );"));

                //retrieve value for id of newly inserted media item
                newId = stmt.getGeneratedKeys().getInt(1);

                //reconnect the surrounding media items
                if (newMedia.getNextItem() != null)
                    stmt.executeUpdate(String.format("UPDATE MediaItem SET PrevItemId = " + newId + " WHERE Id = " + strNextItemId + ";"));

                stmt.executeUpdate(String.format("UPDATE MediaItem SET NextItemId = " + newId + " WHERE Id = " + originalMedia.getId() + ";"));
            }
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void deleteMedia(ArrayList<MediaItem> mediaToDelete) {
        Statement stmt;

        try {
            stmt = dbConnection.createStatement();

            for (MediaItem media : mediaToDelete) {
                if (media instanceof MediaCollection) {
                    //delete children
                    deleteMediaRecursive((MediaCollection) media);

                    //delete self
                    stmt.executeUpdate(String.format("DELETE FROM Collection WHERE Id = " + media.getId() + ";"));
                    stmt.executeUpdate(String.format("DELETE FROM MediaItem WHERE Id = " + media.getId() + ";"));
                }
                else {
                    //delete individual media
                    stmt.executeUpdate(String.format("DELETE FROM MediaItem WHERE Id = " + media.getId() + ";"));
                }
            }
        }
        catch (Exception ex) { System.out.println(ex.getMessage()); }
    }

    private void deleteMediaRecursive(MediaCollection collectionToDelete) {
        Statement stmt;

        try {
            stmt = dbConnection.createStatement();

            refreshCurrentCollection(collectionToDelete, collectionToDelete.getId());

            for (MediaItem media : collectionToDelete.getListOfChildren()) {
                if (media instanceof MediaCollection) {
                    //delete children
                    deleteMediaRecursive((MediaCollection) media);

                    //delete self
                    stmt.executeUpdate(String.format("DELETE FROM Collection WHERE Id = " + media.getId() + ";"));
                    stmt.executeUpdate(String.format("DELETE FROM MediaItem WHERE Id = " + media.getId() + ";"));
                } else {
                    //delete individual media
                    stmt.executeUpdate(String.format("DELETE FROM MediaItem WHERE Id = " + media.getId() + ";"));
                }
            }
        }
        catch (Exception ex) { System.out.println(ex.getMessage()); }
    }

    public void updateCoverPhoto(MediaCollection collection) {
        Statement stmt;

        try {
            stmt = dbConnection.createStatement();

            stmt.executeUpdate(String.format("UPDATE Collection SET CoverPhotoItem = " + collection.getCoverPhotoItem()
                    + " WHERE Id = " + collection.getId() + ";"));
        }
        catch (Exception ex) { System.out.println(ex.getMessage()); }
    }

    public void updateCoverPhoto(Integer collectionId, Integer coverPhotoId) {
        Statement stmt;

        try {
            stmt = dbConnection.createStatement();

            stmt.executeUpdate(String.format("UPDATE Collection SET CoverPhotoItem = " + coverPhotoId
                    + " WHERE Id = " + collectionId + ";"));
        }
        catch (Exception ex) { System.out.println(ex.getMessage()); }
    }

    public String getRelativePathById(Integer id) {
        Statement stmt;
        String relPath = null;

        try {
            stmt = dbConnection.createStatement();

            ResultSet rs = stmt.executeQuery(String.format("SELECT RelPath FROM MediaItem WHERE Id = " + id + ";"));
            while (rs.next()) {
                relPath = rs.getString("RelPath");
            }
        }
        catch (Exception ex) { System.out.println(); }

        return relPath;
    }
}