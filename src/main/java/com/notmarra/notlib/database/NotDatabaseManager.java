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
    
    public static NotDatabaseManager getInstance() { return instance; }

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
    
    public NotDatabaseManager registerDatabase(NotDatabase newDatabase) {
        if (databases.containsKey(newDatabase.getId())) {
            throw new IllegalArgumentException("Database with ID " + newDatabase.getId() + " is already registered.");
        }
        databases.put(newDatabase.getId(), newDatabase);
        setupDatabase(newDatabase);
        return this;
    }
    
    public NotDatabase getDatabase(String dbId) { return databases.get(dbId); }
    
    public Connection getConnection(String dbId) throws SQLException {
        if (databases.get(dbId) == null) {
            throw new SQLException("Databáze není nastavena");
        }
        return databases.get(dbId).getConnection();
    }
    
    public void close(String dbId) {
        NotDatabase db = databases.get(dbId);
        if (db != null) db.close();
    }
}