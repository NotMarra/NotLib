package com.notmarra.notlib.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.notmarra.notlib.extensions.NotPlugin;

public class NotDatabaseManager {
    private static NotDatabaseManager instance;
    private final NotPlugin plugin;
    private Map<String, NotDatabase> databases = new HashMap<>();
    
    public NotDatabaseManager(NotPlugin plugin) { this.plugin = plugin; }
    
    /**
     * Retrieves the instance of DatabaseManager (Singleton pattern)
     * @return Instance of DatabaseManager
     */
    public static NotDatabaseManager getInstance() {
        return instance;
    }

    private void setupDatabase(NotDatabase database) {
        if (database == null) return;
        try {
            database.close();
            database.connect();
            database.setup();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to set up database: " + e.getMessage());
        }
    }
    
    /**
     * Sets the database and prepares it for use
     * @param database Instance of Database to be used
     */
    public NotDatabaseManager registerDatabase(NotDatabase newDatabase) {
        if (databases.containsKey(newDatabase.getId())) {
            throw new IllegalArgumentException("Database with ID " + newDatabase.getId() + " is already registered.");
        }
        databases.put(newDatabase.getId(), newDatabase);
        setupDatabase(newDatabase);
        return this;
    }
    
    /**
     * Retrieves the current instance of the database
     * @return Current instance of the database
     */
    public NotDatabase getDatabase(String dbId) {
        return databases.get(dbId);
    }
    
    /**
     * Retrieves a connection to the database
     * @return Connection to the database
     * @throws SQLException If an error occurs while obtaining the connection
     */
    public Connection getConnection(String dbId) throws SQLException {
        if (databases.get(dbId) == null) {
            throw new SQLException("Databáze není nastavena");
        }
        return databases.get(dbId).getConnection();
    }
    
    /**
     * Closes the database connection
     */
    public void close(String dbId) {
        if (databases.get(dbId) != null) {
            databases.get(dbId).close();
        }
    }
}