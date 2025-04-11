package com.notmarra.notlib.database.structure;

import java.util.Map;

import javax.annotation.Nullable;

import com.notmarra.notlib.database.query.NotSqlWhereBuilder;
import com.notmarra.notlib.utils.NotConverter;

public class NotRecord {
    private final Map<String, Object> data;

    public NotRecord(Map<String, Object> data) { this.data = data; }

    public Map<String, Object> getData() { return data; }
    public boolean isEmpty() { return data.isEmpty(); }
    public boolean hasColumn(String column) { return data.containsKey(column); }

    public Object get(String column) { return data.get(column); }
    public String getString(String column) { return data.get(column).toString(); }

    public @Nullable Integer getInteger(String column) { return NotConverter.toInteger(data.get(column)); }
    public Integer getInteger(String column, int defaultValue) {
        var value = getInteger(column);
        return value != null ? value : defaultValue;
    }

    public @Nullable Long getLong(String column) { return NotConverter.toLong(data.get(column)); }
    public Long getLong(String column, long defaultValue) {
        var value = getLong(column);
        return value != null ? value : defaultValue;
    }

    public @Nullable Float getFloat(String column) { return NotConverter.toFloat(data.get(column)); }
    public Float getFloat(String column, float defaultValue) {
        var value = getFloat(column);
        return value != null ? value : defaultValue;
    }

    public @Nullable Double getDouble(String column) { return NotConverter.toDouble(data.get(column)); }
    public Double getDouble(String column, double defaultValue) {
        var value = getDouble(column);
        return value != null ? value : defaultValue;
    }

    public @Nullable Boolean getBoolean(String column) { return NotConverter.toBoolean(data.get(column)); }
    public Boolean getBoolean(String column, boolean defaultValue) {
        var value = getBoolean(column);
        return value != null ? value : defaultValue;
    }

    public @Nullable Character getCharacter(String column) { return NotConverter.toCharacter(data.get(column)); }
    public Character getCharacter(String column, char defaultValue) {
        var value = getCharacter(column);
        return value != null ? value : defaultValue;
    }

    public @Nullable Byte getByte(String column) { return NotConverter.toByte(data.get(column)); }
    public Byte getByte(String column, byte defaultValue) {
        var value = getByte(column);
        return value != null ? value : defaultValue;
    }

    public @Nullable Short getShort(String column) { return NotConverter.toShort(data.get(column)); }
    public Short getShort(String column, short defaultValue) {
        var value = getShort(column);
        return value != null ? value : defaultValue;
    }

    public NotSqlWhereBuilder buildWhere() {
        NotSqlWhereBuilder builder = NotSqlWhereBuilder.create();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            builder.and(entry.getKey(), "=", entry.getValue());
        }

        return builder;
    }

    public static NotRecord empty() { return new NotRecord(Map.of()); }
}