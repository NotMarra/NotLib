package com.notmarra.notlib.database.query;

import com.notmarra.notlib.database.NotDatabase;
import com.notmarra.notlib.database.structure.NotRecord;

import java.util.List;
import java.util.ArrayList;
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
    public List<NotRecord> executeQuery(String sql) {
        return database.processResult(sql, List.of());
    }
    public List<NotRecord> executeQuery(NotSqlBuilder builder) {
        return database.processResult(builder.build(), builder.getParameters());
    }

    /**
     * Execute an INSERT, UPDATE, or DELETE query and return number of affected rows
     */
    public int executeUpdate(String sql) {
        try {
            return database.getConnection().prepareStatement(sql).executeUpdate();
        } catch (SQLException e) {
            database.getPlugin().getLogger().severe("Error executing update: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    public int executeUpdate(NotSqlBuilder builder) {
        try {
            Connection connection = database.getConnection();
            PreparedStatement stmt = connection.prepareStatement(builder.build());
            
            List<Object> params = builder.getParameters();
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            return stmt.executeUpdate();
        } catch (SQLException e) {
            database.getPlugin().getLogger().severe("Error executing update: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Executes a query and returns a single result as a map
     */
    public NotRecord fetchOne(NotSqlBuilder builder) {
        List<NotRecord> results = executeQuery(builder);
        if (results != null && !results.isEmpty()) {
            return results.get(0);
        }
        return NotRecord.empty();
    }

    /**
     * Executes a query and returns a single column from the first row
     */
    public <T> T fetchValue(NotSqlBuilder builder, String column) {
        NotRecord row = fetchOne(builder);
        if (!row.isEmpty() && row.hasColumn(column)) {
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
        List<NotRecord> results = executeQuery(builder);
        List<T> columnValues = new ArrayList<>();
        
        if (results != null) {
            for (NotRecord record : results) {
                if (record.hasColumn(column)) {
                    try {
                        @SuppressWarnings("unchecked")
                        T value = (T) record.get(column);
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
    public boolean exists(NotSqlBuilder builder) { return !executeQuery(builder).isEmpty(); }

    /**
     * Executes a query to insert/update/delete a record and returns the number of affected rows
     */
    public boolean succeeded(NotSqlBuilder builder) { return executeUpdate(builder) > 0; }

    /**
     * Executes a query and returns the count of results
     */
    public int count(NotSqlBuilder builder) { return executeQuery(builder).size(); }
}