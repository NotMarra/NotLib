package dev.notmarra.notlib.database;

public interface Database {
    void connect();
    void disconnect();
    boolean isConnected();

    QueryBuilder query(String table);
}
