package com.notmarra.notlib.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.notmarra.notlib.NotLib;


public class ChatF {
    public static String K_MESSAGE = "%message%";

    public static final TextColor C_DODGERBLUE = TextColor.color(30, 144, 255);
    public static final TextColor C_RED = TextColor.color(255, 0, 0);
    public static final TextColor C_GREEN = TextColor.color(0, 255, 0);
    public static final TextColor C_BLUE = TextColor.color(0, 0, 255);
    public static final TextColor C_YELLOW = TextColor.color(255, 255, 0);
    public static final TextColor C_ORANGE = TextColor.color(255, 165, 0);
    public static final TextColor C_WHITE = TextColor.color(255, 255, 255);
    public static final TextColor C_BLACK = TextColor.color(0, 0, 0);
    public static final TextColor C_GRAY = TextColor.color(128, 128, 128);
    public static final TextColor C_DARKGRAY = TextColor.color(169, 169, 169);
    public static final TextColor C_LIGHTGRAY = TextColor.color(211, 211, 211);
    public static final TextColor C_PURPLE = TextColor.color(128, 0, 128);
    public static final TextColor C_PINK = TextColor.color(255, 192, 203);
    public static final TextColor C_CYAN = TextColor.color(0, 255, 255);
    public static final TextColor C_MAGENTA = TextColor.color(255, 0, 255);
    public static final TextColor C_LIME = TextColor.color(0, 255, 0);
    public static final TextColor C_BROWN = TextColor.color(165, 42, 42);
    public static final TextColor C_GOLD = TextColor.color(255, 215, 0);

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    private final Component baseComponent;
    private final List<Component> appendComponents = new ArrayList<>();
    private final HashMap<String, Object> replacements = new HashMap<>();

    private Player player;
    private Player targetPlayer;

    public ChatF(Component baseComponent) {
        this.baseComponent = baseComponent;
    }

    public String buildString() {
        return miniMessage.serialize(build());
    }

    private Component buildReplacements(Component baseComponent, HashMap<String, Object> replacements) {
        for (String key : replacements.keySet()) {
            Object value = replacements.get(key);

            TextReplacementConfig.Builder builder = TextReplacementConfig.builder().match(key);

            if (value instanceof ChatF formatted) {
                builder = builder.replacement(formatted.build());
            } else if (value instanceof Component component) {
                builder = builder.replacement(component);
            } else {
                builder = builder.replacement(value.toString());
            }
            
            baseComponent = baseComponent.replaceText(builder.build());
        }

        return baseComponent;
    }

    public Component build() {
        Component baseComponent = this.baseComponent;

        for (Component component : this.appendComponents) {
            baseComponent = baseComponent.append(component);
        }

        baseComponent = buildReplacements(baseComponent, this.replacements);

        if (NotLib.hasPlaceholderAPI()) {
            String message = miniMessage.serialize(baseComponent);
            String placeholded = PlaceholderAPI.setPlaceholders(player, message);
            baseComponent = miniMessage.deserialize(placeholded);
        } else {
            if (player != null) {
                HashMap<String, Object> builtInReplacements = new HashMap<>();
                builtInReplacements.put("%player_name%", player.getName());
                builtInReplacements.put("%player_x%", player.getLocation().getBlockX());
                builtInReplacements.put("%player_y%", player.getLocation().getBlockY());
                builtInReplacements.put("%player_z%", player.getLocation().getBlockZ());
                baseComponent = buildReplacements(baseComponent, builtInReplacements);
            }
        }

        if (targetPlayer != null) {
            HashMap<String, Object> builtInReplacements = new HashMap<>();
            builtInReplacements.put("%target_name%", targetPlayer.getName());
            builtInReplacements.put("%target_x%", targetPlayer.getLocation().getBlockX());
            builtInReplacements.put("%target_y%", targetPlayer.getLocation().getBlockY());
            builtInReplacements.put("%target_z%", targetPlayer.getLocation().getBlockZ());
            baseComponent = buildReplacements(baseComponent, builtInReplacements);
        }

        return baseComponent;
    }

    public ChatF withPlayer(Player player) {
        this.player = player;
        return this;
    }

    public ChatF withTargetPlayer(Player targetPlayer) {
        this.targetPlayer = targetPlayer;
        return this;
    }

    public ChatF appendMany(ChatF... formatters) {
        for (ChatF formatter : formatters) {
            this.appendComponents.add(formatter.build());
        }
        return this;
    }

    public ChatF appendMany(Component... components) {
        Collections.addAll(this.appendComponents, components);
        return this;
    }

    public ChatF appendMany(String... strings) {
        for (String string : strings) {
            this.appendComponents.add(Component.text(string));
        }
        return this;
    }

    public ChatF nl() {
        this.appendComponents.add(Component.newline());
        return this;
    }

    public ChatF append(ChatF formatter) {
        this.appendComponents.add(formatter.build());
        return this;
    }

    public ChatF append(Component component) {
        this.appendComponents.add(component);
        return this;
    }

    public ChatF append(String string) {
        this.appendComponents.add(toComponent(string));
        return this;
    }

    public ChatF append(String string, TextColor color) {
        this.appendComponents.add(toComponent(string, color));
        return this;
    }

    public ChatF append(String string, Style style) {
        this.appendComponents.add(toComponent(string, style));
        return this;
    }

    public ChatF appendBold(String string) {
        this.appendComponents.add(toComponentBold(string));
        return this;
    }

    public ChatF appendBold(String string, TextColor color) {
        this.appendComponents.add(toComponentBold(string, color));
        return this;
    }

    public ChatF replace(String key, Object value) {
        this.replacements.put(key, value);
        return this;
    }

    public static ChatF empty() {
        return new ChatF(Component.empty());
    }

    public static ChatF of(Component inputComponent) {
        return new ChatF(inputComponent);
    }

    public static ChatF of(String inputString) {
        return new ChatF(toComponent(inputString));
    }

    public static ChatF of(String inputString, TextColor color) {
        return new ChatF(toComponent(inputString, color));
    }

    public static ChatF of(String inputString, Style style) {
        return new ChatF(toComponent(inputString, style));
    }

    public static ChatF ofBold(String inputString) {
        return new ChatF(toComponentBold(inputString));
    }

    public static ChatF ofBold(String inputString, TextColor color) {
        return new ChatF(toComponentBold(inputString, color));
    }

    public static ChatF of(ChatF otherFormatter) {
        return new ChatF(otherFormatter.build());
    }

    public static Component toComponent(String inputString) {
        return miniMessage.deserialize(inputString);
    }

    public static Component toComponent(String inputString, TextColor color) {
        return toComponent(inputString).color(color);
    }

    public static Component toComponent(String inputString, Style style) {
        return toComponent(inputString).style(style);
    }

    public static Component toComponentBold(String inputString) {
        return toComponent(inputString).style(Style.style(TextDecoration.BOLD));
    }

    public static Component toComponentBold(String inputString, TextColor color) {
        return toComponent(inputString).style(Style.style(TextDecoration.BOLD)).color(color);
    }

    // shorthands

    public void sendTo(Player player) {
        if (this.player == null) {
            this.player = player;
        }

        player.sendMessage(build());
    }

    public void sendTo(Entity entity) {
        if (entity instanceof Player player) {
            sendTo(player);
        } else {
            entity.sendMessage(build());
        }
    }
}
