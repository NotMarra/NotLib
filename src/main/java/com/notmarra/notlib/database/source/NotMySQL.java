package com.notmarra.notlib.database.source;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import com.notmarra.notlib.database.NotDatabase;
import com.notmarra.notlib.database.structure.NotColumn;
import com.notmarra.notlib.database.structure.NotColumnType;
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
            if (column.getDefaultValue() != null) sql.append(" DEFAULT '").append(column.getDefaultValue()).append("'");
            if (i < table.getColumns().size() - 1) sql.append(", ");
        }
        sql.append(");");

        return processQuery(sql.toString());
    }

    @Override
    public boolean insertRow(NotTable table, List<Object> row) {
        StringBuilder sql = new StringBuilder("INSERT INTO " + table.getName() + " (");
        for (int i = 0; i < table.getColumns().size(); i++) {
            NotColumn column = table.getColumns().get(i);
            sql.append(column.getName());
            if (i < table.getColumns().size() - 1) sql.append(", ");
        }
        sql.append(") VALUES (");
        for (int i = 0; i < row.size(); i++) {
            sql.append("?");
            if (i < row.size() - 1) sql.append(", ");
        }
        sql.append(")");
        
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