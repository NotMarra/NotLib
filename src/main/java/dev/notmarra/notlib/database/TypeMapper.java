package dev.notmarra.notlib.database;

import java.util.UUID;

public class TypeMapper {
    public static String toSqlType(Class<?> type) {
        if (type == int.class || type == Integer.class) return "INTEGER";
        if (type == long.class || type == Long.class) return  "BIGINT";
        if (type == String.class) return "VARCHAR(255)";
        if (type == boolean.class || type == Boolean.class) return "BOOLEAN";
        if (type == double.class || type == Double.class) return "DOUBLE";
        if (type == UUID.class) return "VARCHAR(36)";
        if (type == float.class || type == Float.class) return "FLOAT";
        throw new IllegalArgumentException("Unsupported type: " + type.getName());
    }
}
