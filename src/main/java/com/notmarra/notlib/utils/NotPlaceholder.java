package com.notmarra.notlib.utils;

import org.bukkit.entity.Player;

import com.notmarra.notlib.extensions.NotPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class NotPlaceholder extends PlaceholderExpansion {
    private NotPlugin plugin;

    public NotPlaceholder(NotPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist() {
        return true;
    }

    public boolean canRegister() {
        return true;
    }

    public String getAuthor() {
        return plugin.getPluginMeta().getAuthors().toString();
    }

    public String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public String getIdentifier() {
        throw new UnsupportedOperationException("Unimplemented method 'getIdentifier'");
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        throw new UnsupportedOperationException("Unimplemented method 'onPlaceholderRequest'");
    }
}
