package dev.notmarra.notlib;

import dev.notmarra.notlib.test.database.DatabaseTest;
import dev.notmarra.notlib.test.database.Join;
import org.bukkit.plugin.java.JavaPlugin;

public final class NotLib extends JavaPlugin {
    private DatabaseTest databaseTest;

    @Override
    public void onEnable() {
        databaseTest = new DatabaseTest(this);

        getServer().getPluginManager().registerEvents(new Join(databaseTest), this);
    }

    @Override
    public void onDisable() {
        if (databaseTest != null) databaseTest.close();
    }
}
