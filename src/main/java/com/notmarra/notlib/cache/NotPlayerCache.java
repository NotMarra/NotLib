package com.notmarra.notlib.cache;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.notmarra.notlib.extensions.NotPlugin;

public class NotPlayerCache extends BaseNotCache<Player> implements Listener {
    private final Map<String, UUID> nameToUUID = new ConcurrentHashMap<>();

    public NotPlayerCache(NotPlugin plugin) {
        super(plugin);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public String hash(Player source) {
        return source.getUniqueId().toString();
    }

    public NotPlayerCacheResult connected(UUID uuid) {
        String hash = uuid.toString();
        Player cached = get(hash);

        if (cached != null && cached.isOnline()) {
            return new NotPlayerCacheResult(cached);
        }

        if (cached != null) {
            remove(hash);
            nameToUUID.remove(cached.getName().toLowerCase());
        }

        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null && player.isOnline()) {
            store(player);
            nameToUUID.put(player.getName().toLowerCase(), uuid);
            return new NotPlayerCacheResult(player);
        }

        return new NotPlayerCacheResult(null);
    }

    public NotPlayerCacheResult connected(String name) {
        UUID uuid = nameToUUID.get(name.toLowerCase());
        if (uuid != null) {
            NotPlayerCacheResult result = connected(uuid);
            if (result.isPresent()) {
                return result;
            }
            nameToUUID.remove(name.toLowerCase());
        }

        Player player = plugin.getServer().getPlayer(name);
        if (player != null && player.isOnline()) {
            store(player);
            nameToUUID.put(name.toLowerCase(), player.getUniqueId());
            return new NotPlayerCacheResult(player);
        }

        return new NotPlayerCacheResult(null);
    }

    public NotPlayerCacheResult connected(Player player) {
        return connected(player.getUniqueId());
    }

    @Override
    public Player store(Player player) {
        nameToUUID.put(player.getName().toLowerCase(), player.getUniqueId());
        return super.store(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        remove(player);
        nameToUUID.remove(player.getName().toLowerCase());
    }

    @Override
    public void clear() {
        super.clear();
        nameToUUID.clear();
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