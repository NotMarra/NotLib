package com.notmarra.notlib.database;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class Database {
    protected HikariDataSource dataSource;
    
    /**
     * Initialization of the database and setting up the connection pool
     */
    public abstract void setup();
    
    /**
     * Obtaining a connection from the connection pool
     * @return Connection to the database
     * @throws SQLException If an error occurs while obtaining the connection
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("DataSource is not initialized or closed");
        }
        return dataSource.getConnection();
    }
    
    /**
     * Closing the connection pool
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
    
    /**
     * Check if the connection is active
     * @return true if the connection is active, otherwise false
     */
    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }

    /**
     * Creates tables and prepares the database for use
     * @param sql SQL query for creating tables
     */
    public void createTables(String sql) {
        try (Statement statement = dataSource.getConnection().createStatement()) {
            statement.execute(sql);
        } catch (Exception e) {
            System.err.println("Error creating tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
}