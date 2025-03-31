package com.notmarra.notlib.database.structure;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.notmarra.notlib.database.NotDatabase;
import com.notmarra.notlib.database.source.NotMySQL;
import com.notmarra.notlib.database.source.NotSQLite;

public class NotTable {
    private final String name;
    private List<NotColumn> columns = new ArrayList<>();
    private List<List<Object>> initialInsert = new ArrayList<>();

    public NotTable(String name) { this.name = name; }

    public NotTable addColumn(NotColumn column) { columns.add(column); return this; }

    public NotTable addColumns(List<NotColumn> columns) { this.columns.addAll(columns); return this; }

    public NotTable insert(List<Object> data) { initialInsert.add(data); return this; }
    public NotTable insertList(List<List<Object>> data) { initialInsert.addAll(data); return this; }
    public List<List<Object>> getInsertList() { return initialInsert; }

    public static NotTable create(String name) { return new NotTable(name); }
    
    public static NotTable create(String name, List<NotColumn> columns) { return (new NotTable(name)).addColumns(columns); }

    // Connection

    public boolean exists(NotDatabase database) {
        ResultSet result = database.processPreparedResult(buildExists(database));
        try {
            if (result.next()) return result.getInt(1) > 0;
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Error checking if table exists: " + e.getMessage(), e);
        }
    }

    public void create(NotDatabase database) {
        database.processQuery(buildCreate(database));
    }

    public void insert(NotDatabase database, List<Object> data) {
        database.processQuery(buildInsert(database, data));
    }

    // MySQL 

    public String buildMySQLExists(NotDatabase database) {
        return "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '" + database.getDatabaseName() + "' AND table_name = '" + name + "';";
    }

    public String buildMySQLCreateTable() {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + name + " (");
        for (int i = 0; i < columns.size(); i++) {
            NotColumn column = columns.get(i);
            sql.append(column.getName()).append(" ").append(column.getType().getSqlType());
            if (column.isPrimaryKey()) sql.append(" PRIMARY KEY");
            if (column.isAutoIncrement()) sql.append(" AUTO_INCREMENT");
            if (column.isNotNull()) sql.append(" NOT NULL");
            if (column.isUnique()) sql.append(" UNIQUE");
            if (i < columns.size() - 1) sql.append(", ");
        }
        sql.append(");");
        return sql.toString();
    }

    public String buildMySQLInsert(List<Object> data) {
        StringBuilder sql = new StringBuilder("INSERT INTO " + name + " (");
        for (int i = 0; i < columns.size(); i++) {
            NotColumn column = columns.get(i);
            sql.append(column.getName());
            if (i < columns.size() - 1) sql.append(", ");
        }
        sql.append(") VALUES (");
        for (int i = 0; i < data.size(); i++) {
            sql.append("'").append(data.get(i)).append("'");
            if (i < data.size() - 1) sql.append(", ");
        }
        sql.append(");");
        return sql.toString();
    }

    // SQLite

    public String buildSQLiteExists(NotDatabase database) {
        return "SELECT name FROM sqlite_master WHERE type='table' AND name='" + name + "';";
    }

    public String buildSQLiteCreateTable() {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + name + " (");
        for (int i = 0; i < columns.size(); i++) {
            NotColumn column = columns.get(i);
            sql.append(column.getName()).append(" ").append(column.getType().getSqlType());
            if (column.isPrimaryKey()) sql.append(" PRIMARY KEY");
            if (column.isAutoIncrement()) sql.append(" AUTOINCREMENT");
            if (column.isNotNull()) sql.append(" NOT NULL");
            if (column.isUnique()) sql.append(" UNIQUE");
            if (i < columns.size() - 1) sql.append(", ");
        }
        sql.append(");");
        return sql.toString();
    }

    public String buildSQLiteInsert(List<Object> data) {
        StringBuilder sql = new StringBuilder("INSERT INTO " + name + " (");
        for (int i = 0; i < columns.size(); i++) {
            NotColumn column = columns.get(i);
            sql.append(column.getName());
            if (i < columns.size() - 1) sql.append(", ");
        }
        sql.append(") VALUES (");
        for (int i = 0; i < data.size(); i++) {
            sql.append("'").append(data.get(i)).append("'");
            if (i < data.size() - 1) sql.append(", ");
        }
        sql.append(");");
        return sql.toString();
    }

    public String buildExists(NotDatabase database) {
        if (database instanceof NotMySQL) return buildMySQLExists(database);
        if (database instanceof NotSQLite) return buildSQLiteExists(database);
        throw new IllegalArgumentException("Unsupported database type: " + database.getClass().getSimpleName());
    }

    public String buildCreate(NotDatabase database) {
        if (database instanceof NotMySQL) return buildMySQLCreateTable();
        if (database instanceof NotSQLite) return buildSQLiteCreateTable();
        throw new IllegalArgumentException("Unsupported database type: " + database.getClass().getSimpleName());
    }

    public String buildInsert(NotDatabase database, List<Object> data) {
        if (database instanceof NotMySQL) return buildMySQLInsert(data);
        if (database instanceof NotSQLite) return buildSQLiteInsert(data);
        throw new IllegalArgumentException("Unsupported database type: " + database.getClass().getSimpleName());
    }
}