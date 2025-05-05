
package com.notmarra.notlib.database.query;

import java.util.ArrayList;
import java.util.List;

/**
 * @class NotSqlBuilder
 * @brief A fluent SQL query builder for constructing SQL queries of various types.
 *
 * NotSqlBuilder provides a convenient and type-safe way to build SQL queries for:
 * - SELECT: retrieving data from tables
 * - INSERT: adding new records to tables
 * - UPDATE: modifying existing records
 * - DELETE: removing records from tables
 *
 * This class follows the builder pattern, allowing method chaining for a clean API.
 * 
 * Example usage:
 * @code
 * // Create a SELECT query
 * String query = NotSqlBuilder.select("users")
 *     .columns(Arrays.asList("id", "name", "email"))
 *     .whereEquals("active", 1)
 *     .orderBy("name")
 *     .limit(10)
 *     .build();
 * @endcode
 * 
 * The builder handles proper query formatting and parameter binding to help prevent SQL injection.
 */
public class NotSqlBuilder {
    private String table;
    private List<String> columns = new ArrayList<>();
    private NotSqlWhereBuilder whereBuilder = NotSqlWhereBuilder.create();
    private List<String> joinClauses = new ArrayList<>();
    private List<String> orderByClauses = new ArrayList<>();
    private List<String> groupByClauses = new ArrayList<>();
    private List<NotSqlUpdateValue> updateValues = new ArrayList<>();
    private List<List<Object>> insertValues = new ArrayList<>();
    private Integer limit;
    private Integer offset;
    private QueryType queryType;

    public enum QueryType {
        SELECT, INSERT, UPDATE, DELETE
    }

    /**
     * @brief Retrieves the type of query this SQL builder represents
     *
     * @return The QueryType enumeration value representing the current query type
     */
    public QueryType getQueryType() { return queryType; }

    /**
     * @brief Creates a SELECT query builder for the specified table.
     * 
     * This method initializes a new NotSqlBuilder instance, sets the table name using the from() method,
     * and configures it as a SELECT query.
     * 
     * @param table The name of the table to select from.
     * @return A new NotSqlBuilder instance configured for a SELECT query on the specified table.
     */
    public static NotSqlBuilder select(String table) {
        return new NotSqlBuilder().from(table).setQueryType(QueryType.SELECT);
    }

    /**
     * @brief Creates a new instance of NotSqlBuilder for constructing SELECT queries.
     * 
     * This static factory method initializes a NotSqlBuilder with the given table and columns,
     * setting the query type to SELECT.
     * 
     * @param table The name of the table to select data from
     * @param columns A list of column names to include in the SELECT statement
     * @return A new NotSqlBuilder instance configured for a SELECT query
     */
    public static NotSqlBuilder select(String table, List<String> columns) {
        return new NotSqlBuilder().from(table).columns(columns).setQueryType(QueryType.SELECT);
    }

    /**
     * Creates a new SQL builder for an UPDATE query.
     * 
     * @param table The name of the table to update
     * @return A new NotSqlBuilder instance configured for an UPDATE query
     * @since 1.0
     */
    public static NotSqlBuilder update(String table) {
        return new NotSqlBuilder().table(table).setQueryType(QueryType.UPDATE);
    }

    /**
     * Creates a new SQL INSERT query builder for the specified table.
     * 
     * @param table Name of the table to insert data into
     * @return A new NotSqlBuilder instance with the query type set to INSERT
     */
    public static NotSqlBuilder insertInto(String table) {
        return new NotSqlBuilder().table(table).setQueryType(QueryType.INSERT);
    }

    /**
     * Creates a new NotSqlBuilder instance for DELETE operations.
     * 
     * @param table The table name to delete from
     * @return A new NotSqlBuilder object configured for DELETE operations on the specified table
     */
    public static NotSqlBuilder deleteFrom(String table) {
        return new NotSqlBuilder().table(table).setQueryType(QueryType.DELETE);
    }

    private NotSqlBuilder() {}

