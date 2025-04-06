Base example of a NotDatabase for SQLite

## config.yml
```yml
database:
  # Configuration for SQLite
  SQLite:
    # name of the file with the saved data
    file: "notlibtest"
    # name of the table with the saved data
    table: 'NotCredits'
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

    public MySQLite(NotPlugin plugin, String defaultConfig) {
        super(plugin, defaultConfig);
        registerConfigurable();
    }

    // NOTE: This is the id of the database in the config, "database.SQLite"
    @Override
    public String getId() { return ID; }

    @Override
    public List<NotTable> setupTables() {
        String table = getDatabaseConfig().getString("table");
        if (table == null) {
            getLogger().debug("Table name not found in config for database: " + getId());
            return List.of();
        }

        return List.of(
            NotTable.create(table, List.of(
                NotColumn.varchar("uuid", 36).primaryKey().notNull(),
                NotColumn.varchar("player_name", 36).notNull(),
                NotColumn.doubleType("balance").notNull().defaultValue("0")
            ))
            .insertList(List.of(
                List.of("123e4567-e89b-12d3-a456-426614174000", "Player1", 100.0),
                List.of("123e4567-e89b-12d3-a456-426614174001", "Player2", 200.0),
                List.of("123e4567-e89b-12d3-a456-426614174002", "Player3", 300.0)
            ))
        );
    }
}
```