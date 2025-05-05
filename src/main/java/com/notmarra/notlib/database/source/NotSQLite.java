package com.notmarra.notlib.database.source;

import com.notmarra.notlib.NotLib;
import com.notmarra.notlib.database.NotDatabase;
import com.notmarra.notlib.database.structure.NotColumn;
import com.notmarra.notlib.database.structure.NotColumnType;
import com.notmarra.notlib.database.structure.NotTable;
import com.notmarra.notlib.extensions.NotPlugin;
import com.notmarra.notlib.utils.NotDebugger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

/**
 * @class NotSQLite
 * @brief Abstract SQLite implementation of the NotDatabase class.
 *
 * NotSQLite provides SQLite database functionality using HikariCP connection pooling.
 * This class handles the specifics of connecting to an SQLite database file and
 * creating SQL statements compatible with SQLite syntax, such as table creation
 * and row insertion.
 *
 * @extends NotDatabase
 * 
 * @details
 * The class establishes a connection to an SQLite database specified in the plugin configuration.
 * It configures HikariCP connection pool with SQLite-appropriate settings and provides
 * methods for database operations like table creation and data insertion.
 *
 * SQLite-specific features:
 * - Uses file-based database storage
 * - Implements AUTOINCREMENT instead of standard AUTO_INCREMENT
 * - Creates SQLite-compatible table schemas
 *
 * @note When working with SQLite, be aware of its limitations compared to other RDBMS,
 * including limited concurrent access and transaction isolation.
 */
public abstract class NotSQLite extends NotDatabase {
    public static final String ID = "SQLite";

    public NotSQLite(NotPlugin plugin, String defaultConfig) { super(plugin, defaultConfig); }

    @Override
    public String getId() { return ID; }

    /**
     * @brief Establishes a connection to the SQLite database using HikariCP.
     * 
     * This method configures and initializes a connection to the SQLite database specified in the
     * plugin's configuration. It uses HikariCP for connection pooling with optimized settings.
     * 
     * The method performs the following steps:
     * 1. Retrieves the database configuration
     * 2. Gets the database file path from configuration
     * 3. Configures HikariCP with SQLite-specific settings
     * 4. Sets connection pool properties (size, timeout, etc.)
     * 5. Creates and initializes the HikariDataSource
     * 
     * @throws IllegalArgumentException if the database file path is not specified in the configuration
     */
    @Override
    public void connect() {
        ConfigurationSection configSection = getDatabaseConfig();

        String databasePath = configSection.getString("file");
        if (databasePath == null) {
            throw new IllegalArgumentException("Database file path is not specified in the configuration.");
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("org.sqlite.JDBC");

        File databaseFile = new File(plugin.getDataFolder(), databasePath);
        hikariConfig.setJdbcUrl("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        // hikariConfig.setDataSourceClassName("org.sqlite.SQLiteDataSource");
        hikariConfig.setPoolName(ID);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        
        hikariConfig.setMaximumPoolSize(10); 
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setIdleTimeout(30000);
        hikariConfig.setConnectionTimeout(10000);

        source = new HikariDataSource(hikariConfig);
    }

    /**
     * @brief Creates a new table in the SQLite database if it doesn't exist
     * 
     * This method constructs an SQL CREATE TABLE statement based on the provided NotTable
     * object and executes it against the SQLite database. The statement includes column 
     * definitions with appropriate data types, constraints (PRIMARY KEY, NOT NULL, UNIQUE),
     * and support for autoincrement. For certain data types, it also handles length and
     * precision specifications.
     * 
     * @param table A NotTable object containing the table definition including name, columns,
     *              and primary key information
     * @return boolean True if the operation was successful, false otherwise
     * 
     * @note For VARCHAR, CHAR, BIT, BINARY, and VARBINARY types, length specification is applied
     * @note For DECIMAL type, both precision and scale are specified
     * @note Composite primary keys are supported through the table's primaryKeys list
     */
    @Override
    public boolean createTable(NotTable table) {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + table.getName() + " (");
        for (int i = 0; i < table.getColumns().size(); i++) {
            NotColumn column = table.getColumns().get(i);
            NotColumnType type = column.getType();
            sql.append(column.getName()).append(" ").append(type.getSqlType());
            
            if (type == NotColumnType.VARCHAR || type == NotColumnType.CHAR || 
                type == NotColumnType.BIT || type == NotColumnType.BINARY || 
                type == NotColumnType.VARBINARY) {
                sql.append("(").append(column.getLength()).append(")");
            } else if (type == NotColumnType.DECIMAL) {
                sql.append("(").append(column.getPrecision()).append(",").append(column.getScale()).append(")");
            }

            if (column.isPrimaryKey()) sql.append(" PRIMARY KEY");
            if (column.isAutoIncrement()) sql.append(" AUTOINCREMENT");
            if (column.isNotNull()) sql.append(" NOT NULL");
            if (column.isUnique()) sql.append(" UNIQUE");
            if (i < table.getColumns().size() - 1) sql.append(", ");
        }

        List<String> primaryKeys = table.getPrimaryKeys();
        if (!primaryKeys.isEmpty()) {
            sql.append(", PRIMARY KEY (");
            for (int i = 0; i < primaryKeys.size(); i++) {
                String primaryKey = primaryKeys.get(i);
                sql.append(primaryKey);
                if (i < primaryKeys.size() - 1) sql.append(", ");
            }
            sql.append(")");
        }

        sql.append(");");

        return processResult(sql.toString());
    }

    /**
     * @brief Inserts a new row into the specified database table.
     * 
     * This method generates and executes an SQL INSERT statement for the given table and row data.
     * It automatically handles auto-increment columns by excluding them from the INSERT statement.
     * The method logs both the generated SQL query and its parameters for debugging purposes.
     *
     * @param table The database table where the row should be inserted
     * @param row A list containing the values to be inserted, in the same order as the table columns
     *            (excluding auto-increment columns)
     * @return true if the row was successfully inserted, false otherwise
     * @throws SQLException internally handled, logged to the plugin's logger
     */
    @Override
    public boolean insertRow(NotTable table, List<Object> row) {
        StringBuilder sql = new StringBuilder("INSERT INTO " + table.getName() + " (");
        for (int i = 0; i < table.getColumns().size(); i++) {
            NotColumn column = table.getColumns().get(i);
            if (!column.isAutoIncrement()) {
                sql.append(column.getName());
                if (i < table.getColumns().size() - 1) sql.append(", ");
            }
        }
        sql.append(") VALUES (");
        for (int i = 0; i < row.size(); i++) {
            sql.append("?");
            if (i < row.size() - 1) sql.append(", ");
        }
        sql.append(")");

        NotLib.dbg().log(NotDebugger.C_DATABASE, "[MySQL insertRow] SQL: " + sql.toString());
        if (!row.isEmpty()) {
            NotLib.dbg().log(NotDebugger.C_DATABASE, "[MySQL insertRow] Params: " + row);
        }
        
        try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < row.size(); i++) {
                stmt.setObject(i + 1, row.get(i));
            }
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("Error inserting row: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}