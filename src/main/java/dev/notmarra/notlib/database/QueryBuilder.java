package dev.notmarra.notlib.database;

import java.util.HashMap;
import java.util.List;

public abstract class QueryBuilder {
    protected String table;
    protected HashMap<String, Object> filters = new HashMap<>();
    protected List<String> selectedColumns;
    protected String orderBy;
    protected boolean ascending = true;

    public QueryBuilder(String table) {
        this.table = table;
    }

    public QueryBuilder select(String... columns) {
        this.selectedColumns = List.of(columns);
        return this;
    }

    public QueryBuilder where(String key, Object value) {
        this.filters.put(key, value);
        return this;
    }

    public QueryBuilder orderBy(String column, boolean ascending) {
        this.orderBy = column;
        this.ascending = ascending;
        return this;
    }
}
