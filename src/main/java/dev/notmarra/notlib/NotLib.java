package dev.notmarra.notlib;

import dev.notmarra.notlib.test.database.DatabaseTest;
import dev.notmarra.notlib.test.database.Join;
import org.bukkit.plugin.java.JavaPlugin;

public final class NotLib extends JavaPlugin {
    private NotLib instance;
    private DatabaseTest databaseTest;

    @Override
    public void onEnable() {
        instance = this;
        databaseTest = new DatabaseTest(this);

        getServer().getPluginManager().registerEvents(new Join(databaseTest), this);
    }

    @Override
    public void onDisable() {
        if (databaseTest != null) databaseTest.close();
    }

    public NotLib getInstance() {
        return instance;
    }

    private static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
