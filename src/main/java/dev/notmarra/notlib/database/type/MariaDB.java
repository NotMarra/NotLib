package dev.notmarra.notlib.database.type;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.notmarra.notlib.database.Database;
import dev.notmarra.notlib.database.DbDialect;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

public class MariaDB extends Database {
    private static final Logger logger = Logger.getLogger(MariaDB.class.getName());

    @Override
    public MariaDB setup(Properties props) {
        String user = props.getProperty("dataSource.user");
        String password = props.getProperty("dataSource.password");
        String databaseName = props.getProperty("dataSource.databaseName");
        String port = props.getProperty("dataSource.port");
        String serverName = props.getProperty("dataSource.serverName");
        String url = "jdbc:mariadb://" + serverName + ":" + port + "/" + databaseName;


        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);

        config.setDriverClassName("org.mariadb.jdbc.Driver");

        if (user != null) config.setUsername(user);
        if (password != null) config.setPassword(password);

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30_000);
        config.setIdleTimeout(600_000);
        config.setMaxLifetime(1_800_000);
        config.setKeepaliveTime(60_000);

        ds = new HikariDataSource(config);

        try (Connection conn = ds.getConnection()) {
            logger.info("MariaDB connection tested successfully: " + url);
        } catch (SQLException e) {
            ds.close();
            throw new RuntimeException("Failed to connect to MariaDB: " + e.getMessage(), e);
        }

        return this;
    }

    @Override
    public DbDialect getDialect() { return DbDialect.MARIADB; }
}
