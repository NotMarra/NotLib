package com.notmarra.notlib.extensions;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

public class NotTranslationManager extends NotConfigurable {
    private NotLangId currentLang = NotLangId.EN;
    private List<NotLangId> registeredLangs = new ArrayList<>();
    private String defaultLangFolder = "lang";
    private NotLangId defaultLang = NotLangId.EN;

    public NotTranslationManager(NotPlugin plugin) {
        super(plugin);
        registerConfigurable();
        registerLang(NotLangId.EN);
    }

    public NotTranslationManager setDefaultLangFolder(String folder) { this.defaultLangFolder = folder; return this; }

    public NotTranslationManager setDefaultLang(NotLangId lang) { this.defaultLang = lang; return this; }

    public NotTranslationManager registerLang(NotLangId lang) {
        if (lang == null) return this;
        if (registeredLangs.contains(lang)) return this;
        registeredLangs.add(lang);
        return this;
    }

    private String getLangConfigPath(NotLangId lang) { return defaultLangFolder + "/" + lang.getLangCode() + ".yml"; }

    @Override
    public List<String> getConfigPaths() { return registeredLangs.stream().map(lang -> getLangConfigPath(lang)).toList(); }

    @Override
    public void onConfigReload(List<String> reloadedConfigs) {
        String newLang = getPluginConfig().getString("lang", defaultLang.getLangCode());
        currentLang = NotLangId.fromCode(newLang);
    }

    private FileConfiguration getTranslationConfig() {
        FileConfiguration tConfig = getConfig(getLangConfigPath(currentLang));
        if (tConfig == null) tConfig = getConfig(getLangConfigPath(defaultLang));
        return tConfig;
    }

    private String getTranslationString(String key) {
        String dbgString = "t(" + key + ")?";
        FileConfiguration tConfig = getTranslationConfig();
        if (tConfig == null) return dbgString;
        return tConfig.getString(key, dbgString);
    }

    private List<String> getTranslationListString(String key) {
        List<String> dbgString = List.of("t[" + key + "]?");
        FileConfiguration tConfig = getTranslationConfig();
        if (tConfig == null) return dbgString;
        List<String> stringList = tConfig.getStringList(key);
        if (stringList.isEmpty()) return dbgString;
        return stringList;
    }

    public String get(String key) { return getTranslationString(key); }
    public List<String> getList(String key) { return getTranslationListString(key); }
}