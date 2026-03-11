package dev.notmarra.notlib.database;

import java.util.UUID;

public class TypeMapper {
    public static String toSqlType(Class<?> type) {
        return toSqlType(type, 255);
    }

    public static String toSqlType(Class<?> type, int length) {
        if (type == int.class || type == Integer.class) return "INTEGER";
        if (type == long.class || type == Long.class) return "BIGINT";
        if (type == String.class) return length > 65535 ? "TEXT" : "VARCHAR(" + length + ")";
        if (type == boolean.class || type == Boolean.class) return "BOOLEAN";
        if (type == double.class || type == Double.class) return "DOUBLE";
        if (type == UUID.class) return "VARCHAR(36)";
        if (type == float.class || type == Float.class) return "FLOAT";
        throw new IllegalArgumentException("Unsupported type: " + type.getName());
    }

    public static Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;

        if ((targetType == int.class || targetType == Integer.class) && value instanceof Number n) {
            return n.intValue();
        }
        if ((targetType == long.class || targetType == Long.class) && value instanceof Number n) {
            return n.longValue();
        }
        if ((targetType == boolean.class || targetType == Boolean.class)) {
            if (value instanceof Boolean b) return b;
            if (value instanceof Number n) return n.intValue() != 0;
        }
        if ((targetType == float.class || targetType == Float.class) && value instanceof Number n) {
            return n.floatValue();
        }
        if ((targetType == double.class || targetType == Double.class) && value instanceof Number n) {
            return n.doubleValue();
        }

        return value;
    }
}