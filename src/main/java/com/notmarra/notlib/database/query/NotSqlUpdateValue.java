package com.notmarra.notlib.database.query;

import java.util.List;

public class NotSqlUpdateValue {
    private final String column;
    private final String newValue;
    private final List<Object> values;
    private final boolean isRaw;

    public NotSqlUpdateValue(String column, String newValue, Object value, boolean isRaw) {
        this(column, newValue, List.of(value), isRaw);
    }

    public NotSqlUpdateValue(String column, String newValue, List<Object> values, boolean isRaw) {
        this.column = column;
        this.newValue = newValue;
        this.values = values;
        this.isRaw = isRaw;
    }

    public boolean isMulti() { return values.size() > 1; }
    public String getColumn() { return column; }
    public String getNewValue() { return newValue; }
    public List<Object> getValues() { return values; }
    public boolean isRaw() { return isRaw; }

    public static NotSqlUpdateValue of(String column, Object value) {
        return new NotSqlUpdateValue(column, "?", value, false);
    }

    public static NotSqlUpdateValue of(String column, String newValue, Object value) {
        return new NotSqlUpdateValue(column, newValue, value, false);
    }

    public static NotSqlUpdateValue of(String column, String newValue, List<Object> values) {
        return new NotSqlUpdateValue(column, newValue, values, false);
    }

    public static NotSqlUpdateValue ofRaw(String column, Object value) {
        return new NotSqlUpdateValue(column, "?", value, true);
    }

    public static NotSqlUpdateValue ofRaw(String column, String newValue, String rawValue) {
        return new NotSqlUpdateValue(column, newValue, rawValue, true);
    }

    public static NotSqlUpdateValue ofRaw(String column, String newValue, List<Object> values) {
        return new NotSqlUpdateValue(column, newValue, values, true);
    }
}
