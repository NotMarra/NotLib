package com.notmarra.notlib.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.notmarra.notlib.extensions.NotPlugin;

/**
 * @file NotDatabaseManager.java
 * @brief Manages database connections for the NotLib library
 *
 * The NotDatabaseManager class is a singleton that manages multiple database connections.
 * It allows registering, retrieving, and closing database connections.
 *
 * @version 1.0
 */

/**
 * @brief Manages multiple database connections
 *
 * This class follows the singleton pattern and provides functionality to:
 * - Register and manage multiple database instances
 * - Connect and set up databases
 * - Retrieve connections from registered databases
 * - Close database connections when no longer needed
 */
public class NotDatabaseManager {
    private static NotDatabaseManager instance;
    private final NotPlugin plugin;
    private Map<String, NotDatabase> databases = new HashMap<>();
    
    public NotDatabaseManager(NotPlugin plugin) { this.plugin = plugin; }
    
    public static NotDatabaseManager getInstance() { return instance; }

    /**
     * Adds a new database to the manager.
     *
     * @param newDatabase The database to add to the manager
     * @return The current instance of the database manager for chaining
     * @throws IllegalArgumentException If a database with the same ID already exists in the manager
     */
    public NotDatabaseManager addDatabase(NotDatabase newDatabase) {
        if (databases.containsKey(newDatabase.getId())) {
            throw new IllegalArgumentException("Database with ID " + newDatabase.getId() + " is already registered.");
        }
        databases.put(newDatabase.getId(), newDatabase);
        return this;
    }

    /**
     * @brief Registers and sets up a database by its identifier.
     * 
     * This method attempts to register a database by its ID. If the database is found in the
     * registry, it will close any existing connections, reconnect, and perform setup operations.
     * If the database is not found or if there is an error during setup, appropriate error handling
     * is performed.
     * 
     * @param dbId The unique identifier of the database to register
     * @return The current NotDatabaseManager instance for method chaining
     * @throws IllegalArgumentException If the database with the given ID is not registered
     */
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
    
    /**
     * Registers a new database to the manager.
     * This method adds the database to the manager and registers it with its ID.
     *
     * @param newDatabase The database to register
     * @return This instance of NotDatabaseManager for method chaining
     * @see #addDatabase(NotDatabase)
     * @see #registerDatabase(String)
     */
    public NotDatabaseManager registerDatabase(NotDatabase newDatabase) {
        addDatabase(newDatabase);
        registerDatabase(newDatabase.getId());
        return this;
    }
    
    /**
     * @brief Returns the first connected database found in the databases collection.
     * 
     * @details This method searches through all databases and returns the first one
     *          that has an active connection. If no connected database is found,
     *          null is returned.
     * 
     * @return The first connected NotDatabase instance or null if none are connected.
     */
    public @Nullable NotDatabase firstConnected() {
        return databases.values().stream()
            .filter(db -> db.isConnected())
            .findFirst()
            .orElse(null);
    }
    /**
     * @brief Retrieves all databases managed by this instance.
     * 
     * @return A map containing all databases, where keys are database names and values are the corresponding NotDatabase instances.
     */
    public Map<String, NotDatabase> getDatabases() { return databases; }
    /**
     * @brief Retrieves the IDs of all registered databases.
     * 
     * @return A list containing the IDs of all registered databases.
     */
    public List<String> getDatabaseIds() { return new ArrayList<>(databases.keySet()); }
    /**
     * Retrieves a database by its identifier.
     * 
     * @param dbId The unique identifier of the database to retrieve.
     * @return The database with the specified ID, or null if no database with the given ID exists.
     */
    public NotDatabase getDatabase(String dbId) { return databases.get(dbId); }
    
    /**
     * @brief Retrieves a database connection for the specified database ID.
     *
     * @param dbId The identifier of the database to connect to.
     * @return A Connection object representing the connection to the specified database.
     * @throws SQLException If the database with the specified ID is not configured or if a database access error occurs.
     */
    public Connection getConnection(String dbId) throws SQLException {
        if (databases.get(dbId) == null) {
            throw new SQLException("Databáze není nastavena");
        }
        return databases.get(dbId).getConnection();
    }

    /**
     * @brief Closes all databases and clears the database collection.
     * 
     * Iterates through all the database instances in the collection, closes each one
     * if it's not null, and then clears the entire collection.
     */
    public void close() {
        for (NotDatabase db : databases.values()) {
            if (db != null) db.close();
        }
        databases.clear();
    }
    
    /**
     * @brief Closes and removes a database with the specified ID.
     * 
     * This method retrieves the database with the given ID, closes it if found,
     * and removes it from the managed databases collection.
     * 
     * @param dbId The identifier of the database to close
     */
    public void close(String dbId) {
        NotDatabase db = databases.get(dbId);
        if (db != null) db.close();
        databases.remove(dbId);
    }
}