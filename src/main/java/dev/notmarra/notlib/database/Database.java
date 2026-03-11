package dev.notmarra.notlib.database;

import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

public abstract class Database {
    protected HikariDataSource ds;
    private static final Logger logger = Logger.getLogger(Database.class.getName());

    public abstract Database setup(Properties props);
    public abstract DbDialect getDialect();

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public void withConnection(ThrowingConsumer<Connection> action) {
        try (Connection conn = ds.getConnection()) {
            action.accept(conn);
        } catch (Exception e) {
            logger.severe("DB error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void close() {
        if (ds != null && !ds.isClosed()) {
            ds.close();
            logger.info("Database connection pool closed.");
        }
    }

    public boolean isConnected() {
        return ds != null && !ds.isClosed();
    }

    public static Properties generateProperties(
            String user, String password,
            String databaseName, String port, String serverName) {

        if (user == null || user.isBlank()) throw new IllegalArgumentException("User cannot be empty");
        if (password == null) throw new IllegalArgumentException("Password must not be null");
        if (databaseName == null || databaseName.isBlank()) throw new IllegalArgumentException("DatabaseName must not be empty");
        if (port == null || port.isBlank()) throw new IllegalArgumentException("Port must not be empty");
        if (serverName == null || serverName.isBlank()) throw new IllegalArgumentException("ServerName must not be empty");

        Properties props = new Properties();
        props.setProperty("dataSource.user", user);
        props.setProperty("dataSource.password", password);
        props.setProperty("dataSource.databaseName", databaseName);
        props.setProperty("dataSource.port", port);
        props.setProperty("dataSource.serverName", serverName);
        return props;
    }

    public static Properties generateProperties(File dataFolder, String fileName) {
        if (dataFolder == null) throw new IllegalArgumentException("DataFolder must not be empty");
        if (fileName == null || fileName.isBlank()) throw new IllegalArgumentException("FileName must not be empty");

        if (!fileName.endsWith(".db")) fileName += ".db";

        Properties props = new Properties();
        props.setProperty("dataSource.filePath", new File(dataFolder, fileName).getAbsolutePath());
        return props;
    }
}
