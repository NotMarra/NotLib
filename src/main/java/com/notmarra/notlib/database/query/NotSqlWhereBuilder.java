package com.notmarra.notlib.database.query;

import java.util.ArrayList;
import java.util.List;

/**
 * @class NotSqlWhereBuilder
 * 
 * @brief A builder class for constructing SQL WHERE clauses in a fluent API style.
 * 
 * This class provides a convenient way to build complex SQL WHERE conditions
 * with proper parameter handling to prevent SQL injection. It supports various
 * comparison operators, logical AND/OR combinations, and special conditions like
 * IN, LIKE, and NULL checks.
 * 
 * @example
 * ```
 * NotSqlWhereBuilder whereBuilder = NotSqlWhereBuilder.create()
 *     .andEquals("id", 1)
 *     .or("status", "=", "active")
 *     .andIn("category", Arrays.asList("books", "electronics"));
 * ```
 * 
 * @note The class handles parameter preparation automatically and properly escapes values
 *       to prevent SQL injection vulnerabilities.
 * 
 */
public class NotSqlWhereBuilder {
    private List<NotWhereClause> whereClauses = new ArrayList<>();

    /**
     * @brief Creates and returns a new instance of the NotSqlWhereBuilder.
     * 
     * This static factory method provides a convenient way to instantiate the NotSqlWhereBuilder
     * without directly using the constructor.
     * 
     * @return A new NotSqlWhereBuilder instance ready for building SQL WHERE clauses.
     */
    public static NotSqlWhereBuilder create() {
        return new NotSqlWhereBuilder();
    }

    /**
     * @brief Creates a new NotSqlWhereBuilder instance with an initial condition.
     * 
     * This static factory method creates a new NotSqlWhereBuilder and adds an initial condition using the AND operator.
     * 
     * @param column The database column name to filter on
     * @param operator The comparison operator (e.g., "=", ">", "<", "LIKE", etc.)
     * @param value The value to compare against the column
     * @return A new NotSqlWhereBuilder instance with the initial condition applied
     */
    public static NotSqlWhereBuilder of(String column, String operator, Object value) {
        return (new NotSqlWhereBuilder()).and(column, operator, value);
    }

    /**
     * Creates a new instance of NotSqlWhereBuilder with the given raw SQL condition.
     *
     * This factory method creates a NotSqlWhereBuilder and adds the raw condition using the AND operator.
     *
     * @param rawCondition the raw SQL condition to be added to the where clause
     * @return a new NotSqlWhereBuilder instance with the specified condition
     */
    public static NotSqlWhereBuilder of(String rawCondition) {
        return (new NotSqlWhereBuilder()).and(rawCondition);
    }

    /**
     * @brief Creates a new NotSqlWhereBuilder with an initial OR condition
     * 
     * This static factory method creates a new NotSqlWhereBuilder instance and initializes it
     * with an OR condition using the specified column, operator, and value.
     * 
     * @param column The database column name to use in the condition
     * @param operator The SQL operator to use (e.g., "=", ">", "<", "LIKE", etc.)
     * @param value The value to compare against the column
     * @return A new NotSqlWhereBuilder instance with the initial OR condition
     */
    public static NotSqlWhereBuilder ofOr(String column, String operator, Object value) {
        return (new NotSqlWhereBuilder()).or(column, operator, value);
    }

    /**
     * Creates a new NotSqlWhereBuilder instance with an initial 'OR' condition.
     * This is a factory method that simplifies creating a builder with an OR condition.
     *
     * @param rawCondition The raw SQL condition string to be added with an OR operator
     * @return A new NotSqlWhereBuilder instance with the specified OR condition
     */
    public static NotSqlWhereBuilder ofOr(String rawCondition) {
        return (new NotSqlWhereBuilder()).or(rawCondition);
    }

    /**
     * @brief Adds a new WHERE clause with the AND condition to the query.
     *
     * This method appends a new WHERE clause to the existing query using the AND logical operator.
     * The clause is constructed using the provided column name, operator, and value.
     *
     * @param column The name of the column to compare
     * @param operator The comparison operator (e.g., "=", "<", ">", "LIKE", etc.)
     * @param value The value to compare against the column
     * @return This builder instance, allowing for method chaining
     */
    public NotSqlWhereBuilder and(String column, String operator, Object value) {
        this.whereClauses.add(new NotWhereClause(column, operator, value, "AND"));
        return this;
    }

    /**
     * @brief Adds a new WHERE condition with AND logic operator.
     * 
     * Appends a new condition to the existing WHERE clause using the AND operator.
     * This allows for combining multiple conditions that all must be satisfied.
     * 
     * @param rawCondition The SQL condition to be added to the WHERE clause
     * @return Reference to the same NotSqlWhereBuilder instance for method chaining
     */
    public NotSqlWhereBuilder and(String rawCondition) {
        this.whereClauses.add(new NotWhereClause(rawCondition, "AND"));
        return this;
    }

    /**
     * @brief Adds an equality condition to the WHERE clause with an AND operator.
     *
     * This method appends a new condition to the existing WHERE clause using the AND logical operator.
     * The condition checks if the specified column equals the provided value.
     *
     * @param column The database column name to compare
     * @param value The value to compare against the column
     * @return This builder instance for method chaining
     */
    public NotSqlWhereBuilder andEquals(String column, Object value) {
        this.whereClauses.add(new NotWhereClause(column, "=", value, "AND"));
        return this;
    }

    /**
     * @brief Adds a "AND column != value" clause to the SQL WHERE statement.
     *
     * This method adds a new WHERE clause that checks if the specified column is not equal to the given value,
     * connected with the previous clause (if any) using the AND operator.
     *
     * @param column The column name to compare
     * @param value The value to compare the column against
     * @return This builder instance for method chaining
     */
    public NotSqlWhereBuilder andNotEquals(String column, Object value) {
        this.whereClauses.add(new NotWhereClause(column, "!=", value, "AND"));
        return this;
    }

    /**
     * @brief Adds a "less than" condition to the WHERE clause, connected with AND.
     * 
     * This method appends a condition to the WHERE clause of the SQL query where the specified column
     * is less than the provided value. The condition is joined to previous conditions with an AND operator.
     *
     * @param column The name of the database column to compare
     * @param value The value to compare against the column
     * @return This builder instance for method chaining
     */
    public NotSqlWhereBuilder andLessThan(String column, Object value) {
        this.whereClauses.add(new NotWhereClause(column, "<", value, "AND"));
        return this;
    }

    /**
     * @brief Adds a 'less than or equal' condition to the WHERE clause with an AND operator.
     * 
     * This method creates a new WHERE clause condition that checks if the specified column
     * has a value less than or equal to the provided value, and joins it with an AND operator
     * to any existing conditions.
     * 
     * @param column The column name to compare
     * @param value The value to compare against the column
     * @return This builder instance for method chaining
     */
    public NotSqlWhereBuilder andLessThanOrEquals(String column, Object value) {
        this.whereClauses.add(new NotWhereClause(column, "<=", value, "AND"));
        return this;
    }

    /**
     * Adds a greater than condition to the WHERE clause connected with AND.
     *
     * @param column The column name to compare
     * @param value The value to compare the column with
     * @return The current builder instance for method chaining
     */
    public NotSqlWhereBuilder andGreaterThan(String column, Object value) {
        this.whereClauses.add(new NotWhereClause(column, ">", value, "AND"));
        return this;
    }

    /**
     * @brief Adds an AND condition to the WHERE clause with the >= operator.
     * 
     * This method adds a condition to the WHERE clause that checks if the specified column's value
     * is greater than or equal to the given value. The condition is combined with previous conditions
     * using the AND logical operator.
     * 
     * @param column The name of the column to compare
     * @param value The value to compare the column against
     * @return The current NotSqlWhereBuilder instance for method chaining
     */
    public NotSqlWhereBuilder andGreaterThanOrEquals(String column, Object value) {
        this.whereClauses.add(new NotWhereClause(column, ">=", value, "AND"));
        return this;
    }

    /**
     * @brief Adds an "AND IN" condition to the WHERE clause.
     * 
     * This method creates a SQL WHERE condition that checks if the specified column value is
     * within a given list of values, and joins it with an AND operator.
     * 
     * @param column The name of the column to check.
     * @param values A list of values to check against the column.
     * @return The current NotSqlWhereBuilder instance for method chaining.
     */
    public NotSqlWhereBuilder andIn(String column, List<Object> values) {
        StringBuilder inClause = new StringBuilder(column + " IN (");
        for (int i = 0; i < values.size(); i++) {
            inClause.append(formatValue(values.get(i)));
            if (i < values.size() - 1) {
                inClause.append(", ");
            }
        }
        inClause.append(")");
        this.whereClauses.add(new NotWhereClause(inClause.toString(), values, "AND"));
        return this;
    }

    /**
     * Adds a NOT IN condition to the WHERE clause with AND operator.
     * This condition checks if the specified column's value is not in the provided list of values.
     * 
     * @param column The name of the column to check
     * @param values The list of values to check against
     * @return The current NotSqlWhereBuilder instance for method chaining
     */
    public NotSqlWhereBuilder andNotIn(String column, List<Object> values) {
        StringBuilder notInClause = new StringBuilder(column + " NOT IN (");
        for (int i = 0; i < values.size(); i++) {
            notInClause.append(formatValue(values.get(i)));
            if (i < values.size() - 1) {
                notInClause.append(", ");
            }
        }
        notInClause.append(")");
        this.whereClauses.add(new NotWhereClause(notInClause.toString(), values, "AND"));
        return this;
    }

    /**
     * @brief Adds a LIKE condition to the WHERE clause with an AND operator.
     *
     * This method appends a condition to the WHERE clause that checks if the specified column
     * contains the pattern specified by the value parameter. The condition is combined with
     * the previous conditions using the AND operator.
     *
     * @param column The name of the column to compare.
     * @param value The pattern to match (can use wildcards like % and _).
     * @return The current NotSqlWhereBuilder instance for method chaining.
     */
    public NotSqlWhereBuilder andLike(String column, Object value) {
        this.whereClauses.add(new NotWhereClause(column, "LIKE", value, "AND"));
        return this;
    }

    /**
     * Adds a NOT LIKE condition to the WHERE clause with an AND operator.
     * 
     * @param column The column name to compare
     * @param value The value to compare against
     * @return This builder instance for method chaining
     */
    public NotSqlWhereBuilder andNotLike(String column, Object value) {
        this.whereClauses.add(new NotWhereClause(column, "NOT LIKE", value, "AND"));
        return this;
    }

    /**
     * @brief Adds an `AND IS NULL` condition to the WHERE clause.
     *
     * This method adds a condition checking if the specified column is NULL,
     * and connects it with the previous conditions using the AND operator.
     *
     * @param column The name of the column to check for NULL
     * @return The current NotSqlWhereBuilder instance for method chaining
     */
    public NotSqlWhereBuilder andNull(String column) {
        this.whereClauses.add(new NotWhereClause(column, "IS NULL", null, "AND"));
        return this;
    }

    /**
     * Adds a NOT NULL check for a specified column to the WHERE clause with AND operator.
     * This condition checks if the column does not contain a NULL value.
     *
     * @param column the name of the column to check for not being NULL
     * @return this builder instance for method chaining
     */
    public NotSqlWhereBuilder andNotNull(String column) {
        this.whereClauses.add(new NotWhereClause(column, "IS NOT NULL", null, "AND"));
        return this;
    }

    /**
     * @brief Adds an "OR" condition to the SQL WHERE clause.
     * 
     * This method appends a new WHERE condition connected with "OR" logical operator.
     * 
     * @param column The database column name to check in the condition
     * @param operator The comparison operator (e.g., "=", "<>", ">", "<", ">=", "<=", "LIKE", etc.)
     * @param value The value to compare against the column
     * @return The current NotSqlWhereBuilder instance for method chaining
     */
    public NotSqlWhereBuilder or(String column, String operator, Object value) {
        this.whereClauses.add(new NotWhereClause(column, operator, value, "OR"));
        return this;
    }

    /**
     * @brief Adds a raw "OR" condition to the WHERE clause.
     * 
     * Appends the specified raw condition to the WHERE clause with an "OR" logical operator.
     * This allows for direct SQL condition strings to be included in the query.
     * 
     * @param rawCondition The raw SQL condition string to be added with "OR" operator
     * @return This builder instance for method chaining
     */
    public NotSqlWhereBuilder or(String rawCondition) {
        this.whereClauses.add(new NotWhereClause(rawCondition, "OR"));
        return this;
    }

    /**
     * @brief Formats a value for inclusion in a SQL query.
     * 
     * This method handles formatting of values for SQL queries. It returns "NULL" for null values
     * and a placeholder "?" for all other values, which is suitable for prepared statements.
     * 
     * @param value The value to be formatted, can be null
     * @return "NULL" if the input value is null, otherwise "?" as a placeholder
     */
    private String formatValue(Object value) {
        if (value == null) return "NULL";
        return "?";
    }

    /**
     * @brief Builds the SQL "WHERE" clause segment of a query.
     * 
     * This method constructs the WHERE clause by iteratively processing all where clauses
     * that have been added to this builder. It handles different types of where clauses:
     * - Raw SQL conditions
     * - Multi-value clauses
     * - Standard column-operator-value clauses
     * 
     * Each clause is connected with its logical operator (AND, OR, etc.), except for the first one.
     * 
     * @param query The StringBuilder instance to which the WHERE clause will be appended
     */
    public void build(StringBuilder query) {
        if (!whereClauses.isEmpty()) {
            query.append(" WHERE ");
            boolean isFirst = true;

            for (NotWhereClause clause : whereClauses) {
                if (!isFirst) {
                    query.append(" ").append(clause.getLogicalOperator()).append(" ");
                }
                
                if (clause.isRaw()) {
                    query.append(clause.getRawCondition());
                } else {
                    if (clause.isMulti()) {
                        query.append(clause.getSql());
                    } else {
                        query.append(clause.getSql())
                            .append(" ")
                            .append(clause.getOperator())
                            .append(" ")
                            .append(formatValue(clause.getValue()));
                    }
                }
                
                isFirst = false;
            }
        }
    }

    /**
     * @brief Retrieves all parameter values from non-raw where clauses.
     * 
     * This method collects the parameters from all where clauses that are not marked as raw.
     * Raw clauses are SQL fragments that should be used as-is without parameter binding.
     * 
     * @return A list of parameter values that should be bound to the prepared statement.
     */
    public List<Object> getParameters() {
        List<Object> params = new ArrayList<>();
        
        for (NotWhereClause clause : whereClauses) {
            if (!clause.isRaw()) params.addAll(clause.getValues());
        }
        
        return params;
    }

    /**
     * @class NotWhereClause
     * @brief Represents a SQL WHERE clause condition in the NotLib query builder.
     *
     * This private static class encapsulates the components of a SQL WHERE condition including
     * the SQL fragment, operator, values, and logical operator (AND/OR) that connects it to other conditions.
     * It supports both parameterized and raw SQL conditions.
     *
     * @details The class provides three constructors for different use cases:
     * 1. Single value condition (e.g., column = value)
     * 2. Raw SQL condition without parameterization
     * 3. Multi-value condition (e.g., column IN (values))
     */
    private static class NotWhereClause {
        private String sql;
        private String operator;
        private List<Object> values;
        private String logicalOperator;
        private boolean isRaw;
        private String rawCondition;

        /**
         * @brief Constructor for NotWhereClause with a single value.
         * 
         * Creates a new where clause with the specified SQL condition, operator, 
         * and a single value. This clause can be combined with others using the 
         * specified logical operator (AND/OR).
         * 
         * @param sql The SQL fragment representing the column or expression to evaluate.
         * @param operator The comparison operator (e.g., "=", "<>", ">", "<", etc.).
         * @param value The value to compare against.
         * @param logicalOperator The logical operator ("AND" or "OR") to use when combining with other clauses.
         */
        public NotWhereClause(String sql, String operator, Object value, String logicalOperator) {
            this.sql = sql;
            this.operator = operator;
            this.values = List.of(value);
            this.logicalOperator = logicalOperator;
            this.isRaw = false;
        }

        /**
         * @brief Constructs a NotWhereClause with a raw condition and logical operator.
         *
         * This constructor creates a NotWhereClause instance using a raw SQL condition string
         * and a logical operator (e.g., "AND", "OR") that will be used to combine this clause
         * with other clauses in a SQL WHERE statement.
         *
         * @param rawCondition The raw SQL condition string to be used in the WHERE clause
         * @param logicalOperator The logical operator to combine this clause with others (e.g., "AND", "OR")
         */
        public NotWhereClause(String rawCondition, String logicalOperator) {
            this.rawCondition = rawCondition;
            this.logicalOperator = logicalOperator;
            this.isRaw = true;
        }

        /**
         * @brief Constructs a new NotWhereClause with the provided SQL condition, values, and logical operator.
         *
         * @param sql The SQL condition string to be used in the WHERE clause.
         * @param values List of parameter values associated with the SQL condition.
         * @param logicalOperator The logical operator to combine this clause with others (e.g., "AND", "OR").
         *
         * @note The constructed clause is not considered a raw SQL clause.
         */
        public NotWhereClause(String sql, List<Object> values, String logicalOperator) {
            this.sql = sql;
            this.values = values;
            this.logicalOperator = logicalOperator;
            this.isRaw = false;
        }

        /**
         * @brief Checks if multiple values are stored in this builder.
         *
         * @return true if more than one value is present, false otherwise
         */
        public boolean isMulti() { return values.size() > 1; }

        /**
         * @brief Retrieves the SQL query string constructed by this builder.
         *
         * @return The SQL query string.
         */
        public String getSql() { return sql; }
        /**
         * @brief Gets the operator used in this SQL WHERE condition.
         * 
         * @return The operator as a String.
         */
        public String getOperator() { return operator; }
        /**
         * @brief Retrieves the list of parameter values used in the WHERE clause conditions.
         * @return List containing all parameter values that will be bound to prepared statements.
         */
        public List<Object> getValues() { return values; }
        /**
         * @brief Gets the first value stored in the values list or null if the list is empty.
         * @return The first value in the values list, or null if the list is empty.
         */
        public Object getValue() { return values.isEmpty() ? null : values.get(0); }
        /**
         * Retrieves the logical operator used in the WHERE clause.
         * 
         * @return String containing the logical operator (e.g., "AND", "OR")
         */
        public String getLogicalOperator() { return logicalOperator; }
        /**
         * @brief Checks if the where condition is a raw SQL condition
         * 
         * @return true if the where condition is a raw SQL condition, false otherwise
         */
        public boolean isRaw() { return isRaw; }
        /**
         * @brief Get the raw SQL condition string.
         * @return the raw SQL condition string used in the WHERE clause
         */
        public String getRawCondition() { return rawCondition; }
    }
}