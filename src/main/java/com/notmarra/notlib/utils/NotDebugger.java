package com.notmarra.notlib.utils;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.notmarra.notlib.cache.NotCache;
import com.notmarra.notlib.extensions.NotConfigurable;
import com.notmarra.notlib.extensions.NotPlugin;

public class NotDebugger extends NotConfigurable {
    public static final String C_DEBUG = "DEBUG";
    public static final String C_INFO = "INFO";
    public static final String C_SUCCESS = "SUCCESS";
    public static final String C_WARN = "WARN";
    public static final String C_ERROR = "ERROR";
    public static final String C_FATAL = "FATAL";
    public static final String C_PLAYER = "PLAYER";
    public static final String C_COMMAND = "COMMAND";
    public static final String C_EVENT = "EVENT";
    public static final String C_PERMISSION = "PERMISSION";
    public static final String C_CONFIG = "CONFIG";
    public static final String C_METRICS = "METRICS";
    public static final String C_DATABASE = "DATABASE";
    public static final String C_DEV = "DEV";

    public boolean globalEnabled = false;
    public File globalOutFile = null;
    public HashMap<String, NotDebuggerCategory> categories = new HashMap<>();

    public NotDebugger(NotPlugin plugin) {
        super(plugin);
        registerConfigurable();
    }

    @Override
    public List<String> getConfigPaths() {
        return List.of(plugin.CONFIG_YML);
    }

    public File getLogFile(String subPath) { return plugin.getFile("logs/" + subPath); }

    @Override
    public void onConfigReload(List<String> reloadedConfigs) {
        categories.clear();

        ConfigurationSection debugger = getFirstConfig().getConfigurationSection("debug");
        if (debugger != null) {
            ConfigurationSection globalSection = debugger.getConfigurationSection("global");
            if (globalSection != null) {
                globalEnabled = globalSection.getBoolean("enabled", false);
                if (globalSection.isString("file")) {
                    globalOutFile = getLogFile(globalSection.getString("file"));
                }
            }

            ConfigurationSection categoriesSection = debugger.getConfigurationSection("categories");
            if (categoriesSection != null) {
                for (String key : categoriesSection.getKeys(false)) {
                    ConfigurationSection section = categoriesSection.getConfigurationSection(key);
                    if (section != null) {
                        categories.put(key, new NotDebuggerCategory(section));
                    }
                }
            }
        }
    }

    public NotDebuggerCategory getCategory(String name) {
        NotDebuggerCategory category = categories.get(name);
        return category != null ? category : NotDebuggerCategory.empty();
    }

    public boolean isEnabled(String name) { return getCategory(name).isEnabled(); }
    public String getColor(String name) { return getCategory(name).getColor(); }
    public boolean isConsole(String name) { return getCategory(name).isConsole(); }
    public List<String> forChat(String name) { return getCategory(name).forChat(); }

    private void send(NotDebuggerCategory category, ChatF message) {
        if (category.isConsole()) {
            plugin.getComponentLogger().info(message.build());
        }

        for (String playerName : category.forChat()) {
            NotCache.player().connected(playerName).then(player -> {
                player.sendMessage(message.withEntity(player).build());
            });
        }
    }

    private void writeLog(File file, ChatF message) {
        try {
            if (!file.exists()) {
                File parent = file.getParentFile();
                if (parent != null && !parent.exists()) parent.mkdirs();
                file.createNewFile();
            }
            if (file.canWrite()) (new FileWriter(file, true))
                .append(message.buildString())
                .append(System.lineSeparator())
                .close();
        } catch (Exception e) {}
    }

    private void save(NotDebuggerCategory category, ChatF message) {
        if (globalOutFile != null) writeLog(globalOutFile, message);
        if (category.file() != null) writeLog(getLogFile(category.file()), message);
    }

    public void log(String name, Object message) { log(getCategory(name), message); }
    public void log(NotDebuggerCategory category, Object message) {
        if (!globalEnabled) return;
        if (!category.isEnabled()) return;

        ChatF format = ChatF.empty()
            .append("<" + category.getColor() + ">")
            .append(message);

        send(category, format);
        save(category, format);
    }

    public static class NotDebuggerCategory {
        private final ConfigurationSection section;

        public static final String N_ENABLED = "enabled";
        public static final boolean DEF_ENABLED = false;
        public static final String N_COLOR = "color";
        public static final String DEF_COLOR = "#ffffff";
        public static final String N_CONSOLE = "console";
        public static final boolean DEF_CONSOLE = false;
        public static final String N_CHAT = "chat";
        public static final List<String> DEF_CHAT = List.of();
        public static final String N_FILE = "file";

        public NotDebuggerCategory(ConfigurationSection section) {
            this.section = section;
        }

        public boolean isEnabled() { return this.section.getBoolean(N_ENABLED, DEF_ENABLED); }
        public String getColor() { return this.section.getString(N_COLOR, DEF_COLOR); }
        public boolean isConsole() { return this.section.getBoolean(N_CONSOLE, DEF_CONSOLE); }
        public List<String> forChat() { return this.section.getStringList(N_CHAT); }
        public String file() { return this.section.getString(N_FILE); }

        public static NotDebuggerCategory empty() {
            return new NotDebuggerCategory(new YamlConfiguration());
        }
    }
}
