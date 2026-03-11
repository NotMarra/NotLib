package dev.notmarra.notlib.database.repository;

import dev.notmarra.notlib.database.Database;
import dev.notmarra.notlib.database.EntityTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class QueryBuilder<T> {
    private enum WhereMode { AND, OR }

    private record WhereClause(String column, String operator, Object value, WhereMode mode, boolean isIn) {}
    private record OrderClause(String column, SortOrder order) {}

    private final Database database;
    private final EntityTable<T> table;
    private final EntityRepository<T> repository;

    private final List<WhereClause> whereClauses = new ArrayList<>();
    private final List<OrderClause> orderClauses = new ArrayList<>();
    private Integer limit = null;
    private Integer offset = null;

    public QueryBuilder(Database database, EntityTable<T> table, EntityRepository<T> repository) {
        this.database = database;
        this.table = table;
        this.repository = repository;
    }

    // ── WHERE ────────────────────────────────────────────────────────────────

    public QueryBuilder<T> where(String column, String operator, Object value) {
        whereClauses.add(new WhereClause(column, operator, value, WhereMode.AND, false));
        return this;
    }

    public QueryBuilder<T> orWhere(String column, String operator, Object value) {
        whereClauses.add(new WhereClause(column, operator, value, WhereMode.OR, false));
        return this;
    }

    public QueryBuilder<T> whereIn(String column, Collection<?> values) {
        List<Object> converted = values.stream()
                .map(v -> v instanceof UUID ? v.toString() : v)
                .collect(Collectors.toList());
        whereClauses.add(new WhereClause(column, "IN", converted, WhereMode.AND, true));
        return this;
    }

    public QueryBuilder<T> orWhereIn(String column, Collection<?> values) {
        List<Object> converted = values.stream()
                .map(v -> v instanceof UUID ? v.toString() : v)
                .collect(Collectors.toList());
        whereClauses.add(new WhereClause(column, "IN", converted, WhereMode.OR, true));
        return this;
    }

    // ── ORDER / LIMIT / OFFSET ───────────────────────────────────────────────

    public QueryBuilder<T> orderBy(String column, SortOrder order) {
        orderClauses.add(new OrderClause(column, order));
        return this;
    }

    public QueryBuilder<T> limit(int limit) {
        this.limit = limit;
        return this;
    }

    public QueryBuilder<T> offset(int offset) {
        this.offset = offset;
        return this;
    }

    // ── SQL BUILDING ─────────────────────────────────────────────────────────

    private String buildWhereClause() {
        if (whereClauses.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(" WHERE ");
        for (int i = 0; i < whereClauses.size(); i++) {
            WhereClause wc = whereClauses.get(i);
            if (i > 0) sb.append(" ").append(wc.mode().name()).append(" ");
            if (wc.isIn()) {
                // IN potřebuje dynamický počet placeholderů podle počtu hodnot
                List<?> list = (List<?>) wc.value();
                String placeholders = list.stream().map(v -> "?").collect(Collectors.joining(", "));
                sb.append(wc.column()).append(" IN (").append(placeholders).append(")");
            } else {
                sb.append(wc.column()).append(" ").append(wc.operator()).append(" ?");
            }
        }
        return sb.toString();
    }

    private String buildOrderClause() {
        if (orderClauses.isEmpty()) return "";
        return " ORDER BY " + orderClauses.stream()
                .map(oc -> oc.column() + " " + oc.order().name())
                .collect(Collectors.joining(", "));
    }

    private String buildLimitOffset() {
        StringBuilder sb = new StringBuilder();
        if (limit != null) sb.append(" LIMIT ").append(limit);
        if (offset != null) sb.append(" OFFSET ").append(offset);
        return sb.toString();
    }

    private void bindWhereValues(PreparedStatement stmt, int startIndex) throws Exception {
        int i = startIndex;
        for (WhereClause wc : whereClauses) {
            if (wc.isIn()) {
                for (Object v : (List<?>) wc.value()) stmt.setObject(i++, v);
            } else {
                stmt.setObject(i++, wc.value());
            }
        }
    }

    // ── TERMINATORS ──────────────────────────────────────────────────────────

    public List<T> findAll() {
        String sql = "SELECT * FROM " + table.getTableName()
                + buildWhereClause()
                + buildOrderClause()
                + buildLimitOffset();

        List<T> results = new ArrayList<>();
        database.withConnection(conn -> {
            PreparedStatement stmt = conn.prepareStatement(sql);
            bindWhereValues(stmt, 1);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) results.add(repository.mapRow(rs));
        });
        return results;
    }

    public Optional<T> findFirst() {
        String sql = "SELECT * FROM " + table.getTableName()
                + buildWhereClause()
                + buildOrderClause()
                + " LIMIT 1";

        final Optional<T>[] result = new Optional[]{Optional.empty()};
        database.withConnection(conn -> {
            PreparedStatement stmt = conn.prepareStatement(sql);
            bindWhereValues(stmt, 1);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) result[0] = Optional.of(repository.mapRow(rs));
        });
        return result[0];
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM " + table.getTableName() + buildWhereClause();
        final long[] count = {0};
        database.withConnection(conn -> {
            PreparedStatement stmt = conn.prepareStatement(sql);
            bindWhereValues(stmt, 1);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) count[0] = rs.getLong(1);
        });
        return count[0];
    }

    public void delete() {
        String sql = "DELETE FROM " + table.getTableName() + buildWhereClause();
        database.withConnection(conn -> {
            PreparedStatement stmt = conn.prepareStatement(sql);
            bindWhereValues(stmt, 1);
            stmt.executeUpdate();
        });
    }

    // ── ASYNC VARIANTS ───────────────────────────────────────────────────────

    public CompletableFuture<List<T>> findAllAsync() {
        return CompletableFuture.supplyAsync(this::findAll);
    }

    public CompletableFuture<Optional<T>> findFirstAsync() {
        return CompletableFuture.supplyAsync(this::findFirst);
    }

    public CompletableFuture<Long> countAsync() {
        return CompletableFuture.supplyAsync(this::count);
    }

    public CompletableFuture<Void> deleteAsync() {
        return CompletableFuture.runAsync(this::delete);
    }
}