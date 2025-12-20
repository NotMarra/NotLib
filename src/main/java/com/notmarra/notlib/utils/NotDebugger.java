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

    public File getLogFile(String subPath) {
        return plugin.getFile("logs/" + subPath);
    }

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
                        categories.put(key, new NotDebuggerCategory(key, section));
                    }
                }
            }
        }
    }

    public NotDebuggerCategory getCategory(String name) {
        NotDebuggerCategory category = categories.get(name);
        return category != null ? category : NotDebuggerCategory.empty(name);
    }

    public boolean isEnabled(String name) {
        return getCategory(name).isEnabled();
    }

    public boolean isConsole(String name) {
        return getCategory(name).isConsole();
    }

    public List<String> forChat(String name) {
        return getCategory(name).forChat();
    }

    private void send(NotDebuggerCategory category, ChatF message) {
        if (category.isConsole()) {
            sendConsoleLog(category, message);
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
                if (parent != null && !parent.exists())
                    parent.mkdirs();
                file.createNewFile();
            }
            if (file.canWrite()) {
                FileWriter writer = new FileWriter(file, true);
                writer.append("[" + java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "] ");
                writer.append(message.buildString());
                writer.append(System.lineSeparator());
                writer.close();
            }
        } catch (Exception e) {
        }
    }

    private void save(NotDebuggerCategory category, ChatF message) {
        if (globalOutFile != null)
            writeLog(globalOutFile, message);
        if (category.file() != null)
            writeLog(getLogFile(category.file()), message);
    }

    public void log(String name, Object message) {
        log(getCategory(name), message);
    }

    public void log(NotDebuggerCategory category, Object message) {
        if (!globalEnabled)
            return;
        if (!category.isEnabled())
            return;

        ChatF format = ChatF
                .from(message);

        send(category, format);
        save(category, format);
    }

    private void sendConsoleLog(NotDebuggerCategory category, Object message) {
        String m = ChatF.from(message).buildString();
        if (category.name.equals(C_WARN)) {
            plugin.getLogger().warning(m);
            return;
        }
        if (category.name.equals(C_ERROR) || category.name.equals(C_FATAL)) {
            plugin.getLogger().severe(m);
            return;
        }
        plugin.getLogger().info(m);
    }

    public static class NotDebuggerCategory {
        public final String name;
        private final ConfigurationSection section;

        public static final String N_ENABLED = "enabled";
        public static final boolean DEF_ENABLED = false;
        public static final String N_CONSOLE = "console";
        public static final boolean DEF_CONSOLE = false;
        public static final String N_CHAT = "chat";
        public static final List<String> DEF_CHAT = List.of();
        public static final String N_FILE = "file";

        public NotDebuggerCategory(String name, ConfigurationSection section) {
            this.name = name;
            this.section = section;
        }

        public boolean isEnabled() {
            return this.section.getBoolean(N_ENABLED, DEF_ENABLED);
        }

        public boolean isConsole() {
            return this.section.getBoolean(N_CONSOLE, DEF_CONSOLE);
        }

        public List<String> forChat() {
            return this.section.getStringList(N_CHAT);
        }

        public String file() {
            return this.section.getString(N_FILE);
        }

        public static NotDebuggerCategory empty(String name) {
            return new NotDebuggerCategory(name, new YamlConfiguration());
        }
    }
}
