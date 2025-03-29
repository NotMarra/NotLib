package com.notmarra.notlib.database.source;

import com.notmarra.notlib.database.NotDatabase;
import com.notmarra.notlib.extensions.NotPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;

import org.bukkit.configuration.ConfigurationSection;

public abstract class NotSQLite extends NotDatabase {
    public static final String ID = "SQLite";

    public NotSQLite(NotPlugin plugin, String defaultConfig) { super(plugin, defaultConfig); }

    @Override
    public String getId() { return ID; }

    @Override
    public String getDriver() { return "org.sqlite.JDBC"; }

    @Override
    public void connect() {
        ConfigurationSection configSection = getDatabaseConfig();
        HikariConfig hikariConfig = createHikariConfig();
        
        String databasePath = configSection.getString("file");

        File databaseFile = new File(databasePath);
        if (!databaseFile.getParentFile().exists()) {
            databaseFile.getParentFile().mkdirs();
        }
        
        hikariConfig.setJdbcUrl("jdbc:sqlite:" + databasePath);
        
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