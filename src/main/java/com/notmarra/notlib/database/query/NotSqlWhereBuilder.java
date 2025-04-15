package com.notmarra.notlib.database.query;

import java.util.ArrayList;
import java.util.List;

public class NotSqlWhereBuilder {
    private List<NotWhereClause> whereClauses = new ArrayList<>();

    public static NotSqlWhereBuilder create() {
        return new NotSqlWhereBuilder();
    }

    public static NotSqlWhereBuilder of(String column, String operator, Object value) {
        return (new NotSqlWhereBuilder()).and(column, operator, value);
    }

    public static NotSqlWhereBuilder of(String rawCondition) {
        return (new NotSqlWhereBuilder()).and(rawCondition);
    }

    public static NotSqlWhereBuilder ofOr(String column, String operator, Object value) {
        return (new NotSqlWhereBuilder()).or(column, operator, value);
    }

    public static NotSqlWhereBuilder ofOr(String rawCondition) {
        return (new NotSqlWhereBuilder()).or(rawCondition);
    }

    // WHERE clauses
    public NotSqlWhereBuilder and(String column, String operator, Object value) {
        this.whereClauses.add(new NotWhereClause(column, operator, value, "AND"));
        return this;
    }

    public NotSqlWhereBuilder and(String rawCondition) {
        this.whereClauses.add(new NotWhereClause(rawCondition, "AND"));
        return this;
    }

    public NotSqlWhereBuilder andEquals(String column, Object value) {
        this.whereClauses.add(new NotWhereClause(column, "=", value, "AND"));
        return this;
    }

    public NotSqlWhereBuilder andNotEquals(String column, Object value) {
        this.whereClauses.add(new NotWhereClause(column, "!=", value, "AND"));
        return this;
    }

    public NotSqlWhereBuilder andLessThan(String column, Object value) {
        this.whereClauses.add(new NotWhereClause(column, "<", value, "AND"));
        return this;
    }

    public NotSqlWhereBuilder andLessThanOrEquals(String column, Object value) {
        this.whereClauses.add(new NotWhereClause(column, "<=", value, "AND"));
        return this;
    }

    public NotSqlWhereBuilder andGreaterThan(String column, Object value) {
        this.whereClauses.add(new NotWhereClause(column, ">", value, "AND"));
        return this;
    }

    public NotSqlWhereBuilder andGreaterThanOrEquals(String column, Object value) {
        this.whereClauses.add(new NotWhereClause(column, ">=", value, "AND"));
        return this;
    }

    public NotSqlWhereBuilder andIn(String column, List<Object> values) {
        StringBuilder inClause = new StringBuilder(column + " IN (");
        for (int i = 0; i < values.size(); i++) {
            inClause.append(formatValue(values.get(i)));
            if (i < values.size() - 1) {
                inClause.append(", ");
            }
        }
        inClause.append(")");
        this.whereClauses.add(new NotWhereClause(inClause.toString(), values, "AND"));
        return this;
    }

    public NotSqlWhereBuilder andNotIn(String column, List<Object> values) {
        StringBuilder notInClause = new StringBuilder(column + " NOT IN (");
        for (int i = 0; i < values.size(); i++) {
            notInClause.append(formatValue(values.get(i)));
            if (i < values.size() - 1) {
                notInClause.append(", ");
            }
        }
        notInClause.append(")");
        this.whereClauses.add(new NotWhereClause(notInClause.toString(), values, "AND"));
        return this;
    }

    public NotSqlWhereBuilder andLike(String column, Object value) {
        this.whereClauses.add(new NotWhereClause(column, "LIKE", value, "AND"));
        return this;
    }

    public NotSqlWhereBuilder andNotLike(String column, Object value) {
        this.whereClauses.add(new NotWhereClause(column, "NOT LIKE", value, "AND"));
        return this;
    }

    public NotSqlWhereBuilder andNull(String column) {
        this.whereClauses.add(new NotWhereClause(column, "IS NULL", null, "AND"));
        return this;
    }

    public NotSqlWhereBuilder andNotNull(String column) {
        this.whereClauses.add(new NotWhereClause(column, "IS NOT NULL", null, "AND"));
        return this;
    }

    public NotSqlWhereBuilder or(String column, String operator, Object value) {
        this.whereClauses.add(new NotWhereClause(column, operator, value, "OR"));
        return this;
    }

    public NotSqlWhereBuilder or(String rawCondition) {
        this.whereClauses.add(new NotWhereClause(rawCondition, "OR"));
        return this;
    }

    private String formatValue(Object value) {
        if (value == null) return "NULL";
        return "?";
    }

    public void build(StringBuilder query) {
        if (!whereClauses.isEmpty()) {
            query.append(" WHERE ");
            boolean isFirst = true;

            for (NotWhereClause clause : whereClauses) {
                if (!isFirst) {
                    query.append(" ").append(clause.getLogicalOperator()).append(" ");
                }
                
                if (clause.isRaw()) {
                    query.append(clause.getRawCondition());
                } else {
                    if (clause.isMulti()) {
                        query.append(clause.getSql());
                    } else {
                        query.append(clause.getSql())
                            .append(" ")
                            .append(clause.getOperator())
                            .append(" ")
                            .append(formatValue(clause.getValue()));
                    }
                }
                
                isFirst = false;
            }
        }
    }

    public List<Object> getParameters() {
        List<Object> params = new ArrayList<>();
        
        for (NotWhereClause clause : whereClauses) {
            if (!clause.isRaw()) params.addAll(clause.getValues());
        }
        
        return params;
    }

    private static class NotWhereClause {
        private String sql;
        private String operator;
        private List<Object> values;
        private String logicalOperator;
        private boolean isRaw;
        private String rawCondition;

        public NotWhereClause(String sql, String operator, Object value, String logicalOperator) {
            this.sql = sql;
            this.operator = operator;
            this.values = List.of(value);
            this.logicalOperator = logicalOperator;
            this.isRaw = false;
        }

        public NotWhereClause(String rawCondition, String logicalOperator) {
            this.rawCondition = rawCondition;
            this.logicalOperator = logicalOperator;
            this.isRaw = true;
        }

        public NotWhereClause(String sql, List<Object> values, String logicalOperator) {
            this.sql = sql;
            this.values = values;
            this.logicalOperator = logicalOperator;
            this.isRaw = false;
        }

        public boolean isMulti() { return values.size() > 1; }

        public String getSql() { return sql; }
        public String getOperator() { return operator; }
        public List<Object> getValues() { return values; }
        public Object getValue() { return values.isEmpty() ? null : values.get(0); }
        public String getLogicalOperator() { return logicalOperator; }
        public boolean isRaw() { return isRaw; }
        public String getRawCondition() { return rawCondition; }
    }
}