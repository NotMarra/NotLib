package com.notmarra.notlib.database.query;

import com.notmarra.notlib.database.NotDatabase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @class NotQueryBatch
 * @brief Batch processor for SQL queries that executes multiple queries in a single transaction.
 * 
 * This class allows collecting multiple SQL queries and executing them as a
 * single atomic transaction. If any query in the batch fails, the entire
 * transaction will be rolled back to ensure data integrity.
 * 
 * @details The NotQueryBatch manages a collection of SQL builder objects and executes
 * all of them within a single database transaction. This provides ACID guarantees
 * and better performance for multiple related database operations.
 * 
 * Usage example:
 * @code
 *     NotQueryBatch batch = new NotQueryBatch(database);
 *     batch.add(insert1)
 *          .add(insert2)
 *          .add(update1);
 *     int affectedRows = batch.execute();
 * @endcode
 * 
 * @see NotSqlBuilder
 * @see NotDatabase
 */
public class NotQueryBatch {
    private final NotDatabase database;
    private final List<NotSqlBuilder> builders = new ArrayList<>();
    
    /**
     * @brief Constructs a new NotQueryBatch with the specified database.
     * 
     * This constructor initializes a query batch that will operate on the provided database.
     * The query batch allows for executing multiple database operations as a single unit.
     * 
     * @param database The NotDatabase instance to associate with this query batch
     */
    public NotQueryBatch(NotDatabase database) {
        this.database = database;
    }
    
    /**
     * Adds a NotSqlBuilder to this batch of queries.
     *
     * @param builder The NotSqlBuilder to add to the batch.
     * @return This NotQueryBatch instance for method chaining.
     */
    public NotQueryBatch add(NotSqlBuilder builder) {
        builders.add(builder);
        return this;
    }
    
    /**
     * Executes all SQL statements in the batch as a single transaction.
     * 
     * This method performs the following operations:
     * 1. Gets a database connection and disables auto-commit
     * 2. For each SQL builder in the batch:
     *    - Builds the SQL statement
     *    - Prepares the statement with the connection
     *    - Sets all parameters
     *    - Executes the statement and counts affected rows
     * 3. Commits the transaction if all statements execute successfully
     * 
     * If any statement fails, the entire transaction is rolled back.
     * All resources (statements and connection) are properly closed in the finally block.
     * 
     * @return The total number of rows affected by all statements, or 0 if the batch is empty or execution fails
     */
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
    
    /**
     * @brief Clears all query builders from the batch.
     * 
     * This method removes all previously added query builders from the batch, effectively resetting it to an empty state.
     * After calling this method, the batch will contain no query builders to execute.
     */
    public void clear() {
        builders.clear();
    }
    
    /**
     * Returns the number of query builders contained in this batch.
     *
     * @return the number of query builders in this batch
     */
    public int size() {
        return builders.size();
    }
}