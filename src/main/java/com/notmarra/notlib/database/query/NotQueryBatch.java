package com.notmarra.notlib.database.query;

import com.notmarra.notlib.database.NotDatabase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotQueryBatch {
    private final NotDatabase database;
    private final List<NotSqlBuilder> builders = new ArrayList<>();
    
    public NotQueryBatch(NotDatabase database) {
        this.database = database;
    }
    
    public NotQueryBatch add(NotSqlBuilder builder) {
        builders.add(builder);
        return this;
    }
    
    public int execute() {
        if (builders.isEmpty()) {
            return 0;
        }
        
        Connection connection = null;
        List<PreparedStatement> statements = new ArrayList<>();
        int successCount = 0;
        
        try {
            connection = database.getConnection();
            connection.setAutoCommit(false);
            
            for (NotSqlBuilder builder : builders) {
                String sql = builder.build();
                PreparedStatement stmt = connection.prepareStatement(sql);
                
                List<Object> params = builder.getParameters();
                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i + 1, params.get(i));
                }
                
                statements.add(stmt);
                successCount += stmt.executeUpdate();
            }
            
            connection.commit();
            return successCount;
        } catch (SQLException e) {
            database.getPlugin().getLogger().severe("Error executing batch: " + e.getMessage());
            
            if (connection != null) {
                try {
                    connection.rollback();
                    database.getPlugin().getLogger().info("Transaction rolled back");
                } catch (SQLException rollbackEx) {
                    database.getPlugin().getLogger().severe("Error rolling back: " + rollbackEx.getMessage());
                }
            }
            
            return 0;
        } finally {
            // Close all prepared statements
            for (PreparedStatement stmt : statements) {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (SQLException e) {
                    database.getPlugin().getLogger().severe("Error closing statement: " + e.getMessage());
                }
            }
            
            // Reset auto-commit and close connection
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    database.getPlugin().getLogger().severe("Error closing connection: " + e.getMessage());
                }
            }
        }
    }
    
    public void clear() {
        builders.clear();
    }
    
    /**
     * Get the number of queries in the batch
     */
    public int size() {
        return builders.size();
    }
}