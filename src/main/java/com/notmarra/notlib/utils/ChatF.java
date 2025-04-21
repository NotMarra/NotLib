package com.notmarra.notlib.utils;

import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.notmarra.notlib.NotLib;
import com.notmarra.notlib.utils.gui.NotGUIItem;


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
    public static final TextColor C_AQUA = TextColor.color(0, 255, 255);
    public static final TextColor C_LIGHTPURPLE = TextColor.color(255, 182, 193);
    public static final TextColor C_LIGHTBLUE = TextColor.color(173, 216, 230);
    public static final TextColor C_LIGHTRED = TextColor.color(255, 182, 193);

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    private final List<Component> appendComponents = new ArrayList<>();
    private final HashMap<String, Object> replacements = new HashMap<>();

    private Entity entity;
    private Entity targetEntity;

    public ChatF(Component baseComponent) {
        appendComponents.add(baseComponent);
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
        Component baseComponent = Component.empty();

        for (Component component : appendComponents) {
            baseComponent = baseComponent.append(component);
        }

        baseComponent = buildReplacements(baseComponent, replacements);

        if (NotLib.hasPlaceholderAPI() && entity instanceof Player player) {
            String message = miniMessage.serialize(baseComponent);
            String placeholded = PlaceholderAPI.setPlaceholders(player, message);
            baseComponent = miniMessage.deserialize(placeholded);
        } else {
            if (entity != null) {
                HashMap<String, Object> builtInReplacements = new HashMap<>();
                builtInReplacements.put("%player_name%", entity.getName());
                builtInReplacements.put("%player_x%", entity.getLocation().getBlockX());
                builtInReplacements.put("%player_y%", entity.getLocation().getBlockY());
                builtInReplacements.put("%player_z%", entity.getLocation().getBlockZ());
                baseComponent = buildReplacements(baseComponent, builtInReplacements);
            }
        }

        if (targetEntity != null) {
            HashMap<String, Object> builtInReplacements = new HashMap<>();
            builtInReplacements.put("%target_name%", targetEntity.getName());
            builtInReplacements.put("%target_x%", targetEntity.getLocation().getBlockX());
            builtInReplacements.put("%target_y%", targetEntity.getLocation().getBlockY());
            builtInReplacements.put("%target_z%", targetEntity.getLocation().getBlockZ());
            baseComponent = buildReplacements(baseComponent, builtInReplacements);
        }

        return baseComponent;
    }

    public ChatF withEntity(Entity entity) {
        this.entity = entity;
        return this;
    }

    public ChatF withTargetEntity(Entity targetEntity) {
        this.targetEntity = targetEntity;
        return this;
    }

    public ChatF appendMany(ChatF... formatters) {
        for (ChatF formatter : formatters) {
            appendComponents.add(formatter.build());
        }
        return this;
    }

    public ChatF appendMany(Component... components) {
        Collections.addAll(appendComponents, components);
        return this;
    }

    public ChatF appendMany(String... strings) {
        for (String string : strings) {
            appendComponents.add(Component.text(string));
        }
        return this;
    }

    public ChatF appendListChatF(List<ChatF> formatters) {
        for (ChatF formatter : formatters) {
            appendComponents.add(formatter.build());
        }
        return this;
    }

    public ChatF appendListComponent(List<Component> components) {
        appendComponents.addAll(components);
        return this;
    }

    public ChatF appendListString(List<String> strings) {
        for (String string : strings) {
            appendComponents.add(Component.text(string));
        }
        return this;
    }

    public ChatF nl() {
        appendComponents.add(Component.newline());
        return this;
    }

    public ChatF clickInfinite(ClickCallback<Audience> event) {
        if (appendComponents.isEmpty()) return this;
        Component last = appendComponents.removeLast();
        last = last.clickEvent(ClickEvent.callback(event, o -> o.uses(-1)));
        appendComponents.add(last);
        return this;
    }

    public ChatF clickWithOptions(int uses, @Nullable TemporalAmount duration, ClickCallback<Audience> event) {
        if (appendComponents.isEmpty()) return this;
        Component last = appendComponents.removeLast();
        last = last.clickEvent(ClickEvent.callback(event, o -> {
            o.uses(uses);
            if (duration != null) o.lifetime(duration);
        }));
        appendComponents.add(last);
        return this;
    }

    private ChatF _doAction(ClickEvent.Action action, String stuff) {
        if (appendComponents.isEmpty()) return this;
        Component last = appendComponents.removeLast();
        last = last.clickEvent(ClickEvent.clickEvent(action, stuff));
        appendComponents.add(last);
        return this;
    }

    public ChatF clickOpenUrl(String url) { return _doAction(ClickEvent.Action.OPEN_URL, url); }
    public ChatF clickCopyToClipboard(String value) { return _doAction(ClickEvent.Action.COPY_TO_CLIPBOARD, value); }

    public ChatF click(ClickCallback<Audience> event) {
        if (appendComponents.isEmpty()) return this;
        Component last = appendComponents.removeLast();
        last = last.clickEvent(ClickEvent.callback(event));
        appendComponents.add(last);
        return this;
    }

    public ChatF hoverItem(NotGUIItem item) {
        if (appendComponents.isEmpty()) return this;
        Component last = appendComponents.removeLast();
        last = last.hoverEvent(item.build().asHoverEvent());
        appendComponents.add(last);
        return this;
    }

    public ChatF hoverEntity(Entity entity) {
        if (appendComponents.isEmpty()) return this;
        Component last = appendComponents.removeLast();
        last = last.hoverEvent(entity.asHoverEvent());
        appendComponents.add(last);
        return this;
    }

    public ChatF hover(String component) { return hover(ChatF.of(component)); }
    public ChatF hover(ChatF component) { return hover(component.build()); }
    public ChatF hover(Component component) {
        if (appendComponents.isEmpty()) return this;
        Component last = appendComponents.removeLast();
        last = last.hoverEvent(HoverEvent.showText(component));
        appendComponents.add(last);
        return this;
    }

    public ChatF append(Object o) {
        if (o instanceof Component) {
            return append((Component)o);
        } else if (o instanceof ChatF) {
            return append(((ChatF)o).build());
        } else if (o instanceof String) {
            return append((String)o);
        } else {
            return append(o.toString());
        }
    }

    public ChatF append(ChatF formatter) {
        appendComponents.add(formatter.build());
        return this;
    }

    public ChatF append(Component component) {
        appendComponents.add(component);
        return this;
    }

    public ChatF append(Component component, TextColor color) {
        appendComponents.add(component.color(color));
        return this;
    }

    public ChatF append(Component component, Style style) {
        appendComponents.add(component.style(style));
        return this;
    }

    public ChatF append(String string) {
        appendComponents.add(toComponent(string));
        return this;
    }

    public ChatF append(String string, TextColor color) {
        appendComponents.add(toComponent(string, color));
        return this;
    }

    public ChatF append(String string, Style style) {
        appendComponents.add(toComponent(string, style));
        return this;
    }

    public ChatF appendBold(String string) {
        appendComponents.add(toComponentBold(string));
        return this;
    }

    public ChatF appendBold(String string, TextColor color) {
        appendComponents.add(toComponentBold(string, color));
        return this;
    }

    public ChatF replace(String key, Object value) {
        replacements.put(key, value);
        return this;
    }

    public static ChatF empty() {
        return new ChatF(Component.empty());
    }

    public static ChatF newline() {
        return new ChatF(Component.newline());
    }

    public static ChatF from(Object o) {
        if (o instanceof Component) {
            return ChatF.of((Component)o);
        } else if (o instanceof ChatF) {
            return ChatF.of(((ChatF)o).build());
        } else if (o instanceof String) {
            return ChatF.of((String)o);
        } else {
            return ChatF.of(o.toString());
        }
    }

    public static ChatF of(Component inputComponent) {
        return new ChatF(inputComponent);
    }

    public static ChatF of(Component inputComponent, TextColor color) {
        return new ChatF(inputComponent.color(color));
    }

    public static ChatF of(Component inputComponent, Style style) {
        return new ChatF(inputComponent.style(style));
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

    public void sendTo(Audience audience) {
        audience.sendMessage(build());
    }

    public void sendTo(Player player) {
        if (entity == null) entity = player;
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