    /**
     * @brief Sets the table name for the SQL query.
     *
     * @param table The name of the table to be used in the SQL query.
     * @return A reference to this NotSqlBuilder object to allow for method chaining.
     */
    public NotSqlBuilder table(String table) {
        this.table = table;
        return this;
    }

    /**
     * @brief Sets the table name for the query.
     * 
     * This is an alias for the table() method, providing a more readable syntax when constructing
     * SQL SELECT queries.
     * 
     * @param table The name of the table to query from
     * @return The current NotSqlBuilder instance for method chaining
     */
    public NotSqlBuilder from(String table) {
        return table(table);
    }

    /**
     * Adds a list of columns to the SQL query.
     * 
     * @param columns the list of column names to add to the query
     * @return this builder instance for method chaining
     */
    public NotSqlBuilder columns(List<String> columns) {
        for (String column : columns) {
            this.columns.add(column);
        }
        return this;
    }

    /**
     * @brief Sets the where clause builder for this SQL builder.
     *
     * @param where The NotSqlWhereBuilder instance that contains the where conditions
     * @return This NotSqlBuilder instance for method chaining
     */
    public NotSqlBuilder where(NotSqlWhereBuilder where) {
        this.whereBuilder = where;
        return this;
    }

    /**
     * @brief Adds a WHERE condition to the SQL query with an AND operator.
     * 
     * This method adds a condition to the WHERE clause of the SQL query using the specified column,
     * comparison operator, and value. Multiple calls to this method will combine conditions
     * with AND logic.
     * 
     * @param column The name of the column to compare
     * @param operator The comparison operator (e.g., "=", "<", ">", "LIKE", etc.)
     * @param value The value to compare against, which will be properly escaped
     * @return The current NotSqlBuilder instance for method chaining
     */
    public NotSqlBuilder where(String column, String operator, Object value) {
        this.whereBuilder.and(column, operator, value);
        return this;
    }

    /**
     * @brief Adds a WHERE condition to the SQL query, checking for equality.
     * 
     * This method adds a condition to the WHERE clause of the SQL query that checks
     * if the specified column equals the given value. The condition is combined with
     * any existing conditions using the AND operator.
     * 
     * @param column The name of the column to check
     * @param value The value to compare the column against
     * @return This NotSqlBuilder instance for method chaining
     */
    public NotSqlBuilder whereEquals(String column, Object value) {
        this.whereBuilder.andEquals(column, value);
        return this;
    }

    /**
     * @brief Adds a condition to match rows where the specified column is not equal to the given value.
     * 
     * This method appends a NOT EQUALS condition to the WHERE clause of the SQL query.
     * Multiple conditions are combined with logical AND.
     * 
     * @param column The column name to compare
     * @param value The value to compare against the column
     * @return The current NotSqlBuilder instance for method chaining
     */
    public NotSqlBuilder whereNotEquals(String column, Object value) {
        this.whereBuilder.andNotEquals(column, value);
        return this;
    }

    /**
     * @brief Adds a less-than condition to the WHERE clause of the query.
     *
     * This method adds a condition to the query that requires the specified column
     * to have a value less than the provided value.
     *
     * @param column The name of the column to compare.
     * @param value The value to compare against the column.
     * @return This NotSqlBuilder instance for method chaining.
     */
    public NotSqlBuilder whereLessThan(String column, Object value) {
        this.whereBuilder.andLessThan(column, value);
        return this;
    }

    /**
     * Adds a "less than or equals" condition to the WHERE clause of the SQL query.
     * 
     * @param column The name of the column to compare
     * @param value The value to compare the column against
     * @return This NotSqlBuilder instance for method chaining
     */
    public NotSqlBuilder whereLessThanOrEquals(String column, Object value) {
        this.whereBuilder.andLessThanOrEquals(column, value);
        return this;
    }

    /**
     * @brief Adds a greater-than condition to the WHERE clause of the SQL query.
     * 
     * This method appends a condition to the WHERE clause that checks if the specified column
     * has a value greater than the provided value.
     * 
     * @param column The name of the column to compare
     * @param value The value to compare against
     * @return This NotSqlBuilder instance for method chaining
     */
    public NotSqlBuilder whereGreaterThan(String column, Object value) {
        this.whereBuilder.andGreaterThan(column, value);
        return this;
    }

    /**
     * @brief Adds a "Greater Than Or Equals" condition to the where clause of the SQL query
     * 
     * This method adds a condition to the where clause that filters results where the specified column
     * has a value greater than or equal to the provided value.
     * 
     * @param column The name of the column to compare
     * @param value The value to compare against the column
     * @return The current NotSqlBuilder instance for method chaining
     */
    public NotSqlBuilder whereGreaterThanOrEquals(String column, Object value) {
        this.whereBuilder.andGreaterThanOrEquals(column, value);
        return this;
    }

    /**
     * Adds a WHERE IN clause to the SQL query.
     * 
     * @param column The column name to compare against the values
     * @param values The list of values to check if the column value is contained within
     * @return The current NotSqlBuilder instance for method chaining
     */
    public NotSqlBuilder whereIn(String column, List<Object> values) {
        this.whereBuilder.andIn(column, values);
        return this;
    }

    /**
     * @brief Adds a NOT IN condition to the WHERE clause.
     * 
     * This method applies a NOT IN condition to the query, filtering out records 
     * where the specified column's value exists in the provided list.
     * 
     * @param column The name of the column to check.
     * @param values The list of values against which the column is checked.
     * @return The current NotSqlBuilder instance for method chaining.
     */
    public NotSqlBuilder whereNotIn(String column, List<Object> values) {
        this.whereBuilder.andNotIn(column, values);
        return this;
    }

    /**
     * @brief Adds a LIKE condition to the WHERE clause of the SQL query.
     *
     * This method adds a condition that checks if the specified column contains the given pattern.
     * The condition is combined with previous conditions using AND logic.
     *
     * @param column The name of the column to check against
     * @param pattern The pattern to match (can include SQL wildcard characters like % and _)
     * @return This NotSqlBuilder instance for method chaining
     */
    public NotSqlBuilder whereLike(String column, String pattern) {
        this.whereBuilder.andLike(column, pattern);
        return this;
    }

    /**
     * @brief Adds a NOT LIKE condition to the WHERE clause of the SQL query.
     * 
     * This method adds a condition to the WHERE clause that checks if the specified column's value
     * does NOT match the given pattern.
     * 
     * @param column The name of the column to compare.
     * @param pattern The pattern to check against (can include SQL wildcards like % and _).
     * @return This NotSqlBuilder instance for method chaining.
     */
    public NotSqlBuilder whereNotLike(String column, String pattern) {
        this.whereBuilder.andNotLike(column, pattern);
        return this;
    }

    /**
     * @brief Adds a NULL check condition to the WHERE clause.
     *
     * This method adds a condition to check if a specific column is NULL.
     * The condition is combined with the previous conditions using an AND operator.
     *
     * @param column The name of the column to check for NULL value
     * @return The current NotSqlBuilder instance for method chaining
     */
    public NotSqlBuilder whereNull(String column) {
        this.whereBuilder.andNull(column);
        return this;
    }

    /**
     * @brief Adds a "WHERE column IS NOT NULL" clause to the SQL query.
     * 
     * This method appends a condition that checks if the specified column is not NULL.
     * Multiple conditions are combined with AND logic by default.
     * 
     * @param column The name of the column to check for NOT NULL
     * @return The current NotSqlBuilder instance for method chaining
     */
    public NotSqlBuilder whereNotNull(String column) {
        this.whereBuilder.andNotNull(column);
        return this;
    }

    /**
     * Adds a raw WHERE condition to the SQL query with an AND operator.
     *
     * @param rawCondition The raw SQL condition string to be added to the WHERE clause
     * @return The current NotSqlBuilder instance for method chaining
     */
    public NotSqlBuilder where(String rawCondition) {
        this.whereBuilder.and(rawCondition);
        return this;
    }

    /**
     * Adds an 'OR WHERE' clause to the query.
     *
     * Adds a condition to the query joined with an 'OR' operator. The condition compares the
     * specified column with the provided value using the given operator.
     *
     * @param column The database column to check in the condition
     * @param operator The comparison operator (e.g., "=", "<>", ">", "<", ">=", "<=", "LIKE", "IN", etc.)
     * @param value The value to compare the column against
     * @return The current NotSqlBuilder instance for method chaining
     */
    public NotSqlBuilder orWhere(String column, String operator, Object value) {
        this.whereBuilder.or(column, operator, value);
        return this;
    }

    /**
     * @brief Adds a raw SQL condition to the WHERE clause with an OR logical operator.
     *
     * This method appends the provided raw SQL condition to the WHERE clause of the query,
     * connecting it to existing conditions with an OR operator.
     *
     * @param rawCondition The raw SQL condition string to add
     * @return The current NotSqlBuilder instance for method chaining
     */
    public NotSqlBuilder orWhere(String rawCondition) {
        this.whereBuilder.or(rawCondition);
        return this;
    }

    /**
     * @brief Adds a JOIN clause to the SQL query with the specified table and condition.
     * 
     * This method adds a standard INNER JOIN to the query being built.
     * 
     * @param table The name of the table to join with.
     * @param condition The ON condition that specifies how the tables are related.
     * @return This NotSqlBuilder instance for method chaining.
     */
    public NotSqlBuilder join(String table, String condition) {
        this.joinClauses.add("JOIN " + table + " ON " + condition);
        return this;
    }

    /**
     * Adds a LEFT JOIN clause to the SQL query.
     * 
     * @param table     The name of the table to join with.
     * @param condition The condition on which the join is based (e.g., "table1.id = table2.id").
     * @return          This NotSqlBuilder instance for method chaining.
     */
    public NotSqlBuilder leftJoin(String table, String condition) {
        this.joinClauses.add("LEFT JOIN " + table + " ON " + condition);
        return this;
    }

    /**
     * Adds a RIGHT JOIN clause to the query.
     * 
     * A RIGHT JOIN returns all rows from the right table (table), and the matching rows from the left table.
     * If there is no match, the result is NULL on the left side.
     *
     * @param table The name of the table to join.
     * @param condition The join condition that specifies how the tables are related.
     * @return This NotSqlBuilder instance for method chaining.
     */
    public NotSqlBuilder rightJoin(String table, String condition) {
        this.joinClauses.add("RIGHT JOIN " + table + " ON " + condition);
        return this;
    }

    /**
     * @brief Adds an ascending order by clause for the specified column.
     *
     * @param column The column name to order by in ascending order.
     * @return This builder instance for method chaining.
     */
    public NotSqlBuilder orderBy(String column) {
        this.orderByClauses.add(column + " ASC");
        return this;
    }

    /**
     * @brief Adds an ORDER BY clause to the SQL query.
     *
     * @param column The column name to order by
     * @param direction The direction of the ordering (e.g., "ASC", "DESC")
     * @return This NotSqlBuilder instance for method chaining
     */
    public NotSqlBuilder orderBy(String column, String direction) {
        this.orderByClauses.add(column + " " + direction);
        return this;
    }

    /**
     * Adds an ORDER BY clause to the SQL query.
     * 
     * @param column The column name to order by
     * @param direction The sort direction - true for ascending (ASC), false for descending (DESC)
     * @return This builder instance for method chaining
     */
    public NotSqlBuilder orderBy(String column, boolean direction) {
        this.orderByClauses.add(column + " " + (direction ? "ASC" : "DESC"));
        return this;
    }

    /**
     * @brief Adds a descending order by clause to the SQL query.
     * 
     * This method appends a column to the order by clauses in descending order.
     * Multiple calls to orderBy methods will build a comma-separated list of order by expressions.
     * 
     * @param column The column name to order by in descending order.
     * @return The current NotSqlBuilder instance for method chaining.
     */
    public NotSqlBuilder orderByDesc(String column) {
        this.orderByClauses.add(column + " DESC");
        return this;
    }

    /**
     * Adds an ascending order by clause to the SQL query for the specified column.
     * 
     * @param column The name of the column to order by in ascending order.
     * @return This NotSqlBuilder instance for method chaining.
     */
    public NotSqlBuilder orderByAsc(String column) {
        this.orderByClauses.add(column + " ASC");
        return this;
    }

    /**
     * Adds GROUP BY clauses to the SQL query.
     * 
     * @param columns Variadic parameter of column names to group by.
     * @return The current NotSqlBuilder instance for method chaining.
     */
    public NotSqlBuilder groupBy(String... columns) {
        for (String column : columns) {
            this.groupByClauses.add(column);
        }
        return this;
    }

    /**
     * Sets a limit on the number of rows returned by the SQL query.
     * 
     * @param limit The maximum number of rows to return.
     * @return This NotSqlBuilder instance for method chaining.
     */
    public NotSqlBuilder limit(int limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Sets the offset for the SQL query.
     * The offset specifies the number of rows to skip before starting to return rows.
     *
     * @param offset The number of rows to skip
     * @return This {@code NotSqlBuilder} instance for method chaining
     */
    public NotSqlBuilder offset(int offset) {
        this.offset = offset;
        return this;
    }

    /**
     * Sets a column value for an SQL UPDATE operation.
     * 
     * This method adds a column-value pair to the list of values to be updated.
     * The column name and value are stored as a NotSqlUpdateValue object.
     * 
     * @param column The name of the column to update
     * @param value The new value to set for the column
     * @return This builder instance, for method chaining
     */
    public NotSqlBuilder set(String column, Object value) {
        this.updateValues.add(NotSqlUpdateValue.of(column, value));
        return this;
    }

    /**
     * @brief Sets the update values using a list of NotSqlUpdateValue objects.
     *
     * This method clears any previously set update values and sets them to the values
     * provided in the input list.
     *
     * @param values The list of update values to set.
     * @return A reference to this NotSqlBuilder instance for method chaining.
     */
    public NotSqlBuilder setValues(List<NotSqlUpdateValue> values) {
        this.updateValues.addAll(values);
        return this;
    }

    /**
     * @brief Sets a raw SQL value for a column without escaping or quoting.
     *
     * This method allows setting a raw SQL expression as a value for a specified column.
     * The raw value will be inserted directly into the SQL query without any processing.
     * Use with caution as this can lead to SQL injection if not properly validated.
     *
     * @param column   The name of the column to update
     * @param rawValue The raw SQL expression to use as the value (will not be escaped)
     * @return This NotSqlBuilder instance for method chaining
     */
    public NotSqlBuilder setRaw(String column, String rawValue) {
        this.updateValues.add(NotSqlUpdateValue.ofRaw(column, rawValue));
        return this;
    }

    /**
     * @brief Sets a raw SQL value for a column in an update operation.
     * 
     * This method adds a raw SQL value to be used in an update query without escaping or formatting.
     * Use this when you need to insert a raw SQL expression like functions, calculations, or other SQL fragments.
     * 
     * @param column The name of the column to be updated
     * @param value The raw SQL value or expression to set for the column
     * @param values A list of parameter values that should be bound to any placeholders in the raw SQL
     * @return The current NotSqlBuilder instance for method chaining
     */
    public NotSqlBuilder setRaw(String column, String value, List<Object> values) {
        this.updateValues.add(NotSqlUpdateValue.ofRaw(column, value, values));
        return this;
    }

    /**
     * @brief Adds a list of values to be inserted in the SQL query.
     * @param values The list of values to be inserted.
     * @return A reference to this NotSqlBuilder instance for method chaining.
     */
    public NotSqlBuilder values(List<Object> values) {
        this.insertValues.add(values);
        return this;
    }

    /**
     * @brief Adds one or more values to the SQL builder.
     *
     * This method takes a variable number of values and adds them to the SQL builder.
     * The values are converted to a list and passed to the overloaded values() method.
     *
     * @param values The values to add to the builder. Can be of any object type.
     * @return The current NotSqlBuilder instance for method chaining.
     */
    public NotSqlBuilder values(Object... values) {
        List<Object> valuesList = new ArrayList<>();
        for (Object value : values) {
            valuesList.add(value);
        }
        return values(valuesList);
    }

    /**
     * @brief Sets the type of SQL query to be built.
     * 
     * @param queryType The type of query to set (e.g., SELECT, INSERT, UPDATE, DELETE)
     * @return A reference to this NotSqlBuilder instance for method chaining
     */
    private NotSqlBuilder setQueryType(QueryType queryType) {
        this.queryType = queryType;
        return this;
    }

    /**
     * @brief Builds the SQL query based on the configured query type.
     * 
     * This method assembles the final SQL query string according to the previously specified
     * query type (SELECT, INSERT, UPDATE, or DELETE) and parameters.
     * 
     * @return The complete SQL query string
     * @throws IllegalStateException If the query type has not been specified
     */
    public String build() {
        switch (queryType) {
            case SELECT:
                return buildSelectQuery();
            case INSERT:
                return buildInsertQuery();
            case UPDATE:
                return buildUpdateQuery();
            case DELETE:
                return buildDeleteQuery();
            default:
                throw new IllegalStateException("Query type not specified");
        }
    }

    /**
     * Builds a SELECT SQL query string based on the configured parameters.
     * 
     * The method constructs a query following this structure:
     * SELECT [columns] FROM [table] [joins] [where_conditions] [group_by] [order_by] [limit_offset]
     * 
     * - If no columns are specified, selects all columns (*)
     * - Includes any configured join clauses
     * - Adds WHERE conditions from the whereBuilder
     * - Appends any GROUP BY, ORDER BY, LIMIT, and OFFSET clauses if configured
     * 
     * @return A complete SQL SELECT query string
     */
    private String buildSelectQuery() {
        StringBuilder query = new StringBuilder("SELECT ");

        if (columns.isEmpty()) {
            query.append("*");
        } else {
            query.append(String.join(", ", columns));
        }

        query.append(" FROM ").append(table);

        if (!joinClauses.isEmpty()) {
            query.append(" ").append(String.join(" ", joinClauses));
        }

        whereBuilder.build(query);
        appendGroupByClause(query);
        appendOrderByClause(query);
        appendLimitOffset(query);

        return query.toString();
    }

    /**
     * @brief Builds an SQL INSERT query based on the configured table, columns, and values.
     *
     * This method constructs an INSERT statement with the following structure:
     * "INSERT INTO [table] ([columns]) VALUES ([values])"
     * 
     * If multiple sets of values have been added, it will create a multi-row insert:
     * "INSERT INTO [table] ([columns]) VALUES ([set1]), ([set2]), ..."
     * 
     * Each value is properly formatted according to its type using the formatValue method.
     *
     * @return A string containing the complete SQL INSERT query.
     */
    private String buildInsertQuery() {
        StringBuilder query = new StringBuilder("INSERT INTO ").append(table);

        if (!columns.isEmpty()) {
            query.append(" (").append(String.join(", ", columns)).append(")");
        }

        query.append(" VALUES ");

        List<String> valueGroups = new ArrayList<>();
        for (List<Object> valueSet : insertValues) {
            List<String> formattedValues = new ArrayList<>();
            for (Object value : valueSet) {
                formattedValues.add(formatValue(value));
            }
            valueGroups.add("(" + String.join(", ", formattedValues) + ")");
        }

        query.append(String.join(", ", valueGroups));

        return query.toString();
    }

    /**
     * @brief Builds an SQL UPDATE query with the current state of the builder
     * 
     * Constructs an SQL UPDATE query using the table name, update values, and where conditions
     * that have been configured for this builder. The method formats each update value as
     * "column = newValue" and joins them with commas.
     * 
     * @return String containing the complete SQL UPDATE query
     * 
     * @see NotSqlUpdateValue
     */
    private String buildUpdateQuery() {
        StringBuilder query = new StringBuilder("UPDATE ").append(table).append(" SET ");

        List<String> setStatements = new ArrayList<>();
        for (NotSqlUpdateValue update : updateValues) {
            setStatements.add(update.getColumn() + " = " + update.getNewValue());
        }

        query.append(String.join(", ", setStatements));

        whereBuilder.build(query);

        return query.toString();
    }

    /**
     * @brief Constructs a SQL DELETE query statement
     * 
     * Creates a DELETE query for the specified table with WHERE conditions
     * defined in the whereBuilder.
     * 
     * @return A String containing the complete SQL DELETE query
     */
    private String buildDeleteQuery() {
        StringBuilder query = new StringBuilder("DELETE FROM ").append(table);

        whereBuilder.build(query);

        return query.toString();
    }

    /**
     * @brief Appends the GROUP BY clause to the SQL query if any group by conditions exist.
     * 
     * This method checks if the groupByClauses collection contains any elements.
     * If it does, it appends a GROUP BY statement to the query followed by a comma-separated
     * list of all the group by conditions.
     * 
     * @param query The StringBuilder instance containing the SQL query being constructed.
     */
    private void appendGroupByClause(StringBuilder query) {
        if (!groupByClauses.isEmpty()) {
            query.append(" GROUP BY ").append(String.join(", ", groupByClauses));
        }
    }

    /**
     * @brief Appends ORDER BY clause to the SQL query if any order by clauses are defined.
     * 
     * This method checks if there are any order by clauses defined in the orderByClauses collection.
     * If there are, it appends the " ORDER BY " string to the query followed by the clauses joined with commas.
     * 
     * @param query The StringBuilder to which the ORDER BY clause will be appended.
     */
    private void appendOrderByClause(StringBuilder query) {
        if (!orderByClauses.isEmpty()) {
            query.append(" ORDER BY ").append(String.join(", ", orderByClauses));
        }
    }

    /**
     * @brief Appends LIMIT and OFFSET clauses to the SQL query if they are specified.
     * 
     * This method checks if a limit value has been set and appends it to the query.
     * Additionally, if an offset value has been set, it appends the OFFSET clause after the LIMIT clause.
     * 
     * @param query The StringBuilder containing the SQL query being constructed
     */
    private void appendLimitOffset(StringBuilder query) {
        if (limit != null) {
            query.append(" LIMIT ").append(limit);
            
            if (offset != null) {
                query.append(" OFFSET ").append(offset);
            }
        }
    }

    /**
     * @brief Formats a value for SQL query.
     * @details This method formats a value for use in SQL queries. If the value is null, it returns "NULL",
     *          otherwise it returns "?" which will be replaced with the actual value when the query is executed.
     * @param value The value to format.
     * @return "NULL" if the value is null, otherwise "?".
     */
    private String formatValue(Object value) {
        if (value == null) return "NULL";
        return "?";
    }

    /**
     * Retrieves all parameters used in the SQL query.
     * 
     * This method collects all parameter values that will be used with prepared statements.
     * For UPDATE queries, it gathers values from all update operations.
     * For INSERT queries, it collects values from all insert operations.
     * It also includes all parameters from the WHERE clause.
     * 
     * @return A list containing all parameter values for the query in the order they appear in the SQL statement
     */
    public List<Object> getParameters() {
        List<Object> params = new ArrayList<>();
                
        if (queryType == QueryType.UPDATE) {
            for (NotSqlUpdateValue update : updateValues) {
                params.addAll(update.getValues());
            }
        }
        
        if (queryType == QueryType.INSERT) {
            for (List<Object> values : insertValues) {
                params.addAll(values);
            }
        }

        params.addAll(whereBuilder.getParameters());
        
        return params;
    }
}