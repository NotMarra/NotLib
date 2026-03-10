package dev.notmarra.notlib.test.database;

import dev.notmarra.notlib.database.annotation.Column;
import dev.notmarra.notlib.database.annotation.Table;

import java.util.UUID;

@Table(name = "notlib_players")
public class PlayerProfile {
    @Column(name = "id", primaryKey = true)
    private UUID uuid;

    @Column(name = "player_name")
    private String name;

    @Column(name = "level")
    private int level;

    public PlayerProfile() {} // needed for EntityRepository reflex

    public PlayerProfile(UUID uuid, String name, int level) {
        this.uuid = uuid;
        this.name = name;
        this.level = level;
    }

    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public int getLevel() { return level; }
}