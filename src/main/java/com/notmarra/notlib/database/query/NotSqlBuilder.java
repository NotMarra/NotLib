package com.notmarra.notlib.database.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotSqlBuilder {
    private String table;
    private List<String> columns = new ArrayList<>();
    private NotSqlWhereBuilder whereBuilder = NotSqlWhereBuilder.create();
    private List<String> joinClauses = new ArrayList<>();
    private List<String> orderByClauses = new ArrayList<>();
    private List<String> groupByClauses = new ArrayList<>();
    private Map<String, Object> updateValues = new HashMap<>();
    private List<List<Object>> insertValues = new ArrayList<>();
    private Integer limit;
    private Integer offset;
    private QueryType queryType;

    private enum QueryType {
        SELECT, INSERT, UPDATE, DELETE
    }

    // Static factory methods
    public static NotSqlBuilder select(String table) {
        return new NotSqlBuilder().from(table).setQueryType(QueryType.SELECT);
    }

    public static NotSqlBuilder select(String table, List<String> columns) {
        return new NotSqlBuilder().from(table).columns(columns).setQueryType(QueryType.SELECT);
    }

    public static NotSqlBuilder update(String table) {
        return new NotSqlBuilder().table(table).setQueryType(QueryType.UPDATE);
    }

    public static NotSqlBuilder insertInto(String table) {
        return new NotSqlBuilder().table(table).setQueryType(QueryType.INSERT);
    }

    public static NotSqlBuilder deleteFrom(String table) {
        return new NotSqlBuilder().table(table).setQueryType(QueryType.DELETE);
    }

    private NotSqlBuilder() {}

    // Table and columns
    public NotSqlBuilder table(String table) {
        this.table = table;
        return this;
    }

    public NotSqlBuilder from(String table) {
        return table(table);
    }

    public NotSqlBuilder columns(List<String> columns) {
        for (String column : columns) {
            this.columns.add(column);
        }
        return this;
    }

    // WHERE clauses
    public NotSqlBuilder where(NotSqlWhereBuilder where) {
        this.whereBuilder = where;
        return this;
    }

    public NotSqlBuilder where(String column, String operator, Object value) {
        this.whereBuilder.and(column, operator, value);
        return this;
    }

    public NotSqlBuilder where(String rawCondition) {
        this.whereBuilder.and(rawCondition);
        return this;
    }

    public NotSqlBuilder orWhere(String column, String operator, Object value) {
        this.whereBuilder.or(column, operator, value);
        return this;
    }

    public NotSqlBuilder orWhere(String rawCondition) {
        this.whereBuilder.or(rawCondition);
        return this;
    }

    // JOIN clauses
    public NotSqlBuilder join(String table, String condition) {
        this.joinClauses.add("JOIN " + table + " ON " + condition);
        return this;
    }

    public NotSqlBuilder leftJoin(String table, String condition) {
        this.joinClauses.add("LEFT JOIN " + table + " ON " + condition);
        return this;
    }

    public NotSqlBuilder rightJoin(String table, String condition) {
        this.joinClauses.add("RIGHT JOIN " + table + " ON " + condition);
        return this;
    }

    // ORDER BY clauses
    public NotSqlBuilder orderBy(String column) {
        this.orderByClauses.add(column + " ASC");
        return this;
    }

    public NotSqlBuilder orderBy(String column, String direction) {
        this.orderByClauses.add(column + " " + direction);
        return this;
    }

    // GROUP BY clauses
    public NotSqlBuilder groupBy(String... columns) {
        for (String column : columns) {
            this.groupByClauses.add(column);
        }
        return this;
    }

    // LIMIT and OFFSET
    public NotSqlBuilder limit(int limit) {
        this.limit = limit;
        return this;
    }

    public NotSqlBuilder offset(int offset) {
        this.offset = offset;
        return this;
    }

    // UPDATE values
    public NotSqlBuilder set(String column, Object value) {
        this.updateValues.put(column, value);
        return this;
    }

    // INSERT values
    public NotSqlBuilder values(List<Object> values) {
        this.insertValues.add(values);
        return this;
    }

    public NotSqlBuilder values(Object... values) {
        List<Object> valuesList = new ArrayList<>();
        for (Object value : values) {
            valuesList.add(value);
        }
        return values(valuesList);
    }

    private NotSqlBuilder setQueryType(QueryType queryType) {
        this.queryType = queryType;
        return this;
    }

    public String build() {
        switch (queryType) {
            case SELECT:
                return buildSelectQuery();
            case INSERT:
                return buildInsertQuery();
            case UPDATE:
                return buildUpdateQuery();
            case DELETE:
                return buildDeleteQuery();
            default:
                throw new IllegalStateException("Query type not specified");
        }
    }

    private String buildSelectQuery() {
        StringBuilder query = new StringBuilder("SELECT ");

        if (columns.isEmpty()) {
            query.append("*");
        } else {
            query.append(String.join(", ", columns));
        }

        query.append(" FROM ").append(table);

        if (!joinClauses.isEmpty()) {
            query.append(" ").append(String.join(" ", joinClauses));
        }

        whereBuilder.build(query);
        appendGroupByClause(query);
        appendOrderByClause(query);
        appendLimitOffset(query);

        return query.toString();
    }

    private String buildInsertQuery() {
        StringBuilder query = new StringBuilder("INSERT INTO ").append(table);

        if (!columns.isEmpty()) {
            query.append(" (").append(String.join(", ", columns)).append(")");
        }

        query.append(" VALUES ");

        List<String> valueGroups = new ArrayList<>();
        for (List<Object> valueSet : insertValues) {
            List<String> formattedValues = new ArrayList<>();
            for (Object value : valueSet) {
                formattedValues.add(formatValue(value));
            }
            valueGroups.add("(" + String.join(", ", formattedValues) + ")");
        }

        query.append(String.join(", ", valueGroups));

        return query.toString();
    }

    private String buildUpdateQuery() {
        StringBuilder query = new StringBuilder("UPDATE ").append(table).append(" SET ");

        List<String> setStatements = new ArrayList<>();
        for (Map.Entry<String, Object> entry : updateValues.entrySet()) {
            setStatements.add(entry.getKey() + " = " + formatValue(entry.getValue()));
        }

        query.append(String.join(", ", setStatements));

        whereBuilder.build(query);

        return query.toString();
    }

    private String buildDeleteQuery() {
        StringBuilder query = new StringBuilder("DELETE FROM ").append(table);

        whereBuilder.build(query);

        return query.toString();
    }

    private void appendGroupByClause(StringBuilder query) {
        if (!groupByClauses.isEmpty()) {
            query.append(" GROUP BY ").append(String.join(", ", groupByClauses));
        }
    }

    private void appendOrderByClause(StringBuilder query) {
        if (!orderByClauses.isEmpty()) {
            query.append(" ORDER BY ").append(String.join(", ", orderByClauses));
        }
    }

    private void appendLimitOffset(StringBuilder query) {
        if (limit != null) {
            query.append(" LIMIT ").append(limit);
            
            if (offset != null) {
                query.append(" OFFSET ").append(offset);
            }
        }
    }

    private String formatValue(Object value) {
        if (value == null) return "NULL";
        return "?";
    }

    public List<Object> getParameters() {
        List<Object> params = new ArrayList<>();
        
        params.addAll(whereBuilder.getParameters());
        
        if (queryType == QueryType.UPDATE) {
            for (Object value : updateValues.values()) {
                params.add(value);
            }
        }
        
        if (queryType == QueryType.INSERT) {
            for (List<Object> values : insertValues) {
                params.addAll(values);
            }
        }
        
        return params;
    }
}