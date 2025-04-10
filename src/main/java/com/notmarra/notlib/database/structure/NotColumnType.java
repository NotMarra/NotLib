package com.notmarra.notlib.database.structure;

public enum NotColumnType {
    VARCHAR("VARCHAR", true),
    CHAR("CHAR", true),
    STRING("TEXT", false),
    INTEGER("INTEGER", false),
    LONG("BIGINT", false),
    FLOAT("FLOAT", false),
    DOUBLE("DOUBLE", false),
    DECIMAL("DECIMAL", true),
    BIT("BIT", true),
    BINARY("BINARY", true),
    VARBINARY("VARBINARY", true),
    BOOLEAN("BOOLEAN", false),
    DATE("DATE", false),
    TIME("TIME", false),
    DATETIME("DATETIME", false),
    TIMESTAMP("TIMESTAMP", false);

    private final String sqlType;
    private final boolean requiresLength;

    NotColumnType(String sqlType, boolean requiresLength) {
        this.sqlType = sqlType;
        this.requiresLength = requiresLength;
    }

    public String getSqlType() {
        return sqlType;
    }
    
    public boolean requiresLength() {
        return requiresLength;
    }
}