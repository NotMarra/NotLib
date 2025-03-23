package com.notmarra.notlib.database;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Database database;
    
    private DatabaseManager() {
    }
    
    /**
     * Retrieves the instance of DatabaseManager (Singleton pattern)
     * @return Instance of DatabaseManager
     */
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    /**
     * Sets the database and prepares it for use
     * @param database Instance of Database to be used
     */
    public void setDatabase(Database database) {
        if (this.database != null) {
            this.database.close();
        }
        this.database = database;
        this.database.setup();
    }
    
    /**
     * Retrieves the current instance of the database
     * @return Current instance of the database
     */
    public Database getDatabase() {
        return database;
    }
    
    /**
     * Retrieves a connection to the database
     * @return Connection to the database
     * @throws SQLException If an error occurs while obtaining the connection
     */
    public Connection getConnection() throws SQLException {
        if (database == null) {
            throw new SQLException("Databáze není nastavena");
        }
        return database.getConnection();
    }
    
    /**
     * Closes the database connection
     */
    public void close() {
        if (database != null) {
            database.close();
        }
    }
}