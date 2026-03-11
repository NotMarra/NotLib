package dev.notmarra.notlib.database;

@FunctionalInterface
public interface ThrowingConsumer<T> {
    void accept(T t) throws Exception;
}