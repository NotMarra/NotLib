package com.notmarra.notlib.cache;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.notmarra.notlib.extensions.NotPlugin;

public class NotPlayerCache implements Listener {
    private final NotPlugin plugin;
    private final Map<UUID, Player> cacheByUUID = new ConcurrentHashMap<>();
    private final Map<String, UUID> nameToUUID = new ConcurrentHashMap<>();

    public NotPlayerCache(NotPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public NotPlayerCacheResult get(UUID uuid) {
        Player cached = cacheByUUID.get(uuid);

        if (cached != null && cached.isOnline()) {
            return new NotPlayerCacheResult(cached);
        }

        if (cached != null) {
            remove(uuid);
        }

        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null && player.isOnline()) {
            cache(player);
            return new NotPlayerCacheResult(player);
        }

        return new NotPlayerCacheResult(null);
    }

    public NotPlayerCacheResult get(String name) {
        UUID uuid = nameToUUID.get(name.toLowerCase());
        if (uuid != null) {
            NotPlayerCacheResult result = get(uuid);
            if (result.isPresent()) {
                return result;
            }
            nameToUUID.remove(name.toLowerCase());
        }

        Player player = plugin.getServer().getPlayer(name);
        if (player != null && player.isOnline()) {
            cache(player);
            return new NotPlayerCacheResult(player);
        }

        return new NotPlayerCacheResult(null);
    }

    public NotPlayerCacheResult get(Player player) {
        return get(player.getUniqueId());
    }

    public void cache(Player player) {
        cacheByUUID.put(player.getUniqueId(), player);
        nameToUUID.put(player.getName().toLowerCase(), player.getUniqueId());
    }

    public void remove(UUID uuid) {
        Player removed = cacheByUUID.remove(uuid);
        if (removed != null) {
            nameToUUID.remove(removed.getName().toLowerCase());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        remove(event.getPlayer().getUniqueId());
    }

    public void clear() {
        cacheByUUID.clear();
        nameToUUID.clear();
    }

    public int size() {
        return cacheByUUID.size();
    }

    public static class NotPlayerCacheResult {
        private final Player player;

        public NotPlayerCacheResult(Player player) {
            this.player = player;
        }

        public boolean isPresent() {
            return player != null;
        }

        public Player get() {
            return player;
        }

        public Optional<Player> toOptional() {
            return Optional.ofNullable(player);
        }

        public void then(Consumer<Player> consumer) {
            if (player != null) {
                consumer.accept(player);
            }
        }

        public NotPlayerCacheResult orElse(Runnable runnable) {
            if (player == null) {
                runnable.run();
            }
            return this;
        }
    }
}