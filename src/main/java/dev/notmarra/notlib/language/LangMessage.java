package dev.notmarra.notlib.language;

import dev.notmarra.notlib.chat.Text;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;

public class LangMessage {

    private final LanguageManager manager;
    private final String key;

    private final Map<String, Object> replacements = new LinkedHashMap<>();
    private Entity senderEntity;

    LangMessage(LanguageManager manager, String key) {
        this.manager = manager;
        this.key     = key;
    }

    public LangMessage with(String placeholder, Object value) {
        replacements.put(placeholder, value);
        return this;
    }

    public LangMessage withPlayer(Player player) {
        replacements.put("%player%",         player.getName());
        replacements.put("%player_name%",    player.getName());
        replacements.put("%player_display%", player.getDisplayName());
        replacements.put("%player_x%",       player.getLocation().getBlockX());
        replacements.put("%player_y%",       player.getLocation().getBlockY());
        replacements.put("%player_z%",       player.getLocation().getBlockZ());
        replacements.put("%player_world%",   player.getWorld().getName());
        replacements.put("%player_health%",  (int) player.getHealth());
        replacements.put("%player_level%",   player.getLevel());
        this.senderEntity = player;
        return this;
    }

    public Text build() {
        Text text = Text.of(manager.resolve(key));
        replacements.forEach(text::replace);
        if (senderEntity != null) text.withEntity(senderEntity);
        return text;
    }

    public Component buildComponent() {
        return build().build();
    }

    public void sendTo(Audience audience) {
        build().sendTo(audience);
    }

    public void sendToAll(Iterable<? extends Audience> audiences) {
        Component built = buildComponent();
        for (Audience a : audiences) a.sendMessage(built);
    }

    public void broadcast() {
        manager.getPlugin().getServer().broadcast(buildComponent());
    }
}