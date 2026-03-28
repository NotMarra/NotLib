package dev.notmarra.notlib.chat;

import dev.notmarra.notlib.gui.GUIItem;
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
import org.intellij.lang.annotations.RegExp;

import javax.annotation.Nullable;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Fluent builder for creating Adventure {@link Component} messages.
 * <p>
 * This class supports appending values, placeholder replacements, legacy color conversion,
 * and click/hover interactions on the most recently appended component.
 */
public class Text {
    public static String K_MESSAGE;

    static {
        K_MESSAGE = "%message%";
    }

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    private final List<Component> appendComponents = new ArrayList<>();
    private final HashMap<String, Object> replacements = new HashMap<>();

    private Audience entity;
    private Audience targetEntity;

    /**
     * Creates a new text builder with the given base component.
     *
     * @param baseComponent initial component to add
     */
    public Text(Component baseComponent) {
        appendComponents.add(baseComponent);
    }

    /**
     * Builds the final component and serializes it as MiniMessage.
     *
     * @return built text in MiniMessage format
     */
    public String buildString() {
        return miniMessage.serialize(build());
    }

    /**
     * Applies regex-based replacements to a component.
     *
     * @param baseComponent component to transform
     * @param replacements map of regex keys to replacement values
     * @return transformed component
     */
    private Component buildReplacements(Component baseComponent, HashMap<String, Object> replacements) {
        for (@RegExp String key : replacements.keySet()) {
            Object value = replacements.get(key);

            TextReplacementConfig.Builder builder = TextReplacementConfig.builder()
                    .match(key)
                    .replacement(toComponent(value));

            baseComponent = baseComponent.replaceText(builder.build());
        }

        return baseComponent;
    }

    /**
     * Builds the final component from appended parts and all replacements.
     *
     * @return final component
     */
    public Component build() {
        Component baseComponent = Component.empty();

        for (Component component : appendComponents) {
            baseComponent = baseComponent.append(component);
        }

        baseComponent = buildReplacements(baseComponent, replacements);
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

    /**
     * Sets the source entity used by built-in {@code %player_*%} replacements.
     *
     * @param entity source entity
     * @return this builder
     */
    public Text withEntity(Entity entity) {
        this.entity = entity;
        return this;
    }

    /**
     * Sets the target entity used by built-in {@code %target_*%} replacements.
     *
     * @param targetEntity target entity
     * @return this builder
     */
    public Text withTargetEntity(Entity targetEntity) {
        this.targetEntity = targetEntity;
        return this;
    }

    /**
     * Appends multiple values in order.
     *
     * @param items values to append
     * @return this builder
     */
    public Text appendMany(Object... items) {
        for (Object item : items) append(item);
        return this;
    }

    /**
     * Appends all values from a list.
     *
     * @param items values to append
     * @return this builder
     */
    public Text appendList(List<?> items) {
        for (Object item : items) append(item);
        return this;
    }

    /**
     * Appends all values from a list with the same color.
     *
     * @param items values to append
     * @param color color applied to each appended value
     * @return this builder
     */
    public Text appendList(List<?> items, TextColor color) {
        for (Object item : items) append(item, color);
        return this;
    }

    /**
     * Appends all values from a list with the same style.
     *
     * @param items values to append
     * @param style style applied to each appended value
     * @return this builder
     */
    public Text appendList(List<?> items, Style style) {
        for (Object item : items) append(item, style);
        return this;
    }

    /**
     * Appends a newline component.
     *
     * @return this builder
     */
    public Text nl() {
        appendComponents.add(Component.newline());
        return this;
    }

    /**
     * Adds an unlimited-use click callback to the last appended component.
     *
     * @param event callback to execute on click
     * @return this builder
     */
    public Text clickInfinite(ClickCallback<Audience> event) {
        if (appendComponents.isEmpty()) return this;
        Component last = appendComponents.removeLast();
        last = last.clickEvent(ClickEvent.callback(event, o -> o.uses(-1)));
        appendComponents.add(last);
        return this;
    }

    /**
     * Adds a click callback with usage and lifetime options to the last component.
     *
     * @param uses number of allowed uses (-1 for unlimited)
     * @param duration optional callback lifetime
     * @param event callback to execute on click
     * @return this builder
     */
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

    /**
     * Internal helper that applies a click action to the last appended component.
     *
     * @param action click action
     * @param stuff action payload
     * @return this builder
     */
    private Text _doAction(ClickEvent.Action action, String stuff) {
        if (appendComponents.isEmpty()) return this;
        Component last = appendComponents.removeLast();
        last = last.clickEvent(ClickEvent.clickEvent(action, ClickEvent.Payload.string(stuff)));
        appendComponents.add(last);
        return this;
    }

    /**
     * Sets an OPEN_URL click action on the last appended component.
     *
     * @param url URL to open
     * @return this builder
     */
    public Text clickOpenUrl(String url) { return _doAction(ClickEvent.Action.OPEN_URL, url); }

    /**
     * Sets a COPY_TO_CLIPBOARD click action on the last appended component.
     *
     * @param value text to copy
     * @return this builder
     */
    public Text clickCopyToClipboard(String value) { return _doAction(ClickEvent.Action.COPY_TO_CLIPBOARD, value); }

    /**
     * Adds a click callback to the last appended component.
     *
     * @param event callback to execute on click
     * @return this builder
     */
    public Text click(ClickCallback<Audience> event) {
        if (appendComponents.isEmpty()) return this;
        Component last = appendComponents.removeLast();
        last = last.clickEvent(ClickEvent.callback(event));
        appendComponents.add(last);
        return this;
    }

    /**
     * Adds an item hover event to the last appended component.
     *
     * @param item item shown on hover
     * @return this builder
     */
    public Text hoverItem(GUIItem item) {
        if (appendComponents.isEmpty()) return this;
        Component last = appendComponents.removeLast();
        last = last.hoverEvent(item.build().asHoverEvent());
        appendComponents.add(last);
        return this;
    }

    /**
     * Adds an entity hover event to the last appended component.
     *
     * @param entity entity shown on hover
     * @return this builder
     */
    public Text hoverEntity(Entity entity) {
        if (appendComponents.isEmpty()) return this;
        Component last = appendComponents.removeLast();
        last = last.hoverEvent(entity.asHoverEvent());
        appendComponents.add(last);
        return this;
    }

    /**
     * Adds a hover text from a string to the last appended component.
     *
     * @param component hover text
     * @return this builder
     */
    public Text hover(String component) { return hover(Text.of(component)); }

    /**
     * Adds a hover text from another {@link Text} instance to the last component.
     *
     * @param component hover text builder
     * @return this builder
     */
    public Text hover(Text component) { return hover(component.build()); }

    /**
     * Adds a hover text component to the last appended component.
     *
     * @param component hover component
     * @return this builder
     */
    public Text hover(Component component) {
        if (appendComponents.isEmpty()) return this;
        Component last = appendComponents.removeLast();
        last = last.hoverEvent(HoverEvent.showText(component));
        appendComponents.add(last);
        return this;
    }

    /**
     * Appends a value converted to a component.
     *
     * @param o value to append
     * @return this builder
     */
    public Text append(Object o) {
        appendComponents.add(toComponent(o));
        return this;
    }

    /**
     * Appends a value with a specific color.
     *
     * @param o value to append
     * @param color color to apply
     * @return this builder
     */
    public Text append(Object o, TextColor color) {
        appendComponents.add(toComponent(o).color(color));
        return this;
    }

    /**
     * Appends a value with a specific style.
     *
     * @param o value to append
     * @param style style to apply
     * @return this builder
     */
    public Text append(Object o, Style style) {
        appendComponents.add(toComponent(o).style(style));
        return this;
    }

    /**
     * Appends a value in bold.
     *
     * @param o value to append
     * @return this builder
     */
    public Text appendBold(Object o) {
        appendComponents.add(toComponent(o).style(Style.style(TextDecoration.BOLD)));
        return this;
    }

    /**
     * Appends a value in bold with a specific color.
     *
     * @param o value to append
     * @param color color to apply
     * @return this builder
     */
    public Text appendBold(Object o, TextColor color) {
        appendComponents.add(toComponent(o).style(Style.style(TextDecoration.BOLD)).color(color));
        return this;
    }

    /**
     * Adds a replacement entry where key is treated as a regex.
     *
     * @param key regex key to match
     * @param value replacement value
     * @return this builder
     */
    public Text replace(String key, Object value) {
        replacements.put(key, value);
        return this;
    }

    /**
     * Creates an empty text builder.
     *
     * @return new empty {@link Text}
     */
    public static Text empty() {
        return new Text(Component.empty());
    }

    /**
     * Creates a text builder that starts with a newline component.
     *
     * @return new {@link Text} containing one newline
     */
    public static Text newline() {
        return new Text(Component.newline());
    }

    /**
     * Creates a text builder from any value.
     *
     * @param o source value
     * @return new {@link Text}
     */
    public static Text of(Object o) {
        return new Text(toComponent(o));
    }

    /**
     * Creates a text builder from any value with a color.
     *
     * @param o source value
     * @param color color to apply
     * @return new {@link Text}
     */
    public static Text of(Object o, TextColor color) {
        return new Text(toComponent(o, color));
    }

    /**
     * Creates a text builder from any value with a style.
     *
     * @param o source value
     * @param style style to apply
     * @return new {@link Text}
     */
    public static Text of(Object o, Style style) {
        return new Text(toComponent(o, style));
    }

    /**
     * Creates a text builder from any value in bold.
     *
     * @param o source value
     * @return new {@link Text}
     */
    public static Text ofBold(Object o) {
        return new Text(toComponentBold(o));
    }

    /**
     * Creates a text builder from any value in bold with a color.
     *
     * @param o source value
     * @param color color to apply
     * @return new {@link Text}
     */
    public static Text ofBold(Object o, TextColor color) { return new Text(toComponentBold(o, color)); }

    /**
     * Converts an arbitrary value to an Adventure component.
     *
     * @param o value to convert
     * @return converted component
     */
    public static Component toComponent(Object o) {
        return switch (o) {
            case Component c -> c;
            case Text t -> t.build();
            case Character ch -> miniMessage.deserialize(convertLegacyColors(String.valueOf(ch)))
                    .decoration(TextDecoration.ITALIC, false);
            case String s -> miniMessage.deserialize(convertLegacyColors(s));
            default -> miniMessage.deserialize(convertLegacyColors(o.toString()));
        };
    }

    /**
     * Converts an arbitrary value to a component and applies a color.
     *
     * @param o value to convert
     * @param color color to apply
     * @return converted component
     */
    public static Component toComponent(Object o, TextColor color) {
        return toComponent(o).color(color);
    }

    /**
     * Converts an arbitrary value to a component and applies a style.
     *
     * @param o value to convert
     * @param style style to apply
     * @return converted component
     */
    public static Component toComponent(Object o, Style style) {
        return toComponent(o).style(style);
    }

    /**
     * Converts an arbitrary value to a bold component.
     *
     * @param o value to convert
     * @return converted component
     */
    public static Component toComponentBold(Object o) {
        return toComponent(o).style(Style.style(TextDecoration.BOLD));
    }

    /**
     * Converts an arbitrary value to a bold component with color.
     *
     * @param o value to convert
     * @param color color to apply
     * @return converted component
     */
    public static Component toComponentBold(Object o, TextColor color) {
        return toComponentBold(o).color(color);
    }

    /**
     * Converts supported legacy color syntaxes into MiniMessage tags.
     *
     * @param i input text, may be {@code null}
     * @return converted text, or {@code null} when input is {@code null}
     */
    public static String convertLegacyColors(String i) {
        if (i == null) return null;

        String result = i;

        result = convertLegacyGradients(result);
        result = convertLegacyHexColors(result);
        result = convertLegacyHexShort(result);
        result = convertLegacyBasicColors(result);

        return result;
    }

    /**
     * Converts legacy gradient syntax into MiniMessage gradient tags.
     *
     * @param i input text
     * @return converted text
     */
    private static String convertLegacyGradients(String i) {
        Matcher matcher = LegacyPatterns.LEGACY_GRADIENT.matcher(i);
        StringBuilder sb = new StringBuilder();

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

    /**
     * Converts long legacy hex syntax (for example, {@code &x&F&F&A&A&0&0}).
     *
     * @param i input text
     * @return converted text
     */
    private static String convertLegacyHexColors(String i) {
        Matcher matcher = LegacyPatterns.LEGACY_HEX.matcher(i);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String hexCode = matcher.group(0);
            String hex = hexCode.replaceAll("&x?", "");
            String replacement = "<color:#" + hex + ">";
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * Converts short legacy hex syntax (for example, {@code &#FFAA00}).
     *
     * @param i input text
     * @return converted text
     */
    private static String convertLegacyHexShort(String i) {
        Matcher matcher = LegacyPatterns.LEGACY_HEX_SHORT.matcher(i);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String hex = matcher.group(1);
            String replacement = "<color:#" + hex + ">";
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * Converts basic legacy color and formatting codes into MiniMessage tags.
     *
     * @param input input text
     * @return converted text
     */
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

    /**
     * Converts a named vanilla color to its hex value.
     *
     * @param colorName color name
     * @return hex value without {@code #}, or original input if unknown
     */
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

    // shorthands

    /**
     * Builds this message and sends it to the given audience.
     *
     * @param audience recipient audience
     */
    public void sendTo(Audience audience) {
        if (entity == null) entity = audience;
        audience.sendMessage(build());
    }
}