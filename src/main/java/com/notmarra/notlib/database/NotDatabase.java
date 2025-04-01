package com.notmarra.notlib.database;

import com.notmarra.notlib.database.structure.NotTable;
import com.notmarra.notlib.extensions.NotConfigurable;
import com.notmarra.notlib.extensions.NotPlugin;
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
    protected HikariDataSource source;
    private Map<String, NotTable> tables = new HashMap<>();

    public NotDatabase(NotPlugin plugin, String defaultConfig) {
        super(plugin);
        this.defaultConfig = defaultConfig;
    }

    @Override
    public NotConfigurable registerConfigurable() {
        super.registerConfigurable();
        registerTables();
        return this;
    }

    @Override
    public List<String> getConfigPaths() { return List.of(defaultConfig); }
    
    public abstract String getId();
    
    public abstract List<NotTable> setupTables();

    public abstract void connect();

    public abstract void createTable(NotTable table);

    public abstract void insertRow(NotTable table, List<Object> row);

    public boolean tableExists(NotTable table) { return tableExists(table.getName()); }

    public abstract boolean tableExists(String tableName);

    private void registerTables() {
        for (NotTable table : setupTables()) {
            tables.put(table.getName(), table);
        }
    }

    public Map<String, NotTable> getTables() { return tables; }
    public NotTable getTable(String tableName) { return tables.get(tableName); }

    public void setup() {
        for (NotTable table : setupTables()) {
            createTable(table);

            for (List<Object> row : table.getInsertList()) {
                insertRow(table, row);
            }
        }
    }
    
    @Override
    public void onConfigReload(List<String> reloadedConfigs) {
        close();
        connect();
    }

    public ConfigurationSection getDatabaseConfig() {
        FileConfiguration config = getConfig(getConfigPaths().get(0));
        ConfigurationSection section = config.getConfigurationSection("database." + getId());
        if (section == null) return new YamlConfiguration();
        return section;
    }
    
    public void setSource(HikariDataSource source) { this.source = source; }
    
    public Connection getConnection() throws SQLException {
        if (source == null || source.isClosed()) {
            throw new SQLException("DataSource is not initialized or closed");
        }
        return source.getConnection();
    }

    public void close() {
        if (source != null && !source.isClosed()) source.close();
    }

    public boolean isConnected() {
        return source != null && !source.isClosed();
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
        try (Connection connection = source.getConnection()) {
            plugin.getLogger().info("Executing SQL: " + sql);
            return connection.prepareStatement(sql).executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ResultSet processPreparedResult(String sql, Object... params) {
        if (sql == null || sql.isEmpty() || params == null) return null;
        try (Connection connection = source.getConnection()) {
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

}