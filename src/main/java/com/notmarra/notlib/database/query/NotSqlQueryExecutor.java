package com.notmarra.notlib.database.query;

import com.notmarra.notlib.database.NotDatabase;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class NotSqlQueryExecutor {
    private final NotDatabase database;

    public NotSqlQueryExecutor(NotDatabase database) {
        this.database = database;
    }

    /**
     * Execute a SELECT query and return results as a list of maps
     */
    public List<Map<String, Object>> executeQuery(NotSqlBuilder builder) {
        return database.processResult(builder.build(), builder.getParameters());
    }

    /**
     * Execute an INSERT, UPDATE, or DELETE query and return number of affected rows
     */
    public int executeUpdate(NotSqlBuilder builder) {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            connection = database.getConnection();
            stmt = connection.prepareStatement(builder.build());
            
            List<Object> params = builder.getParameters();
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            return stmt.executeUpdate();
        } catch (SQLException e) {
            database.getPlugin().getLogger().severe("Error executing update: " + e.getMessage());
            e.printStackTrace();
            return 0;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                database.getPlugin().getLogger().severe("Error closing resources: " + e.getMessage());
            }
        }
    }

    /**
     * Executes a query and returns a single result as a map
     */
    public Map<String, Object> fetchOne(NotSqlBuilder builder) {
        List<Map<String, Object>> results = executeQuery(builder);
        if (results != null && !results.isEmpty()) {
            return results.get(0);
        }
        return new HashMap<>();
    }

    /**
     * Executes a query and returns a single column from the first row
     */
    public <T> T fetchValue(NotSqlBuilder builder, String column) {
        Map<String, Object> row = fetchOne(builder);
        if (!row.isEmpty() && row.containsKey(column)) {
            try {
                @SuppressWarnings("unchecked")
                T value = (T) row.get(column);
                return value;
            } catch (ClassCastException e) {
                database.getPlugin().getLogger().severe("Error casting value: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Executes a query and returns a column of values
     */
    public <T> List<T> fetchColumn(NotSqlBuilder builder, String column) {
        List<Map<String, Object>> results = executeQuery(builder);
        List<T> columnValues = new ArrayList<>();
        
        if (results != null) {
            for (Map<String, Object> row : results) {
                if (row.containsKey(column)) {
                    try {
                        @SuppressWarnings("unchecked")
                        T value = (T) row.get(column);
                        columnValues.add(value);
                    } catch (ClassCastException e) {
                        database.getPlugin().getLogger().severe("Error casting value: " + e.getMessage());
                    }
                }
            }
        }
        
        return columnValues;
    }

    /**
     * Executes a query to check if a record exists
     */
    public boolean exists(NotSqlBuilder builder) {
        List<Map<String, Object>> results = executeQuery(builder);
        return results != null && !results.isEmpty();
    }

    /**
     * Executes a query and returns the count of results
     */
    public int count(NotSqlBuilder builder) {
        List<Map<String, Object>> results = executeQuery(builder);
        return results != null ? results.size() : 0;
    }
}