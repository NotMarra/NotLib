package com.notmarra.notlib.database;

import com.notmarra.notlib.database.structure.NotTable;
import com.notmarra.notlib.extensions.NotConfigurable;
import com.notmarra.notlib.extensions.NotPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public abstract String getDatabaseName();
    
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
            plugin.getLogger().severe("Error creating tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void process(Consumer<Connection> consumer) {
        try (Connection connection = getConnection()) {
            consumer.accept(connection);
        } catch (Exception e) {
            plugin.getLogger().severe("Error processing connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void processStatement(BiConsumer<Connection, Statement> consumer) {
        try (Connection connection = getConnection()) {
            consumer.accept(connection, connection.createStatement());
        } catch (Exception e) {
            plugin.getLogger().severe("Error processing statement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean processQuery(String sql) {
        try (Connection connection = getConnection()) {
            plugin.getLogger().info("Executing SQL: " + sql);
            return connection.createStatement().execute(sql);
        } catch (Exception e) {
            plugin.getLogger().severe("Error processing query: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public ResultSet processPreparedResult(String sql) {
        if (sql == null || sql.isEmpty()) return null;
        try (Connection connection = dataSource.getConnection()) {
            plugin.getLogger().info("Executing SQL: " + sql);
            return connection.prepareStatement(sql).executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ResultSet processPreparedResult(String sql, Object... params) {
        if (sql == null || sql.isEmpty() || params == null) return null;
        try (Connection connection = dataSource.getConnection()) {
            plugin.getLogger().info("Executing SQL: " + sql);
            PreparedStatement stmt = connection.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Map<String, String>> processPrepared(String sql, Object... params) {
        if (sql == null || sql.isEmpty() || params == null) return null;
        List<Map<String, String>> resultList = new ArrayList<>();

        ResultSet result = processPreparedResult(sql, params);
        if (result == null) return List.of();

        try {
            ResultSetMetaData metaData = result.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (result.next()) {
                Map<String, String> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String value = result.getString(i);
                    row.put(columnName, value);
                }
                resultList.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resultList.isEmpty() ? null : resultList;
    }

    public Map<String, String> processPreparedFirst(String sql, Object... params) {
        List<Map<String, String>> result = processPrepared(sql, params);
        if (result.isEmpty()) return null;
        return result.get(0);
    }

    public abstract List<NotTable> setup();
}