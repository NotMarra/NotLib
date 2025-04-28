package com.notmarra.notlib.font;

import org.bukkit.Material;

import com.notmarra.notlib.utils.ChatF;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public class NotFont {
    public static Character item(Material material) {
        Character item = NotFontItem.fromMaterial(material);
        return item != null ? item :'?';
    }

    public static Character block(Material material) {
        Character block = NotFontBlock.fromMaterial(material);
        return block != null ? block :'?';
    }

    public static Character any(Material material) {
        Character item = NotFontItem.fromMaterial(material);
        if (item != null) return item;
        Character block = NotFontBlock.fromMaterial(material);
        return block != null ? block :'?';
    }

    public static Component anyAsComponent(Material material) {
        Component c = Component.text(any(material), ChatF.C_WHITE);
        c.decoration(TextDecoration.ITALIC, false);
        return c;
    }
}
