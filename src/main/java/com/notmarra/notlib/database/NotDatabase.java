package com.notmarra.notlib.database;

import com.notmarra.notlib.NotLib;
import com.notmarra.notlib.database.query.NotSqlBuilder;
import com.notmarra.notlib.database.query.NotSqlQueryExecutor;
import com.notmarra.notlib.database.structure.NotTable;
import com.notmarra.notlib.extensions.NotConfigurable;
import com.notmarra.notlib.extensions.NotPlugin;
import com.notmarra.notlib.utils.NotDebugger;
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
    private NotSqlQueryExecutor queryExecutor;
    private Map<String, NotTable> tables = new HashMap<>();

    public NotDatabase(NotPlugin plugin, String defaultConfig) {
        super(plugin);
        this.defaultConfig = defaultConfig;
        this.queryExecutor = new NotSqlQueryExecutor(this);
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

    public abstract boolean createTable(NotTable table);

    public abstract void insertRow(NotTable table, List<Object> row);

    // sql builder
    public NotSqlBuilder select(String table) { return NotSqlBuilder.select(table); }
    public NotSqlBuilder select(String table, List<String> columns) { return NotSqlBuilder.select(table, columns); }
    public NotSqlBuilder insertInto(String table) { return NotSqlBuilder.insertInto(table); }
    public NotSqlBuilder update(String table) { return NotSqlBuilder.update(table); }
    public NotSqlBuilder deleteFrom(String table) { return NotSqlBuilder.deleteFrom(table); }
    public boolean exists(NotSqlBuilder builder) { return queryExecutor.exists(builder); }
    public List<Map<String, Object>> query(NotSqlBuilder builder) { return queryExecutor.executeQuery(builder); }
    public int execute(NotSqlBuilder builder) { return queryExecutor.executeUpdate(builder); }
    public List<Map<String, Object>> executeQuery(NotSqlBuilder builder) { return queryExecutor.executeQuery(builder); }
    public Map<String, Object> fetchOne(NotSqlBuilder builder) { return queryExecutor.fetchOne(builder); }

    public NotSqlQueryExecutor getQueryExecutor() {
        return queryExecutor;
    }

    private void registerTables() {
        for (NotTable table : setupTables()) {
            tables.put(table.getName(), table);
        }
    }

    public Map<String, NotTable> getTables() { return tables; }
    public NotTable getTable(String tableName) { return tables.get(tableName); }

    public void setup() {
        for (NotTable table : setupTables()) {
            if (createTable(table)) {
                for (List<Object> row : table.getInsertList()) {
                    insertRow(table, row);
                }
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

    public Object convertValue(String columnType, Object value) {
        switch (columnType) {
            case "TEXT":
                return String.valueOf(value);
            case "INTEGER":
                return Integer.valueOf(String.valueOf(value));
            case "REAL":
                return Double.valueOf(String.valueOf(value));
            case "DOUBLE":
                return Double.valueOf(String.valueOf(value));
            case "FLOAT":
                return Float.valueOf(String.valueOf(value));
            default:
                throw new IllegalArgumentException("Unsupported column type: " + columnType);
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

    public List<Map<String, Object>> processResult(String sql) { return processResult(sql, null); }

    public List<Map<String, Object>> processResult(String sql, List<Object> params) {
        if (sql == null || sql.isEmpty()) return null;
        

        if (NotDebugger.should(NotLib.DEBUG_DB)) {
            getLogger().info("Executing SQL: " + sql);
        }

        try (Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            if (params != null) {
                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i, params.get(i));
                }
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                List<Map<String, Object>> resultList = new ArrayList<>();
                
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        String columnType = metaData.getColumnTypeName(i);
                        Object columnValue = rs.getObject(i);
                        row.put(columnName, convertValue(columnType, columnValue));
                    }
                    resultList.add(row);
                }
                
                return resultList;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error processing query: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
}