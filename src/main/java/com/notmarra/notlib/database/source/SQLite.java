package com.notmarra.notlib.database.source;

import com.notmarra.notlib.database.Database;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;

public class SQLite extends Database {
    private final String databasePath;
    
    public SQLite(String databasePath) {
        this.databasePath = databasePath;
    }
    
    @Override
    public void setup() {
        try {
            File databaseFile = new File(databasePath);
            if (!databaseFile.getParentFile().exists()) {
                databaseFile.getParentFile().mkdirs();
            }
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:sqlite:" + databasePath);
            config.setDriverClassName("org.sqlite.JDBC");
            
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            
            config.setMaximumPoolSize(10); 
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(10000);
            
            dataSource = new HikariDataSource(config);
            
        } catch (Exception e) {
            System.err.println("Error initializing SQLite database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
    