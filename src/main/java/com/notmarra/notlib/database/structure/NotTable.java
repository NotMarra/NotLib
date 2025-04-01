package com.notmarra.notlib.database.structure;

import java.util.ArrayList;
import java.util.List;

public class NotTable {
    private final String name;
    private List<NotColumn> columns = new ArrayList<>();
    private List<List<Object>> initialInsert = new ArrayList<>();

    public NotTable(String name) { this.name = name; }

    public String getName() { return name; }
    public NotTable addColumn(NotColumn column) { columns.add(column); return this; }
    public NotTable addColumns(List<NotColumn> columns) { this.columns.addAll(columns); return this; }
    public List<NotColumn> getColumns() { return columns; }

    public NotTable insert(List<Object> data) { initialInsert.add(data); return this; }
    public NotTable insertList(List<List<Object>> data) { initialInsert.addAll(data); return this; }
    public List<List<Object>> getInsertList() { return initialInsert; }

    public static NotTable create(String name) { return new NotTable(name); }
    
    public static NotTable create(String name, List<NotColumn> columns) { return (new NotTable(name)).addColumns(columns); }
}