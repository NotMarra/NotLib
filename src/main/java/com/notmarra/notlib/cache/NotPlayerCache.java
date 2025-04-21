package com.notmarra.notlib.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.entity.Player;

import com.notmarra.notlib.extensions.NotPlugin;

public class NotPlayerCache extends BaseNotCache<Player> {
    public final Map<UUID, Player> cachedPlayersByUUID = new HashMap<>();
    public final Map<String, Player> cachedPlayersByName = new HashMap<>();

    public NotPlayerCache(NotPlugin plugin) {
        super(plugin);
    }

    @Override
    public String hash(Player source) {
        return source.getUniqueId().toString();
    }

    public NotPlayerCachePlayerResult connected(UUID playerUUID) {
        if (cachedPlayersByUUID.containsKey(playerUUID)) {
            if (cachedPlayersByUUID.get(playerUUID).isConnected()) {
                return new NotPlayerCachePlayerResult(cachedPlayersByUUID.get(playerUUID));
            } else {
                cachedPlayersByUUID.remove(playerUUID);
            }
        } else {
            Player player = plugin.getServer().getPlayer(playerUUID);
            if (player != null && player.isConnected()) {
                cachedPlayersByUUID.put(playerUUID, player);
                return new NotPlayerCachePlayerResult(player);
            }
        }
        return new NotPlayerCachePlayerResult(null);
    }

    public NotPlayerCachePlayerResult connected(String playerName) {
        if (cachedPlayersByName.containsKey(playerName)) {
            if (cachedPlayersByName.get(playerName).isConnected()) {
                return new NotPlayerCachePlayerResult(cachedPlayersByName.get(playerName));
            } else {
                cachedPlayersByName.remove(playerName);
            }
        } else {
            Player player = plugin.getServer().getPlayer(playerName);
            if (player != null && player.isConnected()) {
                cachedPlayersByName.put(playerName, player);
                return new NotPlayerCachePlayerResult(player);
            }
        }
        return new NotPlayerCachePlayerResult(null);
    }

    public NotPlayerCachePlayerResult connected(Player player) {
        return connected(player.getUniqueId());
    }

    public class NotPlayerCachePlayerResult {
        public final Player player;

        public NotPlayerCachePlayerResult(Player player) {
            this.player = player;
        }

        public void then(Consumer<Player> consumer) {
            if (player == null) return;
            consumer.accept(player);
        }
    }
}
