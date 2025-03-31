package com.notmarra.notlib.database.source;

import com.notmarra.notlib.database.NotDatabase;
import com.notmarra.notlib.extensions.NotPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.ResultSet;

import org.bukkit.configuration.ConfigurationSection;

public abstract class NotSQLite extends NotDatabase {
    public static final String ID = "SQLite";

    public NotSQLite(NotPlugin plugin, String defaultConfig) { super(plugin, defaultConfig); }

    @Override
    public String getId() { return ID; }

    @Override
    public String getDriver() { return "org.sqlite.JDBC"; }

    @Override
    public String getDatabaseName() {
        try {
            ResultSet result = processPreparedResult("SELECT name FROM sqlite_master WHERE type='table' LIMIT 1");
            if (result.next()) return result.getString(1);
        } catch (Exception e) {
            throw new RuntimeException("Error getting database name: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void connect() {
        ConfigurationSection configSection = getDatabaseConfig();
        HikariConfig hikariConfig = createHikariConfig();
        
        String databasePath = configSection.getString("file");
        if (databasePath == null) {
            throw new IllegalArgumentException("Database file path is not specified in the configuration.");
        }

        File databaseFile = new File(plugin.getDataFolder(), databasePath);
        plugin.getLogger().info("Connecting to SQLite database at: " + databaseFile.getAbsolutePath());
        hikariConfig.setJdbcUrl("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        
        hikariConfig.setMaximumPoolSize(10); 
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setIdleTimeout(30000);
        hikariConfig.setConnectionTimeout(10000);

        dataSource = new HikariDataSource(hikariConfig);
    }
}