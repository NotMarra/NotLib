package com.notmarra.notlib.database;

import com.notmarra.notlib.NotLib;
import com.notmarra.notlib.database.query.NotSqlBuilder;
import com.notmarra.notlib.database.query.NotSqlQueryExecutor;
import com.notmarra.notlib.database.structure.NotRecord;
import com.notmarra.notlib.database.structure.NotTable;
import com.notmarra.notlib.extensions.NotConfigurable;
import com.notmarra.notlib.extensions.NotPlugin;
import com.notmarra.notlib.utils.NotDebugger;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * @class NotDatabase
 * @brief An abstract base class for database operations with configurable
 *        settings.
 * 
 *        NotDatabase provides a foundation for database interactions with
 *        built-in connection pooling via HikariCP.
 *        It supports common SQL operations including select, insert, update,
 *        and delete operations through
 *        a fluent SQL builder interface.
 * 
 * @extends NotConfigurable
 * 
 * @details The class manages database connections and provides methods for:
 *          - Connection management via HikariCP
 *          - Table creation and management
 *          - SQL query execution
 *          - Data type conversion
 *          - Configuration handling
 * 
 *          Implementations must provide database-specific logic for connecting,
 *          creating tables, and inserting data.
 * 
 */
public abstract class NotDatabase extends NotConfigurable {
    private final String defaultConfig;
    private String customId;
    protected HikariDataSource source;
    private NotSqlQueryExecutor queryExecutor;
    private Map<String, NotTable> tables = new HashMap<>();

    public NotDatabase(NotPlugin plugin, String defaultConfig) {
        super(plugin);
        this.defaultConfig = defaultConfig;
        this.queryExecutor = new NotSqlQueryExecutor(this);
    }

    public void setCustomId(String id) {
        this.customId = id;
    }

    public String getId() {
        if (customId != null)
            return customId;

        ConfigurationSection config = getDatabaseConfig();
        String configId = config.getString("databaseId");
        if (configId != null)
            return configId;

        return plugin.getName();
    }

    @Override
    public List<String> getConfigPaths() {
        return List.of(defaultConfig);
    }

    public abstract List<NotTable> setupTables();

    public abstract void connect();

    public abstract boolean createTable(NotTable table);

    public abstract boolean insertRow(NotTable table, List<Object> row);

    public NotSqlBuilder select(String table) {
        return NotSqlBuilder.select(table);
    }

    public NotSqlBuilder select(String table, List<String> columns) {
        return NotSqlBuilder.select(table, columns);
    }

    public NotSqlBuilder insertInto(String table) {
        return NotSqlBuilder.insertInto(table);
    }

    public NotSqlBuilder update(String table) {
        return NotSqlBuilder.update(table);
    }

    public NotSqlBuilder deleteFrom(String table) {
        return NotSqlBuilder.deleteFrom(table);
    }

    public boolean exists(NotSqlBuilder builder) {
        return queryExecutor.exists(builder);
    }

    public NotRecord selectOne(NotSqlBuilder builder) {
        return queryExecutor.selectOne(builder);
    }

    public List<NotRecord> select(NotSqlBuilder builder) {
        return queryExecutor.select(builder);
    }

    public int execute(NotSqlBuilder builder) {
        return queryExecutor.update(builder);
    }

    public NotSqlQueryExecutor getQueryExecutor() {
        return queryExecutor;
    }

    public Map<String, NotTable> getTables() {
        return tables;
    }

    public NotTable getTable(String tableName) {
        return tables.get(tableName);
    }

    /**
     * @brief Initializes the database by setting up all tables.
     * 
     *        This method retrieves all tables that need to be set up, sets their
     *        database context,
     *        creates the database structures for each table, and stores them in the
     *        tables map
     *        indexed by their names.
     */
    public void setup() {
        for (NotTable table : setupTables()) {
            table.setDbCtx(this);
            table.createDb();
            tables.put(table.getName(), table);
        }
    }

    /**
     * @brief Handles the configuration reload event.
     * 
     *        This method is called when configurations are reloaded. It closes the
     *        current database
     *        connection and establishes a new connection to reflect any changes in
     *        the configuration.
     * 
     * @param reloadedConfigs List of configuration files that were reloaded
     */
    @Override
    public void onConfigReload(List<String> reloadedConfigs) {
        close();
        connect();
    }

    /**
     * @brief Retrieves the database-specific configuration section.
     * 
     *        This method gets the configuration section from the main configuration
     *        file
     *        that is specific to this database instance. The section is identified
     *        by
     *        "database.[database_id]" where [database_id] is the ID returned by
     *        getId().
     * 
     * @return ConfigurationSection containing database-specific configuration.
     *         If the section doesn't exist, returns an empty YamlConfiguration.
     */
    public ConfigurationSection getDatabaseConfig() {
        FileConfiguration config = getConfig(getConfigPaths().get(0));
        ConfigurationSection section = config.getConfigurationSection("data");
        return (section == null) ? new YamlConfiguration() : section;
    }

    /**
     * Sets the data source for the database.
     *
     * @param source The HikariDataSource to be used for database connections
     */
    public void setSource(HikariDataSource source) {
        this.source = source;
    }

    /**
     * @brief Retrieves a connection from the underlying DataSource.
     * 
     * @return A Connection object representing a connection to the database.
     * @throws SQLException If the DataSource is not initialized, is closed, or if a
     *                      database access error occurs.
     */
    public Connection getConnection() throws SQLException {
        if (source == null || source.isClosed()) {
            throw new SQLException("DataSource is not initialized or closed");
        }
        return source.getConnection();
    }

    /**
     * Closes the database connection.
     * 
     * This method safely closes the data source if it exists and is not already
     * closed.
     * It's recommended to call this method when the database is no longer needed to
     * release resources and prevent memory leaks.
     * 
     * @see javax.sql.DataSource#close()
     */
    public void close() {
        if (source != null && !source.isClosed())
            source.close();
    }

    /**
     * @brief Checks if the database connection is active
     * @return true if the connection is established and open, false otherwise
     */
    public boolean isConnected() {
        return source != null && !source.isClosed();
    }

    /**
     * @brief Converts a value to the appropriate Java type based on a SQL column
     *        type.
     *
     *        This method takes a SQL column type and a value and converts the value
     *        to the appropriate Java type
     *        that corresponds to that column type. For example, it converts values
     *        for "INT" columns to Integer objects,
     *        values for "FLOAT" columns to Float objects, etc.
     *
     * @param columnType The SQL data type of the column (e.g., "TEXT", "INT",
     *                   "FLOAT")
     * @param value      The value to convert to the appropriate Java type
     * @return The value converted to the appropriate Java type based on the column
     *         type,
     *         or null if the input value is null. If the column type is not
     *         supported,
     *         the value is returned as a String and an error is logged.
     */
    public Object convertValue(String columnType, Object value) {
        if (value == null) {
            return null;
        }

        switch (columnType.toUpperCase()) {
            case "TEXT":
            case "VARCHAR":
            case "CHAR":
            case "LONGTEXT":
            case "MEDIUMTEXT":
            case "TINYTEXT":
            case "STRING":
                return String.valueOf(value);
            case "INT":
            case "INTEGER":
            case "SMALLINT":
            case "TINYINT":
            case "MEDIUMINT":
                return Integer.valueOf(String.valueOf(value));
            case "BIGINT":
                return Long.valueOf(String.valueOf(value));
            case "REAL":
            case "DOUBLE":
                return Double.valueOf(String.valueOf(value));
            case "FLOAT":
                return Float.valueOf(String.valueOf(value));
            case "BOOLEAN":
            case "BOOL":
                return Boolean.valueOf(String.valueOf(value));
            case "DATE":
            case "TIME":
            case "DATETIME":
            case "TIMESTAMP":
                return String.valueOf(value);
            default:
                getLogger().error("Unsupported column type: " + columnType + ". Returning as string.");
                return String.valueOf(value);
        }
    }

    /**
     * @brief Processes a database operation using a provided consumer.
     * 
     *        This method opens a database connection, passes it to the provided
     *        consumer for processing,
     *        and ensures the connection is properly closed afterwards. Any
     *        exceptions during processing
     *        are caught, logged, and the stack trace is printed.
     * 
     * @param consumer A Consumer functional interface that accepts a Connection
     *                 object to perform
     *                 database operations.
     */
    public void process(Consumer<Connection> consumer) {
        try (Connection connection = getConnection()) {
            consumer.accept(connection);
        } catch (Exception e) {
            getLogger().error("Error processing connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * @brief Executes custom operations on a database statement.
     * 
     *        This method creates a connection and statement, then passes them to
     *        the provided consumer
     *        for custom database operations. The connection is automatically closed
     *        after execution.
     * 
     * @param consumer A BiConsumer that accepts a Connection and Statement to
     *                 perform database operations
     * 
     * @note The connection and statement are automatically managed, including
     *       proper closing of resources
     * @note Any exceptions during execution are caught, logged, and printed to
     *       stack trace
     */
    public void processStatement(BiConsumer<Connection, Statement> consumer) {
        try (Connection connection = getConnection()) {
            consumer.accept(connection, connection.createStatement());
        } catch (Exception e) {
            getLogger().error("Error processing statement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * @brief Executes a SQL statement that returns a boolean value.
     * 
     *        This method executes the specified SQL statement on the database
     *        connection
     *        and returns the boolean result. The SQL statement is debugged if
     *        logging is enabled.
     * 
     * @param sql The SQL statement to execute
     * @return true if the first result is a ResultSet object; false if it is an
     *         update count or there are no results
     * @throws SQLException if a database access error occurs or the given SQL
     *                      statement produces a ResultSet object that is then
     *                      automatically closed
     * @see java.sql.Statement#execute(String)
     */
    public boolean processResult(String sql) {
        NotLib.dbg().log(NotDebugger.C_DATABASE, "[processResult] SQL: " + sql);

        try (Connection connection = getConnection()) {
            return connection.createStatement().execute(sql);
        } catch (Exception e) {
            getLogger().error("Error processing result: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @brief Executes an SQL update statement with the provided parameters.
     * 
     *        This method executes SQL statements that modify the database (INSERT,
     *        UPDATE, DELETE).
     *        It logs the SQL statement and parameters for debugging purposes.
     * 
     * @param sql    The SQL update statement to execute
     * @param params List of parameters to be set in the prepared statement
     * 
     * @return The number of rows affected by the SQL statement, or -1 if an error
     *         occurs
     * 
     * @throws SQLException Handled internally, errors are logged
     */
    public int processUpdate(String sql, List<Object> params) {
        NotLib.dbg().log(NotDebugger.C_DATABASE, "[processUpdate] SQL: " + sql);
        if (!params.isEmpty()) {
            NotLib.dbg().log(NotDebugger.C_DATABASE, "[processUpdate] Params: " + params);
        }

        try {
            Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql);

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            return stmt.executeUpdate();
        } catch (SQLException e) {
            getLogger().error("Error processing update: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Executes a SELECT SQL query and returns the results as a list of NotRecord
     * objects.
     * 
     * This method prepares and executes the provided SQL query with the given
     * parameters.
     * It processes the ResultSet and converts each row into a NotRecord object with
     * appropriate
     * type conversions for different SQL data types.
     * 
     * @param sql    The SELECT SQL query to execute
     * @param params List of parameters to bind to the prepared statement. Can be
     *               empty if no parameters are needed.
     * @return A List of NotRecord objects containing the query results. Returns an
     *         empty list if an error occurs.
     * @throws SQLException If a database access error occurs (caught internally and
     *                      logged)
     */
    public List<NotRecord> processSelect(String sql, List<Object> params) {
        NotLib.dbg().log(NotDebugger.C_DATABASE, "[processSelect] SQL: " + sql);
        if (!params.isEmpty()) {
            NotLib.dbg().log(NotDebugger.C_DATABASE, "[processSelect] Params: " + params);
        }

        try (Connection connection = getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {

            if (params != null) {
                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i + 1, params.get(i));
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                List<NotRecord> resultList = new ArrayList<>();

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        String columnType = metaData.getColumnTypeName(i);
                        Object columnValue = rs.getObject(i);
                        row.put(columnName, convertValue(columnType, columnValue));
                    }

                    resultList.add(new NotRecord(row));
                }

                return resultList;
            }
        } catch (SQLException e) {
            getLogger().error("Error processing query: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
}