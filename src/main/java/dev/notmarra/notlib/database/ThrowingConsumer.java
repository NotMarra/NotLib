package dev.notmarra.notlib.database;

import java.sql.SQLException;

@FunctionalInterface
public interface ThrowingConsumer<T> {
    void accept(T t) throws Exception;
}