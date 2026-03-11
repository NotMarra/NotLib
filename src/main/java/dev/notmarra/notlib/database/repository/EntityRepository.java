package dev.notmarra.notlib.database.repository;

import dev.notmarra.notlib.database.Database;
import dev.notmarra.notlib.database.EntityTable;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class EntityRepository<T> {
    private final Database database;
    private final EntityTable<T> table;

    public EntityRepository(Database database, Class<T> clazz) {
        this.database = database;
        this.table = new EntityTable<>(clazz);
    }

    public void createTable() {
        database.withConnection(conn -> conn.createStatement().execute(table.buildCreateTable()));
    }

    public void insert(T entity) {
        List<EntityTable.FieldColumn> cols = table.getColumns();
        String columns = cols.stream().map(fc -> fc.annotation().name()).collect(Collectors.joining(", "));
        String placeholders = cols.stream().map(fc -> "?").collect(Collectors.joining(", "));
        String sql = "INSERT INTO " + table.getTableName() + " (" + columns + ") VALUES (" + placeholders + ")";

        database.withConnection(conn -> {
            PreparedStatement stmt = conn.prepareStatement(sql);
            bindValues(stmt, entity, cols);
            stmt.executeUpdate();
        });
    }
    
    public void insertAsync(T entity) {
        CompletableFuture.runAsync(() -> insert(entity));
    }

    public Optional<T> findById(Object id) {
        EntityTable.FieldColumn pk = table.getPrimaryKey()
                .orElseThrow(() -> new IllegalStateException("Entity does not have a primary key"));

        String sql = "SELECT * FROM " + table.getTableName() + " WHERE " + pk.annotation().name() + " = ?";

        final Optional<T>[] result = new Optional[]{Optional.empty()};
        database.withConnection(conn -> {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setObject(1, id.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) result[0] = Optional.of(mapRow(rs));
        });
        return result[0];
    }

    public CompletableFuture<Optional<T>> findByIdAsync(Object id) {
        return CompletableFuture.supplyAsync(() -> findById(id));
    }

    public void update(T entity) {
        EntityTable.FieldColumn pk = table.getPrimaryKey()
                .orElseThrow(() -> new IllegalStateException("Entity does not have a primary key"));

        List<EntityTable.FieldColumn> nonPkCols = table.getColumns().stream()
                .filter(fc -> !fc.annotation().primaryKey()).toList();

        String setClause = nonPkCols.stream()
                .map(fc -> fc.annotation().name() + " = ?").collect(Collectors.joining(", "));
        String sql = "UPDATE " + table.getTableName() + " SET " + setClause + " WHERE " + pk.annotation().name() + " = ?";

        database.withConnection(conn -> {
            PreparedStatement stmt = conn.prepareStatement(sql);
            bindValues(stmt, entity, nonPkCols);
            pk.field().setAccessible(true);
            stmt.setObject(nonPkCols.size() + 1, pk.field().get(entity).toString());
            stmt.executeUpdate();
        });
    }

    public void updateAsync(T entity) {
        CompletableFuture.runAsync(() -> update(entity));
    }

    public void delete(Object id) {
        EntityTable.FieldColumn pk = table.getPrimaryKey()
                .orElseThrow(() -> new IllegalStateException("Entity does not have a primary key"));

        String sql = "DELETE FROM " + table.getTableName() + " WHERE " + pk.annotation().name() + " = ?";
        database.withConnection(conn -> {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setObject(1, id.toString());
            stmt.executeUpdate();
        });
    }

    public void deleteAsync(Object id) {
        CompletableFuture.runAsync(() -> delete(id));
    }

    private void bindValues(PreparedStatement stmt, T entity, List<EntityTable.FieldColumn> cols) throws Exception {
        for (int i = 0; i < cols.size(); i++) {
            Field field = cols.get(i).field();
            field.setAccessible(true);
            Object value = field.get(entity);
            stmt.setObject(i + 1, value instanceof UUID ? value.toString() : value);
        }
    }

    private T mapRow(ResultSet rs) throws Exception {
        T instance = table.getEntityClass().getDeclaredConstructor().newInstance();
        for (EntityTable.FieldColumn fc : table.getColumns()) {
            fc.field().setAccessible(true);
            Object value = rs.getObject(fc.annotation().name());
            if (fc.field().getType() == UUID.class && value instanceof String s) {
                value = UUID.fromString(s);
            }
            fc.field().set(instance, value);
        }
        return instance;
    }
}
