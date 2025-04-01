package com.notmarra.notlib;

import java.util.List;

import com.notmarra.notlib.database.source.NotSQLite;
import com.notmarra.notlib.database.structure.NotColumn;
import com.notmarra.notlib.database.structure.NotTable;
import com.notmarra.notlib.extensions.NotPlugin;
import com.notmarra.notlib.utils.ChatF;

public class NotDevSQLite extends NotSQLite {
    public NotDevSQLite(NotPlugin plugin, String defaultConfig) {
        super(plugin, defaultConfig);
        registerConfigurable();
    }

    @Override
    public List<NotTable> setup() {
        String table = getDatabaseConfig().getString("table");
        if (table == null) {
            getLogger().debug(ChatF.of("Table name not found in config for database: " + getId()).build());
            return List.of();
        }

        return List.of(
            NotTable.create(table, List.of(
                NotColumn.varchar("uuid", 36).primaryKey().notNull(),
                NotColumn.varchar("player_name", 36).notNull(),
                NotColumn.doubleType("balance").notNull().defaultValue("0")
            )).insertList(List.of(
                List.of("123e4567-e89b-12d3-a456-426614174000", "Player1", 100.0),
                List.of("123e4567-e89b-12d3-a456-426614174001", "Player2", 200.0),
                List.of("123e4567-e89b-12d3-a456-426614174002", "Player3", 300.0)
            ))
        );
    }
}
