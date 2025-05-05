package com.notmarra.notlib.database.query;

import com.notmarra.notlib.database.NotDatabase;
import com.notmarra.notlib.database.structure.NotRecord;

import java.util.List;

import javax.annotation.Nullable;


/**
 * @class NotSqlQueryExecutor
 * @brief Provides methods to execute SQL queries on a NotDatabase instance.
 * 
 * The NotSqlQueryExecutor class provides a simplified interface for executing
 * SQL queries against a NotDatabase. It supports operations such as selecting
 * records (returning single results or lists), updating data, checking existence
 * of records, and counting results.
 * 
 * @see NotDatabase
 * @see NotRecord
 * @see NotSqlBuilder
 */
public class NotSqlQueryExecutor {
    private final NotDatabase database;

    /**
     * @brief Constructor for the NotSqlQueryExecutor class.
     * 
     * Initializes a new instance of NotSqlQueryExecutor with the specified database connection.
     * 
     * @param database The NotDatabase instance to be used for executing SQL queries.
     */
    public NotSqlQueryExecutor(NotDatabase database) {
        this.database = database;
    }

    /**
     * @brief Executes a SQL query that is expected to return a single record.
     * 
     * @param sql The SQL query to execute.
     * @return NotRecord A non-null NotRecord containing the first row of the result set.
     *         If the query returns no results, an empty NotRecord is returned.
     */
    public NotRecord selectOne(String sql) {
        NotRecord result = selectOneOrNull(sql);
        return result != null ? result : NotRecord.empty();
    }

    /**
     * Executes a SELECT SQL query to retrieve a single record.
     * 
     * @param builder The SQL builder containing the query to execute
     * @return A NotRecord containing the result. If no record is found, returns an empty NotRecord
     */
    public NotRecord selectOne(NotSqlBuilder builder) {
        NotRecord result = selectOneOrNull(builder);
        return result != null ? result : NotRecord.empty();
    }

    /**
     * @brief Executes an SQL query and returns the first record or null if no records are found.
     * 
     * This method executes the provided SQL query and returns the first record from the result set.
     * If the query returns no records, null is returned instead.
     * 
     * @param sql The SQL query to execute
     * @return The first record from the result set, or null if the result set is empty
     */
    public @Nullable NotRecord selectOneOrNull(String sql) {
        List<NotRecord> results = select(sql);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * @brief Executes a SQL query and returns the first record or null if no records are found.
     * 
     * This method executes a SQL SELECT query built by the provided NotSqlBuilder and returns
     * the first record from the result set. If the query returns no records, null is returned.
     * 
     * @param builder The NotSqlBuilder instance containing the SQL query to execute.
     * @return The first NotRecord from the result set or null if the result set is empty.
     * @see NotSqlBuilder
     * @see NotRecord
     * @see #select(NotSqlBuilder)
     */
    public @Nullable NotRecord selectOneOrNull(NotSqlBuilder builder) {
        List<NotRecord> results = select(builder);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * @brief Executes a SQL SELECT query and returns the results as a list of records.
     * 
     * @param sql The SQL SELECT statement to execute.
     * @return A list of NotRecord objects representing the query results.
     * @throws DatabaseException If there is an error executing the query.
     */
    public List<NotRecord> select(String sql) {
        return database.processSelect(sql, List.of());
    }

    /**
     * Executes a SELECT SQL query using the provided NotSqlBuilder.
     *
     * @param builder The NotSqlBuilder instance containing the SELECT query to execute
     * @return A List of NotRecord objects representing the query results
     */
    public List<NotRecord> select(NotSqlBuilder builder) {
        return database.processSelect(builder.build(), builder.getParameters());
    }

    /**
     * @brief Executes an SQL update statement without parameters.
     * @param sql The SQL update statement to execute.
     * @return The number of rows affected by the update operation.
     */
    public int update(String sql) {
        return database.processUpdate(sql, List.of());
    }

    /**
     * @brief Updates database records using the provided SQL builder.
     * 
     * @param builder The NotSqlBuilder instance containing the UPDATE query and parameters
     * @return The number of rows affected by the update operation
     */
    public int update(NotSqlBuilder builder) {
        return database.processUpdate(builder.build(), builder.getParameters());
    }

    /**
     * Checks if a query result exists in the database.
     * 
     * @param builder The SQL query builder containing the query to execute
     * @return true if the query returns at least one row, false otherwise
     */
    public boolean exists(NotSqlBuilder builder) { return !select(builder).isEmpty(); }

    /**
     * @brief Checks if the SQL query execution succeeded.
     * @details Executes the update query and checks if it affected at least one row.
     * @param builder The SQL query builder containing the query to be executed.
     * @return true if the update affected at least one row, false otherwise.
     */
    public boolean succeeded(NotSqlBuilder builder) { return update(builder) > -1; }

    /**
     * @brief Counts the number of results from a SQL query.
     * 
     * This method executes a SELECT query using the provided SQL builder
     * and returns the count of results in the result set.
     * 
     * @param builder The NotSqlBuilder containing the SQL query configuration
     * @return The number of rows in the result set
     */
    public int count(NotSqlBuilder builder) { return select(builder).size(); }
}