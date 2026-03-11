package dev.notmarra.notlib.test.database;

import dev.notmarra.notlib.database.Database;
import dev.notmarra.notlib.database.repository.EntityRepository;
import dev.notmarra.notlib.database.repository.SortOrder;
import dev.notmarra.notlib.database.type.MariaDB;
import dev.notmarra.notlib.database.type.SQLite;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.logging.Logger;

public class DatabaseTest {
    private final Plugin plugin;
    private final SQLite sqlite;
    private final MariaDB maria;
    private final EntityRepository<PlayerProfile> repo;
    private final Logger log;

    public DatabaseTest(Plugin plugin) {
        this.plugin = plugin;
        this.log = plugin.getLogger();
        this.sqlite = new SQLite().setup(Database.generateProperties(plugin.getDataFolder(), "data"));
        this.maria = new MariaDB().setup(Database.generateProperties("", "", "", "3306", "" ));
        this.repo = new EntityRepository<>(sqlite, PlayerProfile.class);
        this.repo.createTable();
    }

    // ── JOIN ─────────────────────────────────────────────────────────────────

    public void onJoin(Player player) {
        repo.existsAsync(player.getUniqueId())
                .thenAccept(exists -> {
                    if (!exists) {
                        repo.insertAsync(new PlayerProfile(player.getUniqueId(), player.getName(), 1));
                    }
                });
    }

    // ── QUIT ─────────────────────────────────────────────────────────────────

    public void onQuit(Player player) {
        repo.findByIdAsync(player.getUniqueId())
                .thenAccept(result -> result.ifPresent(p -> {
                    PlayerProfile updated = new PlayerProfile(p.getUuid(), p.getName(), p.getLevel() + 1);
                    repo.updateAsync(updated)
                            .thenRun(() -> log.info("[DB Test] " + player.getName() + " left, new level: " + updated.getLevel()));
                }));
    }

    // ── QUERY TESTY ──────────────────────────────────────────────────────────

    public void logAllByLevel() {
        repo.query()
                .orderBy("level", SortOrder.DESC)
                .findAllAsync()
                .thenAccept(players -> {
                    log.info("[DB Test] All players by level:");
                    players.forEach(p -> log.info("  " + p.getName() + " -> level " + p.getLevel()));
                });
    }

    public void logHighLevelPlayers() {
        repo.query()
                .where("level", ">=", 3)
                .orderBy("level", SortOrder.DESC)
                .findAllAsync()
                .thenAccept(players -> {
                    log.info("[DB Test] Player with level >= 3 (" + players.size() + "):");
                    players.forEach(p -> log.info("  " + p.getName() + " -> level " + p.getLevel()));
                });
    }

    public void logPlayerCount() {
        repo.query()
                .countAsync()
                .thenAccept(count -> log.info("[DB Test] Players in DB: " + count));
    }

    public void logTopPlayer() {
        repo.query()
                .orderBy("level", SortOrder.DESC)
                .findFirstAsync()
                .thenAccept(result -> result.ifPresentOrElse(
                        p -> log.info("[DB Test] Top player: " + p.getName() + " (level " + p.getLevel() + ")"),
                        () -> log.info("[DB Test] No player in DB")
                ));
    }

    public void insertTestProfiles() {
        List<PlayerProfile> testProfiles = List.of(
                new PlayerProfile(java.util.UUID.randomUUID(), "NPC_Alpha", 5),
                new PlayerProfile(java.util.UUID.randomUUID(), "NPC_Beta", 10),
                new PlayerProfile(java.util.UUID.randomUUID(), "NPC_Gamma", 15)
        );

        repo.insertAllAsync(testProfiles)
                .thenRun(() -> log.info("[DB Test] Inserted " + testProfiles.size() + " test profiles"))
                .thenRun(this::logAllByLevel);
    }

    // ── CLOSE ────────────────────────────────────────────────────────────────

    public void close() {
        sqlite.close();
    }
}