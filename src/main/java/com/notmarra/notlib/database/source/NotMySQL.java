package com.notmarra.notlib.database.source;

import org.bukkit.configuration.ConfigurationSection;

import com.notmarra.notlib.database.NotDatabase;
import com.notmarra.notlib.extensions.NotPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class NotMySQL extends NotDatabase {
    public static final String ID = "MySQL";

    public NotMySQL(NotPlugin plugin, String defaultConfig) { super(plugin, defaultConfig); }

    @Override
    public String getId() { return ID; }

    @Override
    public String getDriver() { return "com.mysql.cj.jdbc.Driver"; }

    @Override
    public void connect() {
        ConfigurationSection configSection = getDatabaseConfig();

        HikariConfig hikariConfig = createHikariConfig();

        String host = configSection.getString("host");
        String port = configSection.getString("port");
        String database = configSection.getString("name");
        String username = configSection.getString("username");
        String password = configSection.getString("password");

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
        
        dataSource = new HikariDataSource(hikariConfig);
    }

    @Override
    public void setup() {
        String table = getDatabaseConfig().getString("table");
        if (table == null) {
            plugin.getLogger().warning("Table name not specified in the configuration.");
            return;
        }

        processQuery("CREATE TABLE IF NOT EXISTS " + table + " (uuid VARCHAR(36) PRIMARY KEY, player_name VARCHAR(36), balance DOUBLE)");
    }
}