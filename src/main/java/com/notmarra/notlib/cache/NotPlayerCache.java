package com.notmarra.notlib.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.notmarra.notlib.extensions.NotPlugin;

public class NotPlayerCache extends BaseNotCache<Player> implements Listener {
    private final Map<UUID, Player> cachedPlayersByUUID = new HashMap<>();
    private final Map<String, Player> cachedPlayersByName = new HashMap<>();

    public NotPlayerCache(NotPlugin plugin) {
        super(plugin);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public String hash(Player source) {
        return source.getUniqueId().toString();
    }

    @Override
    public Player store(Player player) {
        super.store(player);
        
        cachedPlayersByUUID.put(player.getUniqueId(), player);
        cachedPlayersByName.put(player.getName().toLowerCase(), player);
        
        return player;
    }

    public NotPlayerCachePlayerResult connected(UUID playerUUID) {
        Player cachedPlayer = cachedPlayersByUUID.get(playerUUID);
        if (cachedPlayer != null) {
            if (cachedPlayer.isConnected()) {
                return new NotPlayerCachePlayerResult(cachedPlayer);
            } else {
                removeFromCache(cachedPlayer);
            }
        }

        Player player = plugin.getServer().getPlayer(playerUUID);
        if (player != null && player.isConnected()) {
            store(player);
            return new NotPlayerCachePlayerResult(player);
        }

        return new NotPlayerCachePlayerResult(null);
    }

    public NotPlayerCachePlayerResult connected(String playerName) {
        String lowerName = playerName.toLowerCase();
        
        Player cachedPlayer = cachedPlayersByName.get(lowerName);
        if (cachedPlayer != null) {
            if (cachedPlayer.isConnected()) {
                return new NotPlayerCachePlayerResult(cachedPlayer);
            } else {
                removeFromCache(cachedPlayer);
            }
        }

        Player player = plugin.getServer().getPlayer(playerName);
        if (player != null && player.isConnected()) {
            store(player);
            return new NotPlayerCachePlayerResult(player);
        }

        return new NotPlayerCachePlayerResult(null);
    }

    public NotPlayerCachePlayerResult connected(Player player) {
        if (player == null) {
            return new NotPlayerCachePlayerResult(null);
        }
        
        if (player.isConnected()) {
            store(player);
            return new NotPlayerCachePlayerResult(player);
        }
        
        return new NotPlayerCachePlayerResult(null);
    }

    private void removeFromCache(Player player) {
        if (player == null) return;
        
        remove(player);
        cachedPlayersByUUID.remove(player.getUniqueId());
        cachedPlayersByName.remove(player.getName().toLowerCase());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removeFromCache(event.getPlayer());
    }

    public int getCacheSize() {
        return cachedPlayersByUUID.size();
    }

    public void clearCache() {
        storage.clear();
        cachedPlayersByUUID.clear();
        cachedPlayersByName.clear();
    }

    public class NotPlayerCachePlayerResult {
        public final Player player;

        public NotPlayerCachePlayerResult(Player player) {
            this.player = player;
        }

        public void then(Consumer<Player> consumer) {
            if (player != null) {
                consumer.accept(player);
            }
        }

        public boolean isPresent() {
            return player != null;
        }

        public Player orElse(Player defaultPlayer) {
            return player != null ? player : defaultPlayer;
        }
    }
}