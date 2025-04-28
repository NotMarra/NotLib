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

public abstract class NotSQLite extends NotDatabase {
    public static final String ID = "SQLite";

    public NotSQLite(NotPlugin plugin, String defaultConfig) { super(plugin, defaultConfig); }

    @Override
    public String getId() { return ID; }

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