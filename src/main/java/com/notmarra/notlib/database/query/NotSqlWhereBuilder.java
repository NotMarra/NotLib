package com.notmarra.notlib.database.query;

import java.util.ArrayList;
import java.util.List;

public class NotSqlWhereBuilder {
    private List<NotWhereClause> whereClauses = new ArrayList<>();

    public static NotSqlWhereBuilder create() {
        return new NotSqlWhereBuilder();
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
                    query.append(clause.getColumn())
                        .append(" ")
                        .append(clause.getOperator())
                        .append(" ")
                        .append(formatValue(clause.getValue()));
                }
                
                isFirst = false;
            }
        }
    }

    public List<Object> getParameters() {
        List<Object> params = new ArrayList<>();
        
        for (NotWhereClause clause : whereClauses) {
            if (!clause.isRaw()) {
                params.add(clause.getValue());
            }
        }
        
        return params;
    }

    private static class NotWhereClause {
        private String column;
        private String operator;
        private Object value;
        private String logicalOperator;
        private boolean isRaw;
        private String rawCondition;

        public NotWhereClause(String column, String operator, Object value, String logicalOperator) {
            this.column = column;
            this.operator = operator;
            this.value = value;
            this.logicalOperator = logicalOperator;
            this.isRaw = false;
        }

        public NotWhereClause(String rawCondition, String logicalOperator) {
            this.rawCondition = rawCondition;
            this.logicalOperator = logicalOperator;
            this.isRaw = true;
        }

        public String getColumn() { return column; }
        public String getOperator() { return operator; }
        public Object getValue() { return value; }
        public String getLogicalOperator() { return logicalOperator; }
        public boolean isRaw() { return isRaw; }
        public String getRawCondition() { return rawCondition; }
    }
}