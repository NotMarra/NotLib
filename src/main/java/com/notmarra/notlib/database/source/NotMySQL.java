package com.notmarra.notlib.database.source;

import java.sql.ResultSet;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import com.notmarra.notlib.database.NotDatabase;
import com.notmarra.notlib.database.structure.NotColumn;
import com.notmarra.notlib.database.structure.NotTable;
import com.notmarra.notlib.extensions.NotPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public abstract class NotMySQL extends NotDatabase {
    public static final String ID = "MySQL";

    public NotMySQL(NotPlugin plugin, String defaultConfig) { super(plugin, defaultConfig); }

    @Override
    public String getId() { return ID; }

    @Override
    public void connect() {
        ConfigurationSection configSection = getDatabaseConfig();
        String host = configSection.getString("host");
        String port = configSection.getString("port");
        String database = configSection.getString("name");
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

    @Override
    public boolean tableExists(String tableName) {
        try {
            ResultSet result = processPreparedResult("SHOW TABLES LIKE '?'", tableName);
            if (result.next()) return result.getString(1).equalsIgnoreCase(tableName);
        } catch (Exception e) {
            getLogger().info("Error checking if table exists: " + e.getMessage());
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
            if (column.isAutoIncrement()) sql.append(" AUTO_INCREMENT");
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