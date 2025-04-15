package com.notmarra.notlib;

import java.util.List;

import org.bukkit.entity.Player;

import com.notmarra.notlib.database.query.NotSqlWhereBuilder;
import com.notmarra.notlib.database.source.NotMySQL;
import com.notmarra.notlib.database.structure.NotColumn;
import com.notmarra.notlib.database.structure.NotRecord;
import com.notmarra.notlib.database.structure.NotTable;
import com.notmarra.notlib.extensions.NotPlugin;

class NotDevTestMySQL extends NotMySQL {
    public static final String ID = "NotDevTestMySQL";

    public final String T_USERS = "users";
    public final String T_USERS_C_UUID = "uuid";
    public final String T_USERS_C_PLAYER_NAME = "player_name";
    public final String T_USERS_C_BALANCE = "balance";
    public final String T_USERS_C_XP = "xp";

    public NotDevTestMySQL(NotPlugin plugin, String defaultConfig) {
        super(plugin, defaultConfig);
        registerConfigurable();
    }

    @Override
    public String getId() { return ID; }

    public List<NotRecord> getPlayersWhere(NotSqlWhereBuilder where) {
        return getTable(T_USERS).select(b -> b.where(where));
    }

    public double getPlayerBalance(Player player) {
        return getTable(T_USERS)
            .selectOne(b -> b.whereEquals(T_USERS_C_UUID, player.getUniqueId().toString()))
            .getDouble(T_USERS_C_BALANCE, 0.0);
    }

    public boolean existsPlayer(Player player) {
        return getTable(T_USERS)
            .exists(b -> b.whereEquals(T_USERS_C_UUID, player.getUniqueId().toString()));
    }

    public boolean deletePlayer(String uuid) {
        return getTable(T_USERS)
            .deleteSucceded(b -> b.whereEquals(T_USERS_C_UUID, uuid));
    }

    public boolean insertPlayer(Player player) {
        return getTable(T_USERS).insertRow(List.of(
            player.getUniqueId().toString(),
            player.getName(),
            0.0,
            0
        ));
    }

    public boolean addXp(Player player, int xp) {
        return getTable(T_USERS)
            .update(b -> {
                b.setRaw(T_USERS_C_XP, "xp + ?", List.of(xp));
                b.whereEquals(T_USERS_C_UUID, player.getUniqueId().toString());
            }) > 0;
    }

    public boolean removeXp(Player player, int xp) {
        return getTable(T_USERS)
            .update(b -> {
                b.setRaw(T_USERS_C_XP, "xp - ?", List.of(xp));
                b.whereEquals(T_USERS_C_UUID, player.getUniqueId().toString());
            }) > 0;
    }

    @Override
    public List<NotTable> setupTables() {
        return List.of(
            NotTable.createNew(T_USERS, List.of(
                NotColumn.varchar(T_USERS_C_UUID, 36).primaryKey().notNull(),
                NotColumn.varchar(T_USERS_C_PLAYER_NAME, 36).notNull(),
                NotColumn.doubleType(T_USERS_C_BALANCE).notNull().defaultValue("0"),
                NotColumn.integer(T_USERS_C_XP).notNull().defaultValue("0")
            ))
            .initialInsertList(List.of(
                List.of("123e4567-e89b-12d3-a456-426614174000", "Player1", 100.0, 0),
                List.of("123e4567-e89b-12d3-a456-426614174001", "Player2", 200.0, 0),
                List.of("123e4567-e89b-12d3-a456-426614174002", "Player3", 300.0, 0)
            ))
        );
    }
}