package dev.notmarra.notlib.test.database;

import dev.notmarra.notlib.database.Database;
import dev.notmarra.notlib.database.repository.EntityRepository;
import dev.notmarra.notlib.database.type.SQLite;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class DatabaseTest {
    private final Plugin plugin;
    private final SQLite sqlite;
    private final EntityRepository<PlayerProfile> repo;

    public DatabaseTest(Plugin plugin) {
        this.plugin = plugin;
        this.sqlite = new SQLite().setup(Database.generateProperties(plugin.getDataFolder(), "data"));
        this.repo = new EntityRepository<>(sqlite, PlayerProfile.class);
        this.repo.createTable();
    }

    public void testPlayer(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerProfile profile = new PlayerProfile(player.getUniqueId(), player.getName(), 1);
            repo.insert(profile);

            repo.findById(player.getUniqueId()).ifPresent(p -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage("Tvůj level: " + p.getLevel());
                });
            });
        });
    }

    public void close() {
        sqlite.close();
    }
}