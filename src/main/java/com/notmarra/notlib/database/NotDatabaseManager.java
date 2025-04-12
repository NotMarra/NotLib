package com.notmarra.notlib.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.notmarra.notlib.extensions.NotPlugin;

public class NotDatabaseManager {
    private static NotDatabaseManager instance;
    private final NotPlugin plugin;
    private Map<String, NotDatabase> databases = new HashMap<>();
    
    public NotDatabaseManager(NotPlugin plugin) { this.plugin = plugin; }
    
    public static NotDatabaseManager getInstance() { return instance; }

    public NotDatabaseManager addDatabase(NotDatabase newDatabase) {
        if (databases.containsKey(newDatabase.getId())) {
            throw new IllegalArgumentException("Database with ID " + newDatabase.getId() + " is already registered.");
        }
        databases.put(newDatabase.getId(), newDatabase);
        return this;
    }

    public NotDatabaseManager registerDatabase(String dbId) {
        if (!databases.containsKey(dbId)) {
            throw new IllegalArgumentException("Database with ID " + dbId + " is not registered.");
        }
        try {
            NotDatabase registered = databases.get(dbId);
            registered.close();
            registered.connect();
            registered.setup();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to set up database: " + e.getMessage());
        }
        return this;
    }
    
    public NotDatabaseManager registerDatabase(NotDatabase newDatabase) {
        addDatabase(newDatabase);
        registerDatabase(newDatabase.getId());
        return this;
    }
    
    public @Nullable NotDatabase firstConnected() {
        return databases.values().stream()
            .filter(db -> db.isConnected())
            .findFirst()
            .orElse(null);
    }
    public Map<String, NotDatabase> getDatabases() { return databases; }
    public List<String> getDatabaseIds() { return new ArrayList<>(databases.keySet()); }
    public NotDatabase getDatabase(String dbId) { return databases.get(dbId); }
    
    public Connection getConnection(String dbId) throws SQLException {
        if (databases.get(dbId) == null) {
            throw new SQLException("Databáze není nastavena");
        }
        return databases.get(dbId).getConnection();
    }

    public void close() {
        for (NotDatabase db : databases.values()) {
            if (db != null) db.close();
        }
        databases.clear();
    }
    
    public void close(String dbId) {
        NotDatabase db = databases.get(dbId);
        if (db != null) db.close();
        databases.remove(dbId);
    }
}