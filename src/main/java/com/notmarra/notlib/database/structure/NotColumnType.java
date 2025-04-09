package com.notmarra.notlib.database.structure;

public enum NotColumnType {
    STRING("TEXT"),
    VARCHAR("VARCHAR"),
    INTEGER("INTEGER"),
    LONG("BIGINT"),
    FLOAT("FLOAT"),
    DOUBLE("DOUBLE"),
    BOOLEAN("BOOLEAN"),
    DATE("DATE"),
    TIME("TIME"),
    DATETIME("DATETIME"),
    TIMESTAMP("TIMESTAMP");

    private final String sqlType;

    NotColumnType(String sqlType) {
        this.sqlType = sqlType;
    }

    public String getSqlType() {
        return sqlType;
    }
}