package com.notmarra.notlib.database.source;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import com.notmarra.notlib.NotLib;
import com.notmarra.notlib.database.NotDatabase;
import com.notmarra.notlib.database.structure.NotColumn;
import com.notmarra.notlib.database.structure.NotColumnType;
import com.notmarra.notlib.database.structure.NotTable;
import com.notmarra.notlib.extensions.NotPlugin;
import com.notmarra.notlib.utils.NotDebugger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * @class NotMySQL
 * @brief An abstract class for MySQL database implementation using HikariCP connection pooling.
 * 
 * This class extends NotDatabase and provides MySQL-specific functionality for database operations.
 * It handles MySQL connection configurations, table creation with appropriate column types,
 * and data insertion operations.
 * 
 * @note The class uses HikariCP for efficient connection pooling with optimized configurations.
 * 
 * @see NotDatabase
 */
public abstract class NotMySQL extends NotDatabase {
    public static final String ID = "MySQL";

    public NotMySQL(NotPlugin plugin, String defaultConfig) { super(plugin, defaultConfig); }

    @Override
    public String getId() { return ID; }

    /**
     * @brief Establishes a connection to a MySQL database using HikariCP.
     * 
     * This method reads database configuration from the provided configuration section
     * and sets up a HikariCP connection pool with optimized settings for MySQL.
     * It configures connection properties, statement caching, and pool size parameters
     * to ensure efficient database operations.
     * 
     * The connection uses the following properties from configuration:
     *   - host: Database server hostname
     *   - port: Database server port
     *   - database: Database name
     *   - username: Database username
     *   - password: Database password
     * 
     * @note The method initializes the 'source' field with a new HikariDataSource.
     */
    @Override
    public void connect() {
        ConfigurationSection configSection = getDatabaseConfig();
        String host = configSection.getString("host");
        String port = configSection.getString("port");
        String database = configSection.getString("database");
        String username = configSection.getString("username");
        String password = configSection.getString("password");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
        hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
        hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
        hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
        hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
        hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
        hikariConfig.addDataSourceProperty("maintainTimeStats", "false");
        
        hikariConfig.setMaximumPoolSize(20);
        hikariConfig.setMinimumIdle(5);
        hikariConfig.setIdleTimeout(30000);
        hikariConfig.setConnectionTimeout(10000);
        
        source = new HikariDataSource(hikariConfig);
    }

    /**
     * @brief Creates a new table in the MySQL database if it doesn't exist
     * 
     * This method generates and executes a SQL statement to create a table based on the provided
     * NotTable specification. The method handles various column types, constraints, and attributes
     * including:
     * - Column types with appropriate length/precision/scale parameters
     * - Primary key constraints (both at column level and table level)
     * - Auto increment columns
     * - NOT NULL constraints
     * - UNIQUE constraints
     * - DEFAULT values (both raw and quoted)
     * 
     * @param table The NotTable object containing the table schema definition
     * @return true if the table was created successfully, false otherwise
     * @see NotTable
     * @see NotColumn
     * @see NotColumnType
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
            if (column.isAutoIncrement()) sql.append(" AUTO_INCREMENT");
            if (column.isNotNull()) sql.append(" NOT NULL");
            if (column.isUnique()) sql.append(" UNIQUE");
            if (column.getRawDefaultValue() != null) sql.append(" DEFAULT ").append(column.getRawDefaultValue());
            if (column.getDefaultValue() != null) sql.append(" DEFAULT '").append(column.getDefaultValue()).append("'");
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
     * @brief Inserts a row into the specified table.
     *
     * Constructs an SQL INSERT statement based on the table structure and the provided row data.
     * Automatically skips auto-increment columns during SQL generation.
     * The method logs the generated SQL and parameter values if debugging is enabled.
     *
     * @param table The NotTable object representing the table to insert into
     * @param row A list of objects containing the values to be inserted in the same order as the table columns
     * @return true if the row was successfully inserted, false otherwise
     * @throws SQLException handled internally, prints stack trace and returns false
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