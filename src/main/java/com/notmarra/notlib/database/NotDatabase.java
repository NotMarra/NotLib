package com.notmarra.notlib.database;

import com.notmarra.notlib.extensions.NotConfigurable;
import com.notmarra.notlib.extensions.NotPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public abstract class NotDatabase extends NotConfigurable {
    private final String defaultConfig;
    protected HikariDataSource dataSource;

    public NotDatabase(NotPlugin plugin, String defaultConfig) {
        super(plugin);
        this.defaultConfig = defaultConfig;
    }

    @Override
    public List<String> getConfigPaths() { return List.of(defaultConfig); }

    public abstract String getId();

    public abstract String getDriver();

    @Override
    public void onConfigReload(List<String> reloadedConfigs) {
        close();
        connect();
    }

    public HikariConfig createHikariConfig() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(getDriver());
        return hikariConfig;
    }

    public ConfigurationSection getDatabaseConfig() {
        FileConfiguration config = getConfig(getConfigPaths().get(0));
        ConfigurationSection section = config.getConfigurationSection("database." + getId());
        if (section == null) return new YamlConfiguration();
        return section;
    }
    
    /**
     * Initialization of the database and setting up the connection pool
     */
    public abstract void connect();
    
    /**
     * Obtaining a connection from the connection pool
     * @return Connection to the database
     * @throws SQLException If an error occurs while obtaining the connection
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("DataSource is not initialized or closed");
        }
        return dataSource.getConnection();
    }

    public void setSource(HikariDataSource dataSource) { this.dataSource = dataSource; }
    
    /**
     * Closing the connection pool
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
    
    /**
     * Check if the connection is active
     * @return true if the connection is active, otherwise false
     */
    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }

    /**
     * Creates tables and prepares the database for use
     * @param sql SQL query for creating tables
     */
    public void createTables(String sql) {
        try (Statement statement = dataSource.getConnection().createStatement()) {
            statement.execute(sql);
        } catch (Exception e) {
            System.err.println("Error creating tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void process(Consumer<Connection> consumer) {
        try (Connection connection = getConnection()) {
            consumer.accept(connection);
        } catch (Exception e) {
            System.err.println("Error processing connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void processStatement(BiConsumer<Connection, Statement> consumer) {
        try (Connection connection = getConnection()) {
            consumer.accept(connection, connection.createStatement());
        } catch (Exception e) {
            System.err.println("Error processing statement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean processQuery(String query) {
        try (Connection connection = getConnection()) {
            return connection.createStatement().execute(query);
        } catch (Exception e) {
            System.err.println("Error processing query: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public abstract void setup();
}