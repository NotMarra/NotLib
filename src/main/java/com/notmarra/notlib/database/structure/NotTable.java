package com.notmarra.notlib.database.structure;

import java.util.ArrayList;
import java.util.List;

import com.notmarra.notlib.database.NotDatabase;
import com.notmarra.notlib.database.query.NotSqlBuilder;
import com.notmarra.notlib.database.query.NotSqlQueryExecutor;
import com.notmarra.notlib.database.query.NotSqlWhereBuilder;

public class NotTable {
    private final String name;
    private List<NotColumn> columns = new ArrayList<>();
    private List<List<Object>> initialInsert = new ArrayList<>();
    private NotDatabase dbCtx;

    public NotTable(String name) { this.name = name; }

    public NotDatabase db() { return dbCtx; }
    public NotTable setDbCtx(NotDatabase dbCtx) { this.dbCtx = dbCtx; return this; }

    public String getName() { return name; }
    public NotTable addColumn(NotColumn column) { columns.add(column); return this; }
    public NotTable addColumns(List<NotColumn> columns) { this.columns.addAll(columns); return this; }
    public List<NotColumn> getColumns() { return columns; }

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

    public NotRecord recordGet(String column, String operator, Object value) {
        checkDbCtx();
        return executor().fetchOne(
            NotSqlBuilder.select(getName()).where(column, operator, value)
        );
    }

    public List<NotRecord> recordsGet(NotSqlWhereBuilder where) {
        checkDbCtx();
        return executor().executeQuery(
            NotSqlBuilder.select(getName()).where(where)
        );
    }

    public boolean recordExists(String column, String operator, Object value) {
        checkDbCtx();
        return executor().exists(
            NotSqlBuilder.select(getName()).where(column, operator, value)
        );
    }

    public boolean recordDelete(String column, String operator, Object value) {
        checkDbCtx();
        return executor().succeeded(
            NotSqlBuilder.deleteFrom(getName()).where(column, operator, value)
        );
    }

    public boolean insertRow(List<Object> row) {
        checkDbCtx();
        return dbCtx.insertRow(this, row);
    }

    public int insertRows(List<List<Object>> row) {
        checkDbCtx();
        int total = 0;
        for (List<Object> r : row) {
            if (dbCtx.insertRow(this, r)) total++;
        }
        return total;
    }
}