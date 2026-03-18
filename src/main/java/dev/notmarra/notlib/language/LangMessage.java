package dev.notmarra.notlib.language;

import dev.notmarra.notlib.chat.Text;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A lazy, fluent message builder returned by {@link LanguageManager#get(String)}
 * and {@link LanguageManager#getFor(String, String)}.
 *
 * <p>The raw string is fetched from the active locale's YAML file and processed
 * (prefix injected, placeholders replaced, legacy colors and MiniMessage parsed)
 * only when {@link #build()} or one of the send methods is called.
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * lang.get("player.join")
 *     .withPlayer(player)
 *     .with("%server%", "Survival")
 *     .sendTo(player);
 *
 * // Per-player locale:
 * lang.getFor(player, "error.no_permission")
 *     .sendTo(player);
 * }</pre>
 */
public class LangMessage {

    private final LanguageManager manager;
    private final String key;

    /**
     * Locale to resolve against. {@code null} means use the manager's default locale.
     * Set by {@link LanguageManager#getFor}.
     */
    private final String locale;

    private final Map<String, Object> replacements = new LinkedHashMap<>();

    private Entity senderEntity;
    private Entity targetEntity;

    /** Default-locale constructor. */
    LangMessage(LanguageManager manager, String key) {
        this(manager, key, null);
    }

    /** Per-locale constructor. */
    LangMessage(LanguageManager manager, String key, String locale) {
        this.manager = manager;
        this.key     = key;
        this.locale  = locale;
    }

    // ── REPLACEMENTS ─────────────────────────────────────────────────────────

    /**
     * Adds a placeholder replacement.
     *
     * <p>The value can be a {@link String}, {@link Text}, {@link Component} or any
     * object whose {@code toString()} will be used.
     *
     * @param placeholder e.g. {@code "%player%"}
     * @param value       replacement value
     */
    public LangMessage with(String placeholder, Object value) {
        replacements.put(placeholder, value);
        return this;
    }

    /**
     * Convenience overload – replaces {@code %player%} with the player's display name
     * and registers built-in player placeholders ({@code %player_x%} etc.).
     */
    public LangMessage withPlayer(Player player) {
        replacements.put("%player%", player.getName());
        replacements.put("%player_name%", player.getName());
        replacements.put("%player_display%", player.getDisplayName());
        replacements.put("%player_x%", player.getLocation().getBlockX());
        replacements.put("%player_y%", player.getLocation().getBlockY());
        replacements.put("%player_z%", player.getLocation().getBlockZ());
        replacements.put("%player_world%", player.getWorld().getName());
        replacements.put("%player_health%", (int) player.getHealth());
        replacements.put("%player_level%", player.getLevel());
        this.senderEntity = player;
        return this;
    }

    /**
     * Convenience overload – registers {@code %target%} and built-in target placeholders.
     */
    public LangMessage withTarget(Player target) {
        replacements.put("%target%", target.getName());
        replacements.put("%target_name%", target.getName());
        replacements.put("%target_display%", target.getDisplayName());
        replacements.put("%target_x%", target.getLocation().getBlockX());
        replacements.put("%target_y%", target.getLocation().getBlockY());
        replacements.put("%target_z%", target.getLocation().getBlockZ());
        replacements.put("%target_world%", target.getWorld().getName());
        this.targetEntity = target;
        return this;
    }

    // ── BUILD ─────────────────────────────────────────────────────────────────

    /**
     * Resolves the message and returns a fully built {@link Text} ready to send.
     */
    public Text build() {
        String raw = locale == null
                ? manager.resolve(key)
                : manager.resolveIn(key, locale);

        Text text = Text.of(raw);

        for (Map.Entry<String, Object> entry : replacements.entrySet()) {
            text.replace(entry.getKey(), entry.getValue());
        }

        if (senderEntity != null) text.withEntity(senderEntity);
        if (targetEntity  != null) text.withTargetEntity(targetEntity);

        return text;
    }

    /** Shortcut — returns the Adventure {@link Component} directly. */
    public Component buildComponent() {
        return build().build();
    }

    // ── SEND ─────────────────────────────────────────────────────────────────

    /** Builds and sends the message to {@code audience}. */
    public void sendTo(Audience audience) {
        build().sendTo(audience);
    }

    /** Builds once and sends to every audience in the iterable. */
    public void sendToAll(Iterable<? extends Audience> audiences) {
        Component built = buildComponent();
        for (Audience a : audiences) a.sendMessage(built);
    }

    /** Broadcasts the message to the entire server. */
    public void broadcast() {
        manager.getPlugin().getServer().broadcast(buildComponent());
    }
}