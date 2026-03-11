package dev.notmarra.notlib.chat;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

public class Text {
    public static String K_MESSAGE = "%message%";

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    private final List<Component> appendComponents = new ArrayList<>();
    private final HashMap<String, Object> replacements = new HashMap<>();

    private Audience entity;
    private Audience targetEntity;

    public Text(Component baseComponent) {
        appendComponents.add(baseComponent);
    }

    public String buildString() {
        return miniMessage.serialize(build());
    }

    private Component buildReplacements(Component baseComponent, HashMap<String, Object> replacements) {
        for (String key : replacements.keySet()) {
            Object value = replacements.get(key);

            TextReplacementConfig.Builder builder = TextReplacementConfig.builder().match(key);

            if (value instanceof Text formatted) {
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
        /* TODO
        if (NotLib.hasPlaceholderAPI() && entity instanceof Player player) {
            String message = miniMessage.serialize(baseComponent);
            String placeholder = PlaceholderAPI.setPlaceholders(player, message);
            baseComponent = miniMessage.deserialize(placeholder);
        } else {
            if (entity != null) {
                HashMap<String, Object> builtInReplacements = new HashMap<>();
                if (entity instanceof Player player) {
                    builtInReplacements.put("%player_name%", player.getName());
                    builtInReplacements.put("%player_x%", player.getLocation().getBlockX());
                    builtInReplacements.put("%player_y%", player.getLocation().getBlockY());
                    builtInReplacements.put("%player_z%", player.getLocation().getBlockZ());
                }
                baseComponent = buildReplacements(baseComponent, builtInReplacements);
            }
        }*/

        if (targetEntity != null) {
            HashMap<String, Object> builtInReplacements = new HashMap<>();
            if (targetEntity instanceof Player player) {
                builtInReplacements.put("%target_name%", player.getName());
                builtInReplacements.put("%target_x%", player.getLocation().getBlockX());
                builtInReplacements.put("%target_y%", player.getLocation().getBlockY());
                builtInReplacements.put("%target_z%", player.getLocation().getBlockZ());
            }
            baseComponent = buildReplacements(baseComponent, builtInReplacements);
        }

        return baseComponent;
    }

    public Text withEntity(Entity entity) {
        this.entity = entity;
        return this;
    }

    public Text withTargetEntity(Entity targetEntity) {
        this.targetEntity = targetEntity;
        return this;
    }

    public Text appendMany(Text... formatters) {
        for (Text formatter : formatters) {
            appendComponents.add(formatter.build());
        }
        return this;
    }

    public Text appendMany(Component... components) {
        Collections.addAll(appendComponents, components);
        return this;
    }

    public Text appendMany(String... strings) {
        for (String string : strings) {
            appendComponents.add(Component.text(string));
        }
        return this;
    }

    public Text appendListMessage(List<Text> formatters) {
        for (Text formatter : formatters) {
            appendComponents.add(formatter.build());
        }
        return this;
    }

    public Text appendListComponent(List<Component> components) {
        appendComponents.addAll(components);
        return this;
    }

    public Text appendListString(List<String> strings) {
        for (String string : strings) append(string);
        return this;
    }

    public Text appendListString(List<String> strings, TextColor color) {
        for (String string : strings) append(string, color);
        return this;
    }

    public Text appendListString(List<String> strings, Style style) {
        for (String string : strings) append(string, style);
        return this;
    }

    public Text nl() {
        appendComponents.add(Component.newline());
        return this;
    }

    public Text clickInfinite(ClickCallback<Audience> event) {
        if (appendComponents.isEmpty()) return this;
        Component last = appendComponents.removeLast();
        last = last.clickEvent(ClickEvent.callback(event, o -> o.uses(-1)));
        appendComponents.add(last);
        return this;
    }

    public Text clickWithOptions(int uses, @Nullable TemporalAmount duration, ClickCallback<Audience> event) {
        if (appendComponents.isEmpty()) return this;
        Component last = appendComponents.removeLast();
        last = last.clickEvent(ClickEvent.callback(event, o -> {
            o.uses(uses);
            if (duration != null) o.lifetime(duration);
        }));
        appendComponents.add(last);
        return this;
    }

    private Text _doAction(ClickEvent.Action action, String stuff) {
        if (appendComponents.isEmpty()) return this;
        Component last = appendComponents.removeLast();
        last = last.clickEvent(ClickEvent.clickEvent(action, stuff));
        appendComponents.add(last);
        return this;
    }

    public Text clickOpenUrl(String url) { return _doAction(ClickEvent.Action.OPEN_URL, url); }
    public Text clickCopyToClipboard(String value) { return _doAction(ClickEvent.Action.COPY_TO_CLIPBOARD, value); }

    public Text click(ClickCallback<Audience> event) {
        if (appendComponents.isEmpty()) return this;
        Component last = appendComponents.removeLast();
        last = last.clickEvent(ClickEvent.callback(event));
        appendComponents.add(last);
        return this;
    }

    /*TODO
    public Message hoverItem(NotGUIItem item) {
        if (appendComponents.isEmpty()) return this;
        Component last = appendComponents.removeLast();
        last = last.hoverEvent(item.build().asHoverEvent());
        appendComponents.add(last);
        return this;
    }*/

    public Text hoverEntity(Entity entity) {
        if (appendComponents.isEmpty()) return this;
        Component last = appendComponents.removeLast();
        last = last.hoverEvent(entity.asHoverEvent());
        appendComponents.add(last);
        return this;
    }

    public Text hover(String component) { return hover(Text.of(component)); }
    public Text hover(Text component) { return hover(component.build()); }
    public Text hover(Component component) {
        if (appendComponents.isEmpty()) return this;
        Component last = appendComponents.removeLast();
        last = last.hoverEvent(HoverEvent.showText(component));
        appendComponents.add(last);
        return this;
    }

    public Text append(Object o) {
        if (o instanceof Component) {
            return append((Component)o);
        } else if (o instanceof Text) {
            return append(((Text)o).build());
        } else if (o instanceof String) {
            return append((String)o);
        } else {
            return append(o.toString());
        }
    }

    public Text append(Text formatter) {
        appendComponents.add(formatter.build());
        return this;
    }

    public Text append(Component component) {
        appendComponents.add(component);
        return this;
    }

    public Text append(Component component, TextColor color) {
        appendComponents.add(component.color(color));
        return this;
    }

    public Text append(Component component, Style style) {
        appendComponents.add(component.style(style));
        return this;
    }

    public Text append(String string) {
        appendComponents.add(toComponent(string));
        return this;
    }

    public Text append(String string, TextColor color) {
        appendComponents.add(toComponent(string, color));
        return this;
    }

    public Text append(String string, Style style) {
        appendComponents.add(toComponent(string, style));
        return this;
    }

    public Text append(Character ch) {
        appendComponents.add(toComponent(ch));
        return this;
    }

    public Text append(Character ch, TextColor color) {
        appendComponents.add(toComponent(ch, color));
        return this;
    }

    public Text append(Character ch, Style style) {
        appendComponents.add(toComponent(ch, style));
        return this;
    }

    public Text appendBold(String string) {
        appendComponents.add(toComponentBold(string));
        return this;
    }

    public Text appendBold(String string, TextColor color) {
        appendComponents.add(toComponentBold(string, color));
        return this;
    }

    public Text replace(String key, Object value) {
        replacements.put(key, value);
        return this;
    }

    public static Text empty() {
        return new Text(Component.empty());
    }

    public static Text newline() {
        return new Text(Component.newline());
    }

    public static Text from(Object o) {
        if (o instanceof Component) {
            return Text.of((Component)o);
        } else if (o instanceof Text) {
            return Text.of(((Text)o).build());
        } else if (o instanceof String) {
            return Text.of((String)o);
        } else {
            return Text.of(o.toString());
        }
    }

    public static Text of(Component inputComponent) {
        return new Text(inputComponent);
    }

    public static Text of(Component inputComponent, TextColor color) {
        return new Text(inputComponent.color(color));
    }

    public static Text of(Component inputComponent, Style style) {
        return new Text(inputComponent.style(style));
    }

    public static Text of(String inputString) {
        return new Text(toComponent(inputString));
    }

    public static Text of(String inputString, TextColor color) {
        return new Text(toComponent(inputString, color));
    }

    public static Text of(String inputString, Style style) {
        return new Text(toComponent(inputString, style));
    }

    public static Text of(Character inputCharacter) {
        return new Text(toComponent(inputCharacter));
    }

    public static Text of(Character inputCharacter, TextColor color) {
        return new Text(toComponent(inputCharacter, color));
    }

    public static Text of(Character inputCharacter, Style style) {
        return new Text(toComponent(inputCharacter, style));
    }

    public static Text ofBold(String inputString) {
        return new Text(toComponentBold(inputString));
    }

    public static Text ofBold(String inputString, TextColor color) {
        return new Text(toComponentBold(inputString, color));
    }

    public static Text of(Text otherFormatter) {
        return new Text(otherFormatter.build());
    }

    public static Component toComponent(String inputString) {
        return miniMessage.deserialize(convertLegacyColors(inputString));
    }

    public static Component toComponent(String inputString, TextColor color) {
        return toComponent(inputString).color(color);
    }

    public static Component toComponent(String inputString, Style style) {
        return toComponent(inputString).style(style);
    }

    public static Component toComponent(Character inputCharacter) {
        return miniMessage.deserialize(convertLegacyColors(String.valueOf(inputCharacter))).decoration(TextDecoration.ITALIC, false);
    }

    public static Component toComponent(Character inputCharacter, TextColor color) {
        return toComponent(inputCharacter).color(color);
    }

    public static Component toComponent(Character inputCharacter, Style style) {
        return toComponent(inputCharacter).style(style);
    }

    public static Component toComponentBold(String inputString) {
        return toComponent(inputString).style(Style.style(TextDecoration.BOLD));
    }

    public static Component toComponentBold(String inputString, TextColor color) {
        return toComponent(inputString).style(Style.style(TextDecoration.BOLD)).color(color);
    }

    public static String convertLegacyColors(String i) {
        if (i == null) return null;

        String result = i;

        result = convertLegacyGradients(result);
        result = convertLegacyHexColors(result);
        result = convertLegacyHexShort(result);
        result = convertLegacyBasicColors(result);

        return result;
    }

    private static String convertLegacyGradients(String i) {
        Matcher matcher = LegacyPatterns.LEGACY_GRADIENT.matcher(i);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String startColor = matcher.group(1);
            String middleColor = matcher.group(2);
            String endColor = matcher.group(3);

            middleColor = convertColorNameToHex(middleColor);
            endColor = convertColorNameToHex(endColor);

            String replacement = String.format("<gradient:#%s:%s:%s>", startColor, middleColor, endColor);
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private static String convertLegacyHexColors(String i) {
        Matcher matcher = LegacyPatterns.LEGACY_HEX.matcher(i);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String hexCode = matcher.group(0);
            String hex = hexCode.replaceAll("&[x]?", "");
            String replacement = "<color:#" + hex + ">";
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private static String convertLegacyHexShort(String i) {
        Matcher matcher = LegacyPatterns.LEGACY_HEX_SHORT.matcher(i);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            String replacement = "<color:#" + hex + ">";
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private static String convertLegacyBasicColors(String input) {
        return input
                .replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")

                .replace("&l", "<b>")
                .replace("&n", "<u>")
                .replace("&m", "<st>")
                .replace("&o", "<i>")
                .replace("&k", "<obf>")
                .replace("&r", "<reset>");
    }

    private static String convertColorNameToHex(String colorName) {
        return switch (colorName.toLowerCase()) {
            case "black" -> "000000";
            case "dark_blue" -> "0000AA";
            case "dark_green" -> "00AA00";
            case "dark_aqua" -> "00AAAA";
            case "dark_red" -> "AA0000";
            case "dark_purple" -> "AA00AA";
            case "gold" -> "FFAA00";
            case "gray" -> "AAAAAA";
            case "dark_gray" -> "555555";
            case "blue" -> "5555FF";
            case "green" -> "55FF55";
            case "aqua" -> "55FFFF";
            case "red" -> "FF5555";
            case "light_purple" -> "FF55FF";
            case "yellow" -> "FFFF55";
            case "white" -> "FFFFFF";
            default -> colorName;
        };
    }

    public static Text ofLegacy(String inputString) {
        return new Text(toComponent(convertLegacyColors(inputString)));
    }

    public static Text ofLegacy(String inputString, TextColor color) {
        return new Text(toComponent(convertLegacyColors(inputString), color));
    }

    public static Text ofLegacy(String inputString, Style style) {
        return new Text(toComponent(convertLegacyColors(inputString), style));
    }

    public Text appendLegacy(String string) {
        appendComponents.add(toComponent(convertLegacyColors(string)));
        return this;
    }

    public Text appendLegacy(String string, TextColor color) {
        appendComponents.add(toComponent(convertLegacyColors(string), color));
        return this;
    }

    public Text appendLegacy(String string, Style style) {
        appendComponents.add(toComponent(convertLegacyColors(string), style));
        return this;
    }

    public static Component toComponentWithLegacy(String inputString) {
        return toComponent(convertLegacyColors(inputString));
    }

    // shorthands

    public void sendTo(Audience audience) {
        if (entity == null) entity = audience;
        audience.sendMessage(build());
    }
}