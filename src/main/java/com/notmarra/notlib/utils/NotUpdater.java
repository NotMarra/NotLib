package com.notmarra.notlib.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class NotUpdater {
    private final Plugin plugin;
    private final String pluginName;
    private final String currentVersion;
    private final String pluginURL;
    private final String fetchURL;
    private final boolean autoDownload;
    private final boolean updateConfigs;
    private final File pluginFile;
    private final Set<String> configFilesToUpdate;

    public NotUpdater(Plugin plugin, String pluginName, String currentVersion, String pluginURL, String fetchURL) {
        this(plugin, pluginName, currentVersion, pluginURL, fetchURL, false, false);
    }

    public NotUpdater(Plugin plugin, String pluginName, String currentVersion, String pluginURL, String fetchURL, boolean autoDownload) {
        this(plugin, pluginName, currentVersion, pluginURL, fetchURL, autoDownload, false);
    }

    public NotUpdater(Plugin plugin, String pluginName, String currentVersion, String pluginURL, String fetchURL, boolean autoDownload, boolean updateConfigs) {
        this.plugin = plugin;
        this.pluginName = pluginName;
        this.currentVersion = currentVersion;
        this.pluginURL = pluginURL;
        this.fetchURL = fetchURL;
        this.autoDownload = autoDownload;
        this.updateConfigs = updateConfigs;
        this.pluginFile = getPluginFile();
        this.configFilesToUpdate = new HashSet<>(Arrays.asList(
            "config.yml", "messages.yml", "data.yml", "lang.yml", "settings.yml"
        ));
    }

    public void checkForUpdate() {
        if(currentVersion.contains("SNAPSHOT")) {
            plugin.getLogger().warning("You are running a snapshot build, skipping update check.");
            return;
        }
    }

    private void updateConfigurationFiles() {
        if (pluginFile == null) {
            plugin.getLogger().warning("Cannot update configs - plugin file not found.");
            return;
        }

        try {
            // Extract new config files from the updated JAR
            Map<String, String> newConfigs = extractConfigsFromJar(pluginFile);
            
            for (Map.Entry<String, String> entry : newConfigs.entrySet()) {
                String fileName = entry.getKey();
                String newContent = entry.getValue();
                
                updateConfigFile(fileName, newContent);
            }
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error updating configuration files: " + e.getMessage());
        }
    }

    private Map<String, String> extractConfigsFromJar(File jarFile) throws IOException {
        Map<String, String> configs = new HashMap<>();
        
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(jarFile))) {
            ZipEntry entry;
            
            while ((entry = zipIn.getNextEntry()) != null) {
                String entryName = entry.getName();
                
                // Check if this is a config file we want to update
                if (shouldUpdateFile(entryName)) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int length;
                    
                    while ((length = zipIn.read(buffer)) > 0) {
                        baos.write(buffer, 0, length);
                    }
                    
                    configs.put(entryName, baos.toString("UTF-8"));
                }
                zipIn.closeEntry();
            }
        }
        
        return configs;
    }

    private boolean shouldUpdateFile(String fileName) {
        // Check if file is in our update list
        return configFilesToUpdate.stream().anyMatch(fileName::endsWith);
    }

    private void updateConfigFile(String fileName, String newContent) {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        File configFile = new File(dataFolder, fileName);
        
        try {
            if (fileName.endsWith(".yml") || fileName.endsWith(".yaml")) {
                updateYamlConfig(configFile, newContent);
            } else {
                updateRegularFile(configFile, newContent);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to update " + fileName + ": " + e.getMessage());
        }
    }

    private void updateYamlConfig(File configFile, String newContent) throws IOException {
        try {
            YamlConfiguration newConfig = new YamlConfiguration();
            newConfig.loadFromString(newContent);
            
            if (configFile.exists()) {
                // Load existing config to preserve user settings
                YamlConfiguration existingConfig = YamlConfiguration.loadConfiguration(configFile);
                
                // Create backup
                File backupFile = new File(configFile.getParent(), configFile.getName() + ".backup");
                Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                plugin.getLogger().info("Created config backup: " + backupFile.getName());
                
                // Merge configurations
                YamlConfiguration mergedConfig = mergeConfigurations(existingConfig, newConfig);
                
                // Save merged config
                mergedConfig.save(configFile);
                plugin.getLogger().info("Updated " + configFile.getName() + " with new options while preserving existing settings.");
                
            } else {
                // New file, just save it
                newConfig.save(configFile);
                plugin.getLogger().info("Created new config file: " + configFile.getName());
            }
        } catch (InvalidConfigurationException e) {
            plugin.getLogger().warning("Invalid YAML in new config file " + configFile.getName() + ": " + e.getMessage());
            // Fallback to regular file update
            updateRegularFile(configFile, newContent);
        }
    }

    private YamlConfiguration mergeConfigurations(YamlConfiguration existing, YamlConfiguration newConfig) {
        YamlConfiguration merged = new YamlConfiguration();
        
        // Copy all existing values first
        for (String key : existing.getKeys(true)) {
            if (!existing.isConfigurationSection(key)) {
                merged.set(key, existing.get(key));
            }
        }
        
        // Add new values that don't exist in existing config
        for (String key : newConfig.getKeys(true)) {
            if (!newConfig.isConfigurationSection(key) && !existing.contains(key)) {
                merged.set(key, newConfig.get(key));
                plugin.getLogger().info("Added new config option: " + key + " = " + newConfig.get(key));
            }
        }
        
        return merged;
    }

    private void updateRegularFile(File file, String newContent) throws IOException {
        if (file.exists()) {
            // Create backup
            File backupFile = new File(file.getParent(), file.getName() + ".backup");
            Files.copy(file.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            plugin.getLogger().info("Created backup: " + backupFile.getName());
        }
        
        // Write new content
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(newContent);
        }
        
        plugin.getLogger().info("Updated file: " + file.getName());
    }

    // Method to add custom files to update list
    public void addConfigFileToUpdate(String fileName) {
        configFilesToUpdate.add(fileName);
    }

    // Method to remove files from update list  
    public void removeConfigFileFromUpdate(String fileName) {
        configFilesToUpdate.remove(fileName);
    }

    // Method to set custom config files to update
    public void setConfigFilesToUpdate(Set<String> fileNames) {
        configFilesToUpdate.clear();
        configFilesToUpdate.addAll(fileNames); 
        if (currentVersion.contains("DEV")) {
            plugin.getLogger().warning("You are running a development build, skipping update check.");
            return;
        }

        UpdateInfo updateInfo = getUpdateInfo();
        if (updateInfo == null) {
            plugin.getLogger().warning("Failed to check for updates, please check manually at " + pluginURL);
            return;
        }

        UpdateType updateType = compareVersions(currentVersion, updateInfo.version);
        
        switch (updateType) {
            case NONE:
                plugin.getLogger().info("You are running the latest version of " + pluginName + " (" + currentVersion + ")");
                break;
            case PATCH:
                plugin.getLogger().info("A minor update (patch) is available for " + pluginName + "! " + 
                    currentVersion + " -> " + updateInfo.version);
                handleUpdate(updateInfo, "patch update");
                break;
            case MINOR:
                plugin.getLogger().warning("A feature update is available for " + pluginName + "! " + 
                    currentVersion + " -> " + updateInfo.version);
                handleUpdate(updateInfo, "feature update");
                break;
            case MAJOR:
                plugin.getLogger().warning("A MAJOR update is available for " + pluginName + "! " + 
                    currentVersion + " -> " + updateInfo.version + " - This may contain breaking changes!");
                handleUpdate(updateInfo, "major update");
                break;
        }
    }

    private void handleUpdate(UpdateInfo updateInfo, String updateType) {
        if (autoDownload) {
            plugin.getLogger().info("Auto-downloading " + updateType + "...");
            if (downloadUpdate(updateInfo)) {
                plugin.getLogger().info("Update downloaded successfully!");
                
                if (updateConfigs) {
                    plugin.getLogger().info("Updating configuration files...");
                    updateConfigurationFiles();
                }
                
                plugin.getLogger().info("Restart the server to apply changes.");
            } else {
                plugin.getLogger().warning("Failed to download update. Please download manually at " + pluginURL);
            }
        } else {
            plugin.getLogger().info("You can download it at " + pluginURL + " or enable auto-download in the updater.");
        }
    }

    private UpdateInfo getUpdateInfo() {
        if (currentVersion.contains("DEV") || currentVersion.contains("SNAPSHOT")) {
            return null;
        }
        return fetchUpdateInfo();
    }

    private UpdateInfo fetchUpdateInfo() {
        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create(fetchURL).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            
            String response = getStringFromURL(connection);
            if (response.contains("\"message\":\"Not Found\"")) {
                return null;
            }

            // Parse JSON response
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(response);
            
            String tagName = (String) json.get("tag_name");
            String downloadUrl = null;
            
            // Get download URL from assets
            if (json.get("assets") != null) {
                org.json.simple.JSONArray assets = (org.json.simple.JSONArray) json.get("assets");
                if (!assets.isEmpty()) {
                    JSONObject asset = (JSONObject) assets.get(0);
                    downloadUrl = (String) asset.get("browser_download_url");
                }
            }
            
            // Remove 'v' prefix if present
            if (tagName != null && tagName.startsWith("v")) {
                tagName = tagName.substring(1);
            }
            
            return new UpdateInfo(tagName, downloadUrl);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error fetching update info: " + e.getMessage());
            return null;
        }
    }

    private boolean downloadUpdate(UpdateInfo updateInfo) {
        if (updateInfo.downloadUrl == null) {
            plugin.getLogger().warning("No download URL available for the update.");
            return false;
        }

        if (pluginFile == null) {
            plugin.getLogger().warning("Could not determine plugin file location for update.");
            return false;
        }

        try {
            plugin.getLogger().info("Downloading update from: " + updateInfo.downloadUrl);
            
            HttpURLConnection connection = (HttpURLConnection) URI.create(updateInfo.downloadUrl).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("Accept", "application/octet-stream");
            
            // Create temporary file
            File tempFile = new File(pluginFile.getParent(), pluginFile.getName() + ".tmp");
            
            // Download to temporary file
            try (InputStream inputStream = connection.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytes = 0;
                
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                }
                
                plugin.getLogger().info("Downloaded " + totalBytes + " bytes");
            }
            
            // Create backup of current plugin
            File backupFile = new File(pluginFile.getParent(), pluginFile.getName() + ".backup");
            Files.copy(pluginFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            plugin.getLogger().info("Created backup: " + backupFile.getName());
            
            // Replace current plugin with new version
            Files.move(tempFile.toPath(), pluginFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            plugin.getLogger().info("Plugin updated successfully!");
            
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().severe("Error downloading update: " + e.getMessage());
            return false;
        }
    }

    private File getPluginFile() {
        try {
            // Try to get the plugin file from the plugin's data folder parent
            File dataFolder = plugin.getDataFolder();
            if (dataFolder != null) {
                File pluginsFolder = dataFolder.getParentFile();
                if (pluginsFolder != null) {
                    File[] files = pluginsFolder.listFiles((dir, name) -> 
                        name.toLowerCase().endsWith(".jar") && name.contains(pluginName.toLowerCase()));
                    if (files != null && files.length > 0) {
                        return files[0];
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Could not determine plugin file: " + e.getMessage());
        }
        return null;
    }

    private UpdateType compareVersions(String current, String latest) {
        try {
            SemanticVersion currentVer = SemanticVersion.parse(current);
            SemanticVersion latestVer = SemanticVersion.parse(latest);
            
            if (latestVer.major > currentVer.major) {
                return UpdateType.MAJOR;
            } else if (latestVer.major == currentVer.major && latestVer.minor > currentVer.minor) {
                return UpdateType.MINOR;
            } else if (latestVer.major == currentVer.major && latestVer.minor == currentVer.minor && latestVer.patch > currentVer.patch) {
                return UpdateType.PATCH;
            } else {
                return UpdateType.NONE;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error comparing versions, falling back to string comparison: " + e.getMessage());
            // Fallback to simple string comparison
            if (!current.equals(latest)) {
                return UpdateType.MINOR; // Default to minor update
            }
            return UpdateType.NONE;
        }
    }

    @NotNull
    private static String getStringFromURL(HttpURLConnection connection) throws IOException {
        try (InputStream inputStream = connection.getInputStream();
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            return stringBuilder.toString();
        }
    }

    // Helper classes
    private static class UpdateInfo {
        final String version;
        final String downloadUrl;
        
        UpdateInfo(String version, String downloadUrl) {
            this.version = version;
            this.downloadUrl = downloadUrl;
        }
    }

    private enum UpdateType {
        NONE, PATCH, MINOR, MAJOR
    }

    private static class SemanticVersion {
        final int major;
        final int minor;
        final int patch;
        
        SemanticVersion(int major, int minor, int patch) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
        }
        
        static SemanticVersion parse(String version) {
            // Remove 'v' prefix if present
            if (version.startsWith("v")) {
                version = version.substring(1);
            }
            
            String[] parts = version.split("\\.");
            int major = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
            int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
            
            return new SemanticVersion(major, minor, patch);
        }
        
        @Override
        public String toString() {
            return major + "." + minor + "." + patch;
        }
    }
}