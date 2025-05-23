Base example of a NotDatabase for SQLite

## config.yml
```yml
database:
  <id>:
    # name of the file with the saved data
    file: "notlibtest"
```

## NotPlugin.java
```java
@Override
public void initNotPlugin() {
    db().registerDatabase(new MySQLite(this, "config.yml"));
}
```

## MySQLite.java
```java
public class MySQLite extends NotSQLite {
    public static final String ID = "SQLite";

    public final String T_USERS = "users";
    public final String T_USERS_C_UUID = "uuid";
    public final String T_USERS_C_PLAYER_NAME = "player_name";
    public final String T_USERS_C_BALANCE = "balance";

    public MySQLite(NotPlugin plugin, String defaultConfig) {
        super(plugin, defaultConfig);
        registerConfigurable();
    }

    // NOTE: This is the id of the database in the config at path "database.<id>"
    @Override
    public String getId() { return ID; }

    public double getPlayerBalance(Player player) {
        return getTable(T_USERS)
            .recordGet(T_USERS_C_UUID, "=", player.getUniqueId().toString())
            .getDouble(T_USERS_C_BALANCE, 0.0);
    }

    public boolean existsPlayer(Player player) {
        return getTable(T_USERS)
            .recordExists(T_USERS_C_UUID, "=", player.getUniqueId().toString());
    }

    public boolean deletePlayer(String uuid) {
        return getTable(T_USERS)
            .recordDelete(T_USERS_C_UUID, "=", uuid);
    }

    public boolean insertPlayer(Player player) {
        return getTable(T_USERS).insertRow(List.of(
            player.getUniqueId().toString(),
            player.getName(),
            0.0
        ));
    }

    @Override
    public List<NotTable> setupTables() {
        return List.of(
            NotTable.createNew(T_USERS, List.of(
                NotColumn.varchar(T_USERS_C_UUID, 36).primaryKey().notNull(),
                NotColumn.varchar(T_USERS_C_PLAYER_NAME, 36).notNull(),
                NotColumn.doubleType(T_USERS_C_BALANCE).notNull().defaultValue("0")
            ))
            .initialInsertList(List.of(
                List.of("123e4567-e89b-12d3-a456-426614174000", "Player1", 100.0),
                List.of("123e4567-e89b-12d3-a456-426614174001", "Player2", 200.0),
                List.of("123e4567-e89b-12d3-a456-426614174002", "Player3", 300.0)
            ))
        );
    }
}
```