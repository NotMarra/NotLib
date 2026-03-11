package dev.notmarra.notlib.database.type;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.notmarra.notlib.database.Database;
import dev.notmarra.notlib.database.DbDialect;
import org.sqlite.SQLiteDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

public class SQLite extends Database {
    private static final Logger logger = Logger.getLogger(SQLite.class.getName());

    @Override
    public SQLite setup(Properties props) {
        String filePath = props.getProperty("dataSource.filePath");

        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("dataSource.filePath must not be empty");
        }

        File dbFile = new File(filePath);
        if (dbFile.getParentFile() != null) {
            dbFile.getParentFile().mkdirs();
        }

        HikariConfig config = new HikariConfig();
        SQLiteDataSource sqliteDs = new SQLiteDataSource();
        sqliteDs.setUrl("jdbc:sqlite:" + filePath);
        config.setDataSource(sqliteDs);

        config.setMaximumPoolSize(1);
        config.setMinimumIdle(1);

        config.addDataSourceProperty("foreign_keys", "true");
        config.addDataSourceProperty("journal_mode", "WAL");
        config.addDataSourceProperty("synchronous", "NORMAL");

        config.setConnectionTimeout(30_000);
        config.setIdleTimeout(600_000);
        config.setMaxLifetime(1_800_000);

        ds = new HikariDataSource(config);

        try (Connection conn = ds.getConnection()) {
            logger.info("SQLite connection successfully tested: " + filePath);
        } catch (SQLException e) {
            ds.close();
            throw new RuntimeException("Failed to connect to SQLite: " + e.getMessage(), e);
        }
        return this;
    }

    @Override
    public DbDialect getDialect() { return DbDialect.SQLITE; }
}
