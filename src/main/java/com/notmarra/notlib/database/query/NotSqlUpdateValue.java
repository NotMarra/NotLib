package com.notmarra.notlib.database.query;

public class NotSqlUpdateValue {
    private final String column;
    private final Object value;
    private final boolean isRaw;

    public NotSqlUpdateValue(String column, Object value, boolean isRaw) {
        this.column = column;
        this.value = value;
        this.isRaw = isRaw;
    }

    public String getColumn() { return column; }
    public Object getValue() { return value; }
    public boolean isRaw() { return isRaw; }

    public static NotSqlUpdateValue of(String column, Object value) {
        return new NotSqlUpdateValue(column, value, false);
    }

    public static NotSqlUpdateValue ofRaw(String column, String rawValue) {
        return new NotSqlUpdateValue(column, rawValue, true);
    }
}
