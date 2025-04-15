package com.notmarra.notlib.utils;

import javax.annotation.Nullable;

public class NotConverter {
    public static Object convertToType(Object value, Class<?> type) {
        if (type == String.class) { return value.toString();
        } else if (type == Integer.class) { return toInteger(value);
        } else if (type == Long.class) { return toLong(value);
        } else if (type == Float.class) { return toFloat(value);
        } else if (type == Double.class) { return toDouble(value);
        } else if (type == Boolean.class) { return toBoolean(value);
        } else if (type == Character.class) { return toCharacter(value);
        } else if (type == Byte.class) { return toByte(value);
        } else if (type == Short.class) { return toShort(value); }
        return value;
    }

    public static @Nullable Integer toInteger(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (value instanceof Boolean) {
            return ((Boolean) value) ? 1 : 0;
        } else if (value instanceof Character) {
            return (int) ((Character) value);
        } else if (value instanceof Byte) {
            return ((Byte) value).intValue();
        } else if (value instanceof Short) {
            return ((Short) value).intValue();
        } else if (value instanceof Long) {
            return ((Long) value).intValue();
        } else if (value instanceof Float) {
            return ((Float) value).intValue();
        } else {
            return null;
        }
    }

    public static @Nullable Long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (value instanceof Boolean) {
            return ((Boolean) value) ? 1L : 0L;
        } else if (value instanceof Character) {
            return (long) ((Character) value);
        } else if (value instanceof Byte) {
            return ((Byte) value).longValue();
        } else if (value instanceof Short) {
            return ((Short) value).longValue();
        } else if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else if (value instanceof Float) {
            return ((Float) value).longValue();
        } else {
            return null;
        }
    }

    public static @Nullable Float toFloat(Object value) {
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        } else if (value instanceof String) {
            try {
                return Float.parseFloat((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (value instanceof Boolean) {
            return ((Boolean) value) ? 1.0f : 0.0f;
        } else if (value instanceof Character) {
            return (float) ((Character) value);
        } else if (value instanceof Byte) {
            return ((Byte) value).floatValue();
        } else if (value instanceof Short) {
            return ((Short) value).floatValue();
        } else if (value instanceof Integer) {
            return ((Integer) value).floatValue();
        } else if (value instanceof Long) {
            return ((Long) value).floatValue();
        } else {
            return null;
        }
    }

    public static @Nullable Double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (value instanceof Boolean) {
            return ((Boolean) value) ? 1.0 : 0.0;
        } else if (value instanceof Character) {
            return (double) ((Character) value);
        } else if (value instanceof Byte) {
            return ((Byte) value).doubleValue();
        } else if (value instanceof Short) {
            return ((Short) value).doubleValue();
        } else if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else if (value instanceof Long) {
            return ((Long) value).doubleValue();
        } else if (value instanceof Float) {
            return ((Float) value).doubleValue();
        } else {
            return null;
        }
    }

    public static @Nullable Boolean toBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        } else if (value instanceof Character) {
            return ((Character) value) != '\0';
        } else if (value instanceof Byte) {
            return ((Byte) value).intValue() != 0;
        } else if (value instanceof Short) {
            return ((Short) value).intValue() != 0;
        } else if (value instanceof Integer) {
            return ((Integer) value).intValue() != 0;
        } else if (value instanceof Long) {
            return ((Long) value).intValue() != 0;
        } else if (value instanceof Float) {
            return ((Float) value).intValue() != 0;
        } else {
            return null;
        }
    }

    public static @Nullable Character toCharacter(Object value) {
        if (value instanceof Character) {
            return (Character) value;
        } else if (value instanceof String) {
            return ((String) value).charAt(0);
        } else if (value instanceof Number) {
            return (char) ((Number) value).intValue();
        } else if (value instanceof Boolean) {
            return ((Boolean) value) ? '1' : '0';
        } else if (value instanceof Byte) {
            return (char) ((Byte) value).intValue();
        } else if (value instanceof Short) {
            return (char) ((Short) value).intValue();
        } else if (value instanceof Integer) {
            return (char) ((Integer) value).intValue();
        } else if (value instanceof Long) {
            return (char) ((Long) value).intValue();
        } else if (value instanceof Float) {
            return (char) ((Float) value).intValue();
        } else {
            return null;
        }
    }

    public static @Nullable Byte toByte(Object value) {
        if (value instanceof Byte) {
            return (Byte) value;
        } else if (value instanceof String) {
            try {
                return Byte.parseByte((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (value instanceof Number) {
            return ((Number) value).byteValue();
        } else if (value instanceof Boolean) {
            return ((Boolean) value) ? (byte) 1 : (byte) 0;
        } else if (value instanceof Character) {
            return (byte) ((Character) value).charValue();
        } else if (value instanceof Short) {
            return ((Short) value).byteValue();
        } else if (value instanceof Integer) {
            return ((Integer) value).byteValue();
        } else if (value instanceof Long) {
            return ((Long) value).byteValue();
        } else if (value instanceof Float) {
            return ((Float) value).byteValue();
        } else {
            return null;
        }
    }

    public static @Nullable Short toShort(Object value) {
        if (value instanceof Short) {
            return (Short) value;
        } else if (value instanceof String) {
            try {
                return Short.parseShort((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (value instanceof Number) {
            return ((Number) value).shortValue();
        } else if (value instanceof Boolean) {
            return ((Boolean) value) ? (short) 1 : (short) 0;
        } else if (value instanceof Character) {
            return (short) ((Character) value).charValue();
        } else if (value instanceof Byte) {
            return ((Byte) value).shortValue();
        } else if (value instanceof Integer) {
            return ((Integer) value).shortValue();
        } else if (value instanceof Long) {
            return ((Long) value).shortValue();
        } else if (value instanceof Float) {
            return ((Float) value).shortValue();
        } else {
            return null;
        }
    }

    public static String toString(Object value) {
        return value instanceof String ? (String) value : value != null ? value.toString() : "";
    }
}