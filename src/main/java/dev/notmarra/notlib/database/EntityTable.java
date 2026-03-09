package dev.notmarra.notlib.database;

import dev.notmarra.notlib.database.annotation.Column;
import dev.notmarra.notlib.database.annotation.Table;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class EntityTable<T> {
    private final Class<T> clazz;
    private final String tableName;
    private final List<FieldColumn> columns;

    public record FieldColumn(Field field, Column annotation) {}

    public EntityTable(Class<T> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) throw new IllegalArgumentException(clazz.getName() + " does not have @Table annotation!");

        this.clazz = clazz;
        this.tableName = table.name();
        this.columns = Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Column.class))
                .map(f -> new FieldColumn(f, f.getAnnotation(Column.class)))
                .toList();
    }

    public String buildCreateTable() {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");

        List<String> parts = new ArrayList<>();
        for (FieldColumn fc : columns) {
            StringBuilder col = new StringBuilder();
            col.append(fc.annotation().name()).append(" ");
            col.append(TypeMapper.toSqlType(fc.field().getType()));

            if (fc.annotation().primaryKey())    col.append(" PRIMARY KEY");
            if (fc.annotation().autoIncrement()) col.append(" AUTO_INCREMENT");
            if (!fc.annotation().nullable())     col.append(" NOT NULL");
            if (fc.annotation().unique())        col.append(" UNIQUE");

            parts.add(col.toString());
        }

        sql.append(String.join(", ", parts)).append(")");
        return sql.toString();
    }

    public String getTableName() { return tableName; }
    public List<FieldColumn> getColumns() { return columns; }
    public Class<T> getEntityClass() { return clazz; }

    public Optional<FieldColumn> getPrimaryKey() {
        return columns.stream().filter(fc -> fc.annotation().primaryKey()).findFirst();
    }
}
