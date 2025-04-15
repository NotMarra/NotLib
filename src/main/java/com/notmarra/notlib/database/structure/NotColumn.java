package com.notmarra.notlib.database.structure;

public class NotColumn {
    private final String name;
    private NotColumnType type;
    private int length;
    private int precision;  // For DECIMAL type
    private int scale;      // For DECIMAL type
    private boolean primaryKey;
    private boolean autoIncrement;
    private boolean notNull;
    private boolean unique;
    private String rawDefaultValue;
    private String defaultValue;

    public NotColumn(String name) { this.name = name; }

    public static NotColumn create(String name) { return new NotColumn(name); }

    public NotColumn type(NotColumnType type) { this.type = type; return this; }
    public NotColumn primaryKey() { this.primaryKey = true; return this; }
    public NotColumn autoIncrement() {
        this.autoIncrement = true;
        this.primaryKey = true;
        return this;
    }
    public NotColumn notNull() { this.notNull = true; return this; }
    public NotColumn unique() { this.unique = true; return this; }
    public NotColumn length(int length) { this.length = length; return this; }
    public NotColumn precision(int precision, int scale) { 
        this.precision = precision; 
        this.scale = scale; 
        return this; 
    }
    public NotColumn rawDefaultValue(String rawDefaultValue) { this.rawDefaultValue = rawDefaultValue; return this; }
    public NotColumn defaultValue(String defaultValue) { this.defaultValue = defaultValue; return this; }

    public String getName() { return name; }
    public NotColumnType getType() { return type; }
    public int getLength() { return length; }
    public int getPrecision() { return precision; }
    public int getScale() { return scale; }
    public boolean isPrimaryKey() { return primaryKey; }
    public boolean isAutoIncrement() { return autoIncrement; }
    public boolean isNotNull() { return notNull; }
    public boolean isUnique() { return unique; }
    public String getRawDefaultValue() { return rawDefaultValue; }
    public String getDefaultValue() { return defaultValue; }

    public static NotColumn string(String name) { return new NotColumn(name).type(NotColumnType.STRING); }
    public static NotColumn varchar(String name, int length) { return new NotColumn(name).type(NotColumnType.VARCHAR).length(length); }
    public static NotColumn integer(String name) { return new NotColumn(name).type(NotColumnType.INTEGER).length(11); }
    public static NotColumn integer(String name, int length) { return new NotColumn(name).type(NotColumnType.INTEGER).length(length); }
    public static NotColumn longType(String name) { return new NotColumn(name).type(NotColumnType.LONG); }
    public static NotColumn floatType(String name) { return new NotColumn(name).type(NotColumnType.FLOAT); }
    public static NotColumn doubleType(String name) { return new NotColumn(name).type(NotColumnType.DOUBLE); }
    public static NotColumn booleanType(String name) { return new NotColumn(name).type(NotColumnType.BOOLEAN); }
    public static NotColumn date(String name) { return new NotColumn(name).type(NotColumnType.DATE); }
    public static NotColumn time(String name) { return new NotColumn(name).type(NotColumnType.TIME); }
    public static NotColumn dateTime(String name) { return new NotColumn(name).type(NotColumnType.DATETIME); }
    public static NotColumn timeStamp(String name) { return new NotColumn(name).type(NotColumnType.TIMESTAMP); }
    
    public static NotColumn char_(String name, int length) { return new NotColumn(name).type(NotColumnType.CHAR).length(length); }
    public static NotColumn decimal(String name, int precision, int scale) { return new NotColumn(name).type(NotColumnType.DECIMAL).precision(precision, scale); }
    public static NotColumn bit(String name, int length) { return new NotColumn(name).type(NotColumnType.BIT).length(length); }
    public static NotColumn binary(String name, int length) { return new NotColumn(name).type(NotColumnType.BINARY).length(length); }
    public static NotColumn varbinary(String name, int length) { return new NotColumn(name).type(NotColumnType.VARBINARY).length(length); }
}