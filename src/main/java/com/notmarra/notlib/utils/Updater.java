package com.notmarra.notlib.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.net.URI;

public class Updater {
    private final Plugin plugin;
    private final String pluginName;
    private final String currentVersion;
    private final String pluginURL;
    private final String fetchURL;

    public Updater(Plugin plugin, String pluginName, String currentVersion, String pluginURL, String fetchURL) {
        this.plugin = plugin;
        this.pluginName = pluginName;
        this.currentVersion = currentVersion;
        this.pluginURL = pluginURL;
        this.fetchURL = fetchURL;
    }

    public void checkForUpdate() {
        String latestVersion = getLatestVersion();
        if(currentVersion.contains("SNAPSHOT")) {
            plugin.getLogger().warning("You are running a snapshot build, skipping update check.");
            return;
        } 
        if (currentVersion.contains("DEV")) {
            plugin.getLogger().warning("You are running a development build, skipping update check.");
            return;
        }
        if (latestVersion != null) {
            if (!currentVersion.equals(latestVersion)) {
                plugin.getLogger().warning("A new version of " + pluginName + " is available! You are running version " + currentVersion + ", the latest version is " + latestVersion + ". You can download it at " + pluginURL);
            } else {
                plugin.getLogger().info("You are running the latest version of " + pluginName + " (" + currentVersion + ")");
            }
        } else {
            plugin.getLogger().warning("Failed to check for updates, please check manually at " + pluginURL);
        }
    }

    private String getLatestVersion() {
        if (currentVersion.contains("DEV") || currentVersion.contains("SNAPSHOT")) {
            return null;
        } else {
            return fetchLatestVersion();
        }
    }

    private String fetchLatestVersion() {
        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create(fetchURL).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            String response = getStringFromURL(connection);
            if (response.contains("\"message\":\"Not Found\"")) {
                return null;
            }
            return response.substring(response.indexOf("\"tag_name\":\"") + 12, response.indexOf("\",\"target_commitish\""));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @NotNull
    private static String getStringFromURL(HttpURLConnection connection) throws IOException {
        InputStream inputStream = connection.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new java.io.InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append("\n");
        }
        bufferedReader.close();
        inputStream.close();
        return stringBuilder.toString();
    }
    
}
