package com.notmarra.notlib.database.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.notmarra.notlib.database.NotDatabase;
import com.notmarra.notlib.database.query.NotSqlBuilder;
import com.notmarra.notlib.database.query.NotSqlQueryExecutor;

public class NotTable {
    private final String name;
    private List<NotColumn> columns = new ArrayList<>();
    private List<List<Object>> initialInsert = new ArrayList<>();
    private List<String> primaryKeys = new ArrayList<>();
    private NotDatabase dbCtx;

    public NotTable(String name) { this.name = name; }

    public NotDatabase db() { return dbCtx; }
    public NotTable setDbCtx(NotDatabase dbCtx) { this.dbCtx = dbCtx; return this; }

    public String getName() { return name; }
    public NotTable addColumn(NotColumn column) { columns.add(column); return this; }
    public NotTable addColumns(List<NotColumn> columns) { this.columns.addAll(columns); return this; }
    public NotTable addPrimaryKeys(List<String> primaryKeys) { this.primaryKeys = primaryKeys; return this; }
    public List<NotColumn> getColumns() { return columns; }
    public List<String> getPrimaryKeys() { return primaryKeys; }

    public NotTable initialInsert(List<Object> data) { initialInsert.add(data); return this; }
    public NotTable initialInsertList(List<List<Object>> data) { initialInsert.addAll(data); return this; }
    public List<List<Object>> getInsertList() { return initialInsert; }

    public static NotTable createNew(String name) { return new NotTable(name); }
    public static NotTable createNew(String name, List<NotColumn> columns) { return (new NotTable(name)).addColumns(columns); }

    // db

    private void checkDbCtx() {
        if (dbCtx == null) {
            throw new IllegalStateException("Database context is not set for table: " + name);
        }
    }

    public boolean createDb() {
        checkDbCtx();
        if (!dbCtx.isConnected()) return false;
        if (dbCtx.createTable(this)) {
            for (List<Object> row : initialInsert) {
                dbCtx.insertRow(this, row);
            }
            return true;
        }
        return false;
    }

    public NotSqlQueryExecutor executor() { 
        checkDbCtx();
        return dbCtx.getQueryExecutor();
    }

    // select, update, insert, delete

    private NotSqlBuilder _useSqlBuilder(Consumer<NotSqlBuilder> consumer, NotSqlBuilder builder) {
        checkDbCtx();
        consumer.accept(builder);
        return builder;
    }

    public NotRecord selectOne(Consumer<NotSqlBuilder> builder) {
        return executor().selectOne(_useSqlBuilder(builder, NotSqlBuilder.select(getName())));
    }

    public List<NotRecord> select() {
        return executor().select(NotSqlBuilder.select(getName()));
    }

    public List<NotRecord> select(Consumer<NotSqlBuilder> builder) {
        return executor().select(_useSqlBuilder(builder, NotSqlBuilder.select(getName())));
    }

    // NOTE: checks if select returns any row
    public boolean exists(Consumer<NotSqlBuilder> builder) {
        return executor().exists(_useSqlBuilder(builder, NotSqlBuilder.select(getName())));
    }

    // NOTE: executes delete and returns number of affected rows
    public int delete(Consumer<NotSqlBuilder> builder) {
        return executor().update(_useSqlBuilder(builder, NotSqlBuilder.deleteFrom(getName())));
    }

    // NOTE: executes delete and returns true if succeeded (> -1 affected rows)
    public boolean deleteSucceded(Consumer<NotSqlBuilder> builder) {
        return executor().succeeded(_useSqlBuilder(builder, NotSqlBuilder.deleteFrom(getName())));
    }

    // NOTE: executes update and returns number of affected rows
    public int update(Consumer<NotSqlBuilder> builder) {
        return executor().update(_useSqlBuilder(builder, NotSqlBuilder.update(getName())));
    }

    // NOTE: executes update and returns true if succeeded (> -1 affected rows)
    public boolean updateSucceded(Consumer<NotSqlBuilder> builder) {
        return executor().succeeded(_useSqlBuilder(builder, NotSqlBuilder.update(getName())));
    }

    public boolean insertRow(List<Object> row) {
        checkDbCtx();
        return dbCtx.insertRow(this, row);
    }

    public int insertRows(List<List<Object>> rows) {
        checkDbCtx();
        return rows.stream().mapToInt(o -> dbCtx.insertRow(this, o) ? 1 : 0).sum();
    }
}