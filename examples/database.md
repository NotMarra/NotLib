# Documentation: Working with NotDatabase API

This documentation describes how to properly set up and initialize database connections (MySQL or SQLite) using the NotLib library.

## 1. Configuration Structure (config.yml)

All database settings are now centralized under the `data` section. The `type` key determines which driver to use. The `databaseId` key is optional; if omitted, the library defaults to using the plugin's name.

```yaml
data:
  # Database type: SQLite or MySQL
  type: "SQLite"

  # Optional unique ID (useful if managing multiple databases in one plugin)
  # databaseId: "MyUniqueDatabase"

  # SQLite Configuration (Used if type is SQLite)
  file: "data"

  # MySQL Configuration (Used if type is MySQL)
  mysql:
    host: "127.0.0.1"
    port: 3306
    username: "root"
    database: "minecraft_db"
    password: ""
```

## 2. Implementing Your Database Class

Create a class that extends either `NotSQLite` or `NotMySQL`. This is where you define your table schemas.

```java
public class MyDatabase extends NotSQLite {

    public final String T_CREDITS = "not_credits";
    public final String C_UUID = "uuid";
    public final String C_AMOUNT = "amount";

    public MyDatabase(NotPlugin plugin, String configFileName) {
        super(plugin, configFileName);
        // Register this class within the NotLib configuration system
        registerConfigurable();
    }

    @Override
    public List<NotTable> setupTables() {
        return List.of(
            new NotTable(T_CREDITS)
                .addColumn(NotColumn.varchar(C_UUID, 36).primaryKey().notNull())
                .addColumn(NotColumn.integer(C_AMOUNT).notNull().defaultValue("0"))
        );
    }

    // Example helper method to retrieve data
    public int getCredits(UUID uuid) {
        NotRecord record = getTable(T_CREDITS).selectOne(builder ->
            builder.where(C_UUID, "=", uuid.toString())
        );
        return record.getInteger(C_AMOUNT, 0);
    }
}
```

## 3. Initialization in the Plugin

In your main plugin class (`NotPlugin`), register and initialize the database within the `initNotPlugin` method.

```java
@Override
public void initNotPlugin() {
    // 1. Determine the database type from the config
    String type = getConfig().getString("data.type", "SQLite");
    NotDatabase myDb;

    // 2. Instantiate the correct implementation
    if (type.equalsIgnoreCase("MySQL")) {
        myDb = new MyMySQLDatabase(this, "config.yml");
    } else {
        myDb = new MyDatabase(this, "config.yml");
    }

    // 3. Register with the manager (this triggers connect() and setupTables())
    NotDatabaseManager.getInstance().registerDatabase(myDb);
}
```
