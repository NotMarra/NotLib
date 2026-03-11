package dev.notmarra.notlib.test.database;

import dev.notmarra.notlib.database.Database;
import dev.notmarra.notlib.database.repository.EntityRepository;
import dev.notmarra.notlib.database.type.SQLite;
import dev.notmarra.notlib.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class DatabaseTest {
    private final Plugin plugin;
    private final SQLite sqlite;
    private final EntityRepository<PlayerProfile> repo;
    private final Scheduler scheduler;

    public DatabaseTest(Plugin plugin) {
        this.plugin = plugin;
        this.sqlite = new SQLite().setup(Database.generateProperties(plugin.getDataFolder(), "data"));
        this.repo = new EntityRepository<>(sqlite, PlayerProfile.class);
        this.repo.createTable();
        this.scheduler = new Scheduler(plugin);
    }

    public void testPlayer(Player player) {
        PlayerProfile profile = new PlayerProfile(player.getUniqueId(), player.getName(), 1);

        repo.insertAsync(profile)
                .thenCompose(v -> repo.findByIdAsync(player.getUniqueId()))
                .thenAccept(result -> result.ifPresent(p -> {
                    player.sendMessage("Tvůj level: " + p.getLevel());
                }));
    }

    public void close() {
        sqlite.close();
    }
}