package com.notmarra.notlib.database.source;

import com.notmarra.notlib.database.NotDatabase;
import com.notmarra.notlib.database.structure.NotColumn;
import com.notmarra.notlib.database.structure.NotTable;
import com.notmarra.notlib.extensions.NotPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.ResultSet;
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
        hikariConfig.setDataSourceClassName("org.sqlite.SQLiteDataSource");
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
    public boolean tableExists(String tableName) {
        try {
            ResultSet result = processPreparedResult("SELECT name FROM sqlite_master WHERE type='table' AND name=?", tableName);
            if (result.next()) return result.getString("name").equalsIgnoreCase(tableName);
        } catch (Exception e) {
            plugin.getLogger().severe("Error checking if table exists: " + e.getMessage());
        }
        return false;
    }

    @Override
    public void createTable(NotTable table) {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + table.getName() + " (");
        for (int i = 0; i < table.getColumns().size(); i++) {
            NotColumn column = table.getColumns().get(i);
            sql.append(column.getName()).append(" ").append(column.getType().getSqlType());
            if (column.isPrimaryKey()) sql.append(" PRIMARY KEY");
            if (column.isAutoIncrement()) sql.append(" AUTOINCREMENT");
            if (column.isNotNull()) sql.append(" NOT NULL");
            if (column.isUnique()) sql.append(" UNIQUE");
            if (i < table.getColumns().size() - 1) sql.append(", ");
        }
        sql.append(");");

        processQuery(sql.toString());
    }

    public void insertRow(NotTable table, List<Object> row) {
        StringBuilder sql = new StringBuilder("INSERT INTO " + table.getName() + " (");
        for (int i = 0; i < table.getColumns().size(); i++) {
            NotColumn column = table.getColumns().get(i);
            sql.append(column.getName());
            if (i < table.getColumns().size() - 1) sql.append(", ");
        }
        sql.append(") VALUES (");
        for (int i = 0; i < row.size(); i++) {
            sql.append("'").append(row.get(i)).append("'");
            if (i < row.size() - 1) sql.append(", ");
        }
        sql.append(");");

        processQuery(sql.toString());
    }
}