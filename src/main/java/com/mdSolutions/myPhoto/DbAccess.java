package com.mdSolutions.myPhoto;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DbAccess {

    private static final String CREATE_TABLE_MEDIA_ITEM = String.format("CREATE TABLE MediaItem (Id INT PRIMARY KEY, Name VARCHAR(256) NOT NULL, RelPath VARCHAR(256) NOT NULL, ParentId INT REFERENCES MediaItem (Id) ON DELETE CASCADE, NextItemId INT REFERENCES MediaItem (Id) ON DELETE CASCADE, PrevItemId INT REFERENCES MediaItem (Id) ON DELETE CASCADE, LevelNum INT NOT NULL );");
    private static final String CREATE_TABLE_COLLECTION = String.format("CREATE TABLE Collection (Id INT PRIMARY KEY REFERENCES MediaItem (Id) ON DELETE CASCADE, CoverPhotoPath VARCHAR(256) NULL);");

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
            _instance.establishConnection("myPhotoLibrary");
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
        }
        catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        finally {
            try {
                if (stmt != null) { stmt.close(); }
            }
            catch (SQLException ex){
                System.out.println(ex.getMessage());
            }
        }


        //TODO Eventually: Run through directory and auto-import existing media, or ask for confirmation to remove existing media
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

}