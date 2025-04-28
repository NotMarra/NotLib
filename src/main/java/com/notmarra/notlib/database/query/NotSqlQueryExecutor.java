package com.notmarra.notlib.database.query;

import com.notmarra.notlib.database.NotDatabase;
import com.notmarra.notlib.database.structure.NotRecord;

import java.util.List;

import javax.annotation.Nullable;

public class NotSqlQueryExecutor {
    private final NotDatabase database;

    public NotSqlQueryExecutor(NotDatabase database) {
        this.database = database;
    }

    /**
     * Executes a query and returns a single result as a map
     */

    public NotRecord selectOne(String sql) {
        NotRecord result = selectOneOrNull(sql);
        return result != null ? result : NotRecord.empty();
    }

    public NotRecord selectOne(NotSqlBuilder builder) {
        NotRecord result = selectOneOrNull(builder);
        return result != null ? result : NotRecord.empty();
    }

    public @Nullable NotRecord selectOneOrNull(String sql) {
        List<NotRecord> results = select(sql);
        return results.isEmpty() ? null : results.get(0);
    }

    public @Nullable NotRecord selectOneOrNull(NotSqlBuilder builder) {
        List<NotRecord> results = select(builder);
        return results.isEmpty() ? null : results.get(0);
    }

    public List<NotRecord> select(String sql) {
        return database.processSelect(sql, List.of());
    }

    public List<NotRecord> select(NotSqlBuilder builder) {
        return database.processSelect(builder.build(), builder.getParameters());
    }

    public int update(String sql) {
        return database.processUpdate(sql, List.of());
    }

    public int update(NotSqlBuilder builder) {
        return database.processUpdate(builder.build(), builder.getParameters());
    }

    /**
     * Executes a query to check if a record exists
     */
    public boolean exists(NotSqlBuilder builder) { return !select(builder).isEmpty(); }

    /**
     * Executes a query to insert/update/delete a record and returns the number of affected rows
     * -1 if an error occurred
     */
    public boolean succeeded(NotSqlBuilder builder) { return update(builder) > -1; }

    /**
     * Executes a query and returns the count of results
     */
    public int count(NotSqlBuilder builder) { return select(builder).size(); }
}