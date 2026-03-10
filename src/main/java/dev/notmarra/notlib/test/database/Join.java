package dev.notmarra.notlib.test.database;

import dev.notmarra.notlib.NotLib;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class Join implements Listener {
    private final DatabaseTest db;

    public Join(DatabaseTest db) {
        this.db = db;
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        db.testPlayer(e.getPlayer());
    }
}
