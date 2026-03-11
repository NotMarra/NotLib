package dev.notmarra.notlib.test.database;

import dev.notmarra.notlib.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public class Join implements Listener {
    private final DatabaseTest db;
    private final Plugin plugin;
    private final Scheduler scheduler;

    public Join(DatabaseTest db, Plugin plugin) {
        this.db = db;
        this.plugin = plugin;
        this.scheduler = new Scheduler(plugin);

        scheduler.asyncRepeating(() -> {
            db.logPlayerCount();
            db.logTopPlayer();
        }, 20L * 60 * 5, 20L * 60 * 5, TimeUnit.SECONDS);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        db.onJoin(e.getPlayer());

        db.logHighLevelPlayers();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        db.onQuit(e.getPlayer());
    }
}