package com.notmarra.notlib.database.query;

import java.util.List;

/**
 * @brief Represents a value to be updated in a SQL UPDATE statement.
 *
 * This class encapsulates the information needed to create a SQL UPDATE parameter,
 * including the column name, the new value expression, and the actual values to be used.
 * It supports both prepared statement values and raw SQL expressions.
 *
 * @details The class provides several factory methods for different update scenarios:
 * - Simple column=? updates
 * - Custom expressions with placeholders
 * - Raw SQL expressions
 * - Support for multiple values
 */
public class NotSqlUpdateValue {
    private final String column;
    private final String newValue;
    private final List<Object> values;
    private final boolean isRaw;

    /**
     * @brief Constructor for creating an update value with a single parameter.
     * 
     * This constructor initializes a NotSqlUpdateValue object with a column name, 
     * new value expression, and a single parameter value.
     * 
     * @param column The name of the column to be updated
     * @param newValue The SQL expression representing the new value
     * @param value The parameter value to be bound to the expression
     * @param isRaw Flag indicating whether the newValue should be treated as raw SQL (true) or as a parameterized statement (false)
     */
    public NotSqlUpdateValue(String column, String newValue, Object value, boolean isRaw) {
        this(column, newValue, List.of(value), isRaw);
    }

    /**
     * @brief Constructor for NotSqlUpdateValue class.
     *
     * Creates a new instance of NotSqlUpdateValue with the specified column, value, parameters, and raw flag.
     *
     * @param column The name of the column to update.
     * @param newValue The new value or placeholder to set for the column.
     * @param values List of parameter values associated with the update operation.
     * @param isRaw Flag indicating whether the new value should be treated as raw SQL (true) or as a parameter (false).
     */
    public NotSqlUpdateValue(String column, String newValue, List<Object> values, boolean isRaw) {
        this.column = column;
        this.newValue = newValue;
        this.values = values;
        this.isRaw = isRaw;
    }

    /**
     * @brief Checks if this update operation involves multiple values.
     *
     * @return true if multiple values are being updated, false if only one value is being updated
     */
    public boolean isMulti() { return values.size() > 1; }
    /**
     * @brief Retrieves the column name for this SQL update value.
     * @return The column name as a String.
     */
    public String getColumn() { return column; }
    /**
     * @brief Gets the new value.
     * @return The new value as a string.
     */
    public String getNewValue() { return newValue; }
    /**
     * Retrieves the list of values for this update operation.
     *
     * @return List of values to be used in the SQL UPDATE statement
     */
    public List<Object> getValues() { return values; }
    /**
     * Determines if the value is a raw SQL expression.
     *
     * @return true if the value represents a raw SQL expression rather than a literal value,
     *         false otherwise.
     */
    public boolean isRaw() { return isRaw; }

    /**
     * Creates a new SQL update value for a specified column with a value to be replaced by a placeholder.
     *
     * @param column The name of the column to update
     * @param value The value to set for the column (will be represented as a placeholder)
     * @return A new NotSqlUpdateValue object representing this update operation
     */
    public static NotSqlUpdateValue of(String column, Object value) {
        return new NotSqlUpdateValue(column, "?", value, false);
    }

    /**
     * @brief Creates a new NotSqlUpdateValue instance with the specified column, SQL expression, and parameter value.
     * 
     * This factory method creates an update value where the right side of the assignment is a raw SQL expression
     * that may contain parameter placeholders, along with the value to be bound to that placeholder.
     * 
     * @param column The database column name to update
     * @param newValue A SQL expression string representing the new value (may contain parameter placeholders)
     * @param value The parameter value to bind to the SQL expression
     * @return A new NotSqlUpdateValue instance
     * 
     * @see NotSqlUpdateValue#NotSqlUpdateValue(String, String, Object, boolean)
     */
    public static NotSqlUpdateValue of(String column, String newValue, Object value) {
        return new NotSqlUpdateValue(column, newValue, value, false);
    }

    /**
     * Creates a new SqlUpdateValue with a string value.
     *
     * @param column The column name to update.
     * @param newValue The SQL expression or placeholder for the new value.
     * @param values The list of values to bind to placeholders in the expression.
     * @return A new NotSqlUpdateValue instance.
     */
    public static NotSqlUpdateValue of(String column, String newValue, List<Object> values) {
        return new NotSqlUpdateValue(column, newValue, values, false);
    }

    /**
     * Creates a raw update value for an SQL UPDATE statement.
     * 
     * @param column The column name to be updated
     * @param value The value to update the column with
     * @return A new NotSqlUpdateValue instance that represents a raw value assignment
     */
    public static NotSqlUpdateValue ofRaw(String column, Object value) {
        return new NotSqlUpdateValue(column, "?", value, true);
    }

    /**
     * Creates a new NotSqlUpdateValue with raw SQL value.
     * This method allows you to use raw SQL expressions as values in your update queries.
     * 
     * @param column The name of the column to update
     * @param newValue The new value as a string representation
     * @param rawValue The raw SQL value/expression to use in the query
     * @return A new NotSqlUpdateValue instance with raw SQL value
     */
    public static NotSqlUpdateValue ofRaw(String column, String newValue, String rawValue) {
        return new NotSqlUpdateValue(column, newValue, rawValue, true);
    }

    /**
     * Creates a new NotSqlUpdateValue instance with raw SQL for the new value.
     * 
     * @param column   The name of the column to update
     * @param newValue Raw SQL expression for the new value (will not be escaped)
     * @param values   List of parameter values to be bound to the SQL expression
     * @return A new NotSqlUpdateValue instance with the given column and raw SQL value
     */
    public static NotSqlUpdateValue ofRaw(String column, String newValue, List<Object> values) {
        return new NotSqlUpdateValue(column, newValue, values, true);
    }
}
