package com.notmarra.notlib.extensions;

import java.io.File;
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

    public NotTranslationManager setDefaultLangFolder(String folder) {
        this.defaultLangFolder = folder;
        return this;
    }

    public NotTranslationManager setDefaultLang(NotLangId lang) {
        this.defaultLang = lang;
        return this;
    }

    public NotTranslationManager registerLangs(NotLangId... langs) {
        List.of(langs).forEach(lang -> registerLang(lang));
        return reloadLangFiles();
    }

    public NotTranslationManager registerLang(NotLangId lang) {
        if (lang == null)
            return this;
        if (registeredLangs.contains(lang))
            return this;
        registeredLangs.add(lang);
        reloadLangFile(getLangConfigPath(lang));
        return this;
    }

    public boolean isLanguageRegistered(NotLangId lang) {
        if (lang == null)
            return false;
        return registeredLangs.contains(lang);
    }

    public boolean isLanguageCodeRegistered(String langCode) {
        if (langCode == null)
            return false;
        return registeredLangs.stream().anyMatch(lang -> lang.getLangCode().equalsIgnoreCase(langCode));
    }

    public void setLanguage(NotLangId lang) {
        if (lang == null)
            return;
        if (!registeredLangs.contains(lang))
            return;
        currentLang = lang;
    }

    public void setLanguage(String langCode) {
        if (langCode == null)
            return;
        NotLangId lang = registeredLangs.stream().filter(l -> l.getLangCode().equalsIgnoreCase(langCode)).findFirst()
                .orElse(null);
        if (lang == null)
            return;
        currentLang = lang;
    }

    public List<NotLangId> getRegisteredLanguages() {
        return registeredLangs;
    }

    public List<String> getRegisteredLanguageCodes() {
        return registeredLangs.stream().map(lang -> lang.getLangCode()).toList();
    }

    public List<String> getRegisteredLanguagePaths() {
        return registeredLangs.stream().map(lang -> getLangConfigPath(lang)).toList();
    }

    public String getLangConfigPath(NotLangId lang) {
        return defaultLangFolder + "/" + lang.getLangCode() + ".yml";
    }

    @Override
    public List<String> getConfigPaths() {
        List<String> configPaths = new ArrayList<>();
        configPaths.add(plugin.CONFIG_YML);
        configPaths.addAll(getRegisteredLanguagePaths());
        return configPaths;
    }

    public NotTranslationManager reloadLangFile(String path) {
        if (path == null)
            return this;
        if (!getRegisteredLanguagePaths().contains(path))
            return this;
        plugin.reloadConfig(path);
        onConfigReload(List.of(path));
        return this;
    }

    public NotTranslationManager reloadLangFiles(List<String> paths) {
        paths.forEach(path -> plugin.reloadConfig(path));
        onConfigReload(paths);
        return this;
    }

    public NotTranslationManager reloadLangFiles() {
        return reloadLangFiles(getRegisteredLanguagePaths());
    }

    @Override
    public void onConfigReload(List<String> reloadedConfigs) {
        String newLang = getPluginConfig().getString("lang", defaultLang.getLangCode());
        currentLang = NotLangId.fromCode(newLang);
    }

    public FileConfiguration getTranslationConfig() {
        FileConfiguration tConfig = getConfig(getLangConfigPath(currentLang));
        if (tConfig == null)
            tConfig = getConfig(getLangConfigPath(defaultLang));
        return tConfig;
    }

    public String get(String key) {
        String dbgString = "t(" + key + ")?";
        FileConfiguration tConfig = getTranslationConfig();
        if (tConfig == null)
            return dbgString;
        return tConfig.getString(key, dbgString);
    }

    public List<String> getList(String key) {
        List<String> dbgString = List.of("t[" + key + "]?");
        FileConfiguration tConfig = getTranslationConfig();
        if (tConfig == null)
            return dbgString;
        List<String> stringList = tConfig.getStringList(key);
        if (stringList.isEmpty())
            return dbgString;
        return stringList;
    }

    public void discoverAndRegisterLocalLangs() {
        List<String> jarResources = plugin.listResources(defaultLangFolder + "/");
        boolean hasResources = !jarResources.isEmpty();

        File langFolder = new File(plugin.getDataFolder(), defaultLangFolder);

        if (!hasResources && !langFolder.exists()) {
            return;
        }

        if (hasResources && !langFolder.exists()) {
            langFolder.mkdirs();
            if (plugin.getResource(defaultLangFolder + "/en.yml") != null) {
                plugin.saveResource(defaultLangFolder + "/en.yml", false);
            }
        }

        File[] files = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                plugin.registerConfigurable(this, defaultLangFolder + "/" + file.getName());
            }
        }
    }
}