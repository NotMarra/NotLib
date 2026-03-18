package dev.notmarra.notlib.test.database;

import dev.notmarra.notlib.cache.*;
import dev.notmarra.notlib.database.Database;
import dev.notmarra.notlib.database.type.SQLite;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Demonstrates all three write strategies for CachedRepository.
 *
 * Strategy quick reference:
 *
 *   WRITE_THROUGH  – every write goes to DB immediately + updates cache.
 *                    Best for: consistency-critical data.
 *
 *   WRITE_BEHIND   – writes land in cache first, flushed to DB every N seconds.
 *                    Best for: high-frequency updates (e.g. XP, playtime).
 *
 *   READ_THROUGH   – cache populated on read; writes always go direct to DB.
 *                    Best for: mostly-read, rarely-written data.
 */
public class CachedRepositoryTest {

    private final Plugin plugin;
    private final Logger log;

    // ── WRITE_THROUGH repo (safe, consistent) ────────────────────────────────
    private final CachedRepository<UUID, PlayerProfile> profileRepo;

    // ── WRITE_BEHIND repo (high-frequency stats) ──────────────────────────────
    private final CachedRepository<UUID, PlayerProfile> statsRepo;

    public CachedRepositoryTest(Plugin plugin) {
        this.plugin = plugin;
        this.log = plugin.getLogger();

        SQLite sqlite = new SQLite().setup(
                Database.generateProperties(plugin.getDataFolder(), "cached_data")
        );

        // ── WRITE_THROUGH ─────────────────────────────────────────────────────
        profileRepo = CachedRepository.<UUID, PlayerProfile>builder(sqlite, PlayerProfile.class)
                .writeStrategy(WriteStrategy.WRITE_THROUGH)
                .cache(NotCache.<UUID, PlayerProfile>builder()
                        .maxSize(200)
                        .ttlMinutes(15)
                        .evictionPolicy(CacheEvictionPolicy.LRU)
                        .build())
                .build();
        profileRepo.createTable();

        // ── WRITE_BEHIND ──────────────────────────────────────────────────────
        statsRepo = CachedRepository.<UUID, PlayerProfile>builder(sqlite, PlayerProfile.class)
                .writeStrategy(WriteStrategy.WRITE_BEHIND)
                .flushIntervalSeconds(30)          // flush to DB every 30 s
                .cache(NotCache.<UUID, PlayerProfile>builder()
                        .maxSize(500)
                        .ttlMinutes(30)
                        .evictionPolicy(CacheEvictionPolicy.LRU)
                        .cleanupIntervalMillis(60_000)
                        .build())
                .build();
        statsRepo.createTable();
    }

    // ── JOIN ─────────────────────────────────────────────────────────────────

    public void onJoin(Player player) {
        UUID id = player.getUniqueId();

        profileRepo.existsAsync(id).thenAccept(exists -> {
            if (!exists) {
                PlayerProfile newProfile = new PlayerProfile(id, player.getName(), 1);
                profileRepo.insertAsync(newProfile)
                        .thenRun(() -> log.info("[Cache] New player inserted: " + player.getName()));
            } else {
                // Warm up cache eagerly on join
                profileRepo.findByIdAsync(id)
                        .thenAccept(opt -> opt.ifPresent(p ->
                                log.info("[Cache] Loaded from " + (profileRepo.getCacheStats().hits() > 0
                                        ? "cache" : "DB") + ": " + p.getName())));
            }
        });
    }

    // ── QUIT ─────────────────────────────────────────────────────────────────

    public void onQuit(Player player) {
        UUID id = player.getUniqueId();

        // findById hits cache first
        profileRepo.findByIdAsync(id).thenAccept(opt -> opt.ifPresent(p -> {
            PlayerProfile updated = new PlayerProfile(p.getUuid(), p.getName(), p.getLevel() + 1);
            // WRITE_THROUGH: goes to DB + cache immediately
            profileRepo.updateAsync(updated)
                    .thenRun(() -> log.info("[Cache] " + player.getName() + " saved. Level: " + updated.getLevel()));
        }));

        // Invalidate after quit (player is offline, no need to keep in memory)
        profileRepo.invalidate(id);
    }

    // ── HIGH-FREQUENCY STAT UPDATE (WRITE_BEHIND) ────────────────────────────

    public void onBlockBreak(Player player) {
        UUID id = player.getUniqueId();
        // Hits cache, no DB call – perfect for frequent events
        statsRepo.findByIdAsync(id).thenAccept(opt -> opt.ifPresent(p -> {
            PlayerProfile updated = new PlayerProfile(p.getUuid(), p.getName(), p.getLevel() + 1);
            statsRepo.upsert(updated); // lands in cache, flushed to DB every 30 s
        }));
    }

    // ── COMPLEX QUERY (bypasses cache) ───────────────────────────────────────

    public void logTopPlayersByLevel() {
        profileRepo.query()
                .where("level", ">=", 5)
                .orderBy("level", dev.notmarra.notlib.database.repository.SortOrder.DESC)
                .limit(10)
                .findAllAsync()
                .thenAccept(players -> {
                    log.info("[Cache] Top players (fetched from DB):");
                    players.forEach(p -> log.info("  " + p.getName() + " lv." + p.getLevel()));
                    // Optionally warm cache with results
                    profileRepo.cacheAll(players);
                });
    }

    // ── BULK INSERT TEST ──────────────────────────────────────────────────────

    public void insertTestData() {
        List<PlayerProfile> fakes = List.of(
                new PlayerProfile(UUID.randomUUID(), "Alpha", 5),
                new PlayerProfile(UUID.randomUUID(), "Beta", 10),
                new PlayerProfile(UUID.randomUUID(), "Gamma", 20)
        );
        profileRepo.insertAllAsync(fakes)
                .thenRun(() -> log.info("[Cache] Inserted " + fakes.size() + " test profiles. Stats: "
                        + profileRepo.getCacheStats()));
    }

    // ── STATS ─────────────────────────────────────────────────────────────────

    public void logCacheStats() {
        CacheStats stats = profileRepo.getCacheStats();
        log.info("[Cache] " + stats);
    }

    // ── SHUTDOWN ─────────────────────────────────────────────────────────────

    /**
     * Must be called on plugin disable.
     * WRITE_BEHIND repos flush all pending writes before closing.
     */
    public void close() {
        profileRepo.close();
        statsRepo.close(); // triggers final flush of WRITE_BEHIND entries
        log.info("[Cache] All repositories closed cleanly.");
    }
}