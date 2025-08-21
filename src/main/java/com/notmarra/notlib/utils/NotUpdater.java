package com.notmarra.notlib.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.notmarra.notlib.NotLib;
import com.notmarra.notlib.extensions.NotPlugin;

public class NotUpdater {
    private final NotPlugin plugin;
    private final String pluginName;
    private final String currentVersion;
    private final String pluginURL;
    private final String fetchURL;

    public NotUpdater(NotPlugin plugin, String pluginURL, String fetchURL) {
        this.plugin = plugin;
        this.pluginName = plugin.getPluginMeta().getName();
        this.currentVersion = plugin.getPluginMeta().getVersion();
        this.pluginURL = pluginURL;
        this.fetchURL = fetchURL;
    }

    public static void check(NotPlugin plugin, String pluginURL, String fetchURL) {
        NotUpdater updater = new NotUpdater(plugin, pluginURL, fetchURL);
        updater.checkForUpdate();
    }

    public void checkForUpdate() {
        if(currentVersion.equalsIgnoreCase("SNAPSHOT")) {
            NotLib.dbg().log(NotDebugger.C_WARN, "You are running a snapshot build, skipping update check.");
            return;
        }

        if (currentVersion.equalsIgnoreCase("DEV")) {
            NotLib.dbg().log(NotDebugger.C_WARN, "You are running a development build, skipping update check.");
            return;
        }

        UpdateInfo updateInfo = getUpdateInfo();
        if (updateInfo == null) {
            NotLib.dbg().log(NotDebugger.C_WARN,"Failed to check for updates, please check them manually at " + pluginURL);
            return;
        }

        UpdateType updateType = compareVersions(currentVersion, updateInfo.version);
        
        switch (updateType) {
            case NONE:
                NotLib.dbg().log(NotDebugger.C_INFO,"You are running the latest version of " + pluginName + " (" + currentVersion + ")");
                break;
            case PATCH:
                NotLib.dbg().log(NotDebugger.C_INFO,"A minor update (patch) is available for " + pluginName + "! " + 
                    currentVersion + " -> " + updateInfo.version);
                handleUpdate(updateInfo, "patch update");
                break;
            case MINOR:
                NotLib.dbg().log(NotDebugger.C_WARN,"A feature update is available for " + pluginName + "! " + 
                    currentVersion + " -> " + updateInfo.version);
                handleUpdate(updateInfo, "feature update");
                break;
            case MAJOR:
                NotLib.dbg().log(NotDebugger.C_WARN,"A MAJOR update is available for " + pluginName + "! " + 
                    currentVersion + " -> " + updateInfo.version + " - This may contain breaking changes!");
                handleUpdate(updateInfo, "major update");
                break;
            case SNAP:
                NotLib.dbg().log(NotDebugger.C_WARN, "Because you are using a snapshot, there is an new version of snapshot of " + pluginName + "! " + 
                    currentVersion + " -> " + updateInfo.version + " - This may contain some bugs!");
                handleUpdate(updateInfo, "snapshot update");
        }


        try {
            List<String> files = plugin.getConfigFilePaths();
            for (String file : files) {
                InputStream defaultConfigStream = plugin.getResource(file);
                if (defaultConfigStream != null) {
                    FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream, StandardCharsets.UTF_8));
                    FileConfiguration actualConfig = plugin.getSubConfig(file);

                    for (String key : defaultConfig.getKeys(true)) {
                        if(!actualConfig.contains(key)) {
                            actualConfig.set(key, defaultConfig.get(key));
                        }
                    }
                } else {
                    NotLib.dbg().log(NotDebugger.C_ERROR, "File " + file + " not found!");
                }
            }
        } catch (Exception e) {
            NotLib.dbg().log(NotDebugger.C_WARN, "Error updating configuration files: " + e.getMessage());
        }
    }

    private void handleUpdate(UpdateInfo updateInfo, String updateType) {
        NotLib.dbg().log(NotDebugger.C_INFO,"You can download it at " + pluginURL + " to get newest features or bug fixes!");
    }

    private UpdateInfo getUpdateInfo() {
        if (currentVersion.contains("DEV")) {
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

            JSONParser parser = new JSONParser();
            JSONArray json = (JSONArray) parser.parse(response);
            JSONObject jObject = (JSONObject) json.getFirst();
            String tagName = (String) jObject.get("tag_name");
            
            if (tagName != null && tagName.startsWith("v")) {
                tagName = tagName.substring(1);
            }
            
            return new UpdateInfo(tagName);
            
        } catch (Exception e) {
            NotLib.dbg().log(NotDebugger.C_ERROR,"Error fetching update info: " + e.getMessage());
            return null;
        }
    }

    private UpdateType compareVersions(String current, String latest) {
        try {
            SemanticVersion currentVer = SemanticVersion.parse(current);
            SemanticVersion latestVer = SemanticVersion.parse(latest);
            
            if (latestVer.major != currentVer.major || latestVer.minor != currentVer.minor || latestVer.patch != currentVer.patch && latestVer.snap == currentVer.snap) {
                return UpdateType.SNAP;
            } else if (latestVer.major > currentVer.major) {
                return UpdateType.MAJOR;
            } else if (latestVer.major == currentVer.major && latestVer.minor > currentVer.minor) {
                return UpdateType.MINOR;
            } else if (latestVer.major == currentVer.major && latestVer.minor == currentVer.minor && latestVer.patch > currentVer.patch) {
                return UpdateType.PATCH;
            }  else {
                return UpdateType.NONE;
            }
        } catch (Exception e) {
            NotLib.dbg().log(NotDebugger.C_ERROR,"Error comparing versions, falling back to string comparison: " + e.getMessage());
            if (!current.equals(latest)) {
                return UpdateType.MINOR;
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

    private static class UpdateInfo {
        final String version;
        
        UpdateInfo(String version) {
            this.version = SemanticVersion.parse(version).toString();
        }
    }

    private enum UpdateType {
        NONE, PATCH, MINOR, MAJOR, SNAP
    }

    private static class SemanticVersion {
        final int major;
        final int minor;
        final int patch;
        final String snap;
        
        SemanticVersion(int major, int minor, int patch, String snap) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.snap = snap;
        }
        
        static SemanticVersion parse(String version) {
            if (version.startsWith("v")) {
                version = version.substring(1);
            }

            boolean isSnapshot = false;

            if(version.contains("snap")) {
                isSnapshot = true;
            }

            String[] ver = version.split("\\-");
            String[] parts = ver[0].split("\\.");
            int major = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
            int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
            String snapshot = isSnapshot ? "SNAPSHOT" : "";
            
            return new SemanticVersion(major, minor, patch, snapshot);
        }
        
        @Override
        public String toString() {
            return major + "." + minor + "." + patch + "-" + snap;
        }
    }
}