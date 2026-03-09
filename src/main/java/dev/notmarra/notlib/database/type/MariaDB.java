package dev.notmarra.notlib.database.type;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.notmarra.notlib.database.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

public class MariaDB extends Database {
    private static final Logger logger = Logger.getLogger(MariaDB.class.getName());

    @Override
    public void setup(Properties props) {
        props.setProperty("dataSourceClassName", "org.mariadb.jdbc.MariaDbDataSource");

        HikariConfig config = new HikariConfig(props);

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30_000);   // 30s
        config.setIdleTimeout(600_000);        // 10 min
        config.setMaxLifetime(1_800_000);      // 30 min

        config.setKeepaliveTime(60_000);

        ds = new HikariDataSource(config);

        try (Connection conn = ds.getConnection()) {
            logger.info("MySQL connection tested successfully.");
        } catch (SQLException e) {
            ds.close();
            throw new RuntimeException("Failed to connect to MySQL: " + e.getMessage(), e);
        }
    }
}
