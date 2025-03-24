package com.notmarra.notlib;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.notmarra.notlib.utils.ChatF;
import com.notmarra.notlib.utils.command.NotCommand;
import com.notmarra.notlib.utils.gui.NotInvHolder;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

public final class NotLib extends JavaPlugin {
    private static NotLib instance;
    private static Boolean PlaceholderAPI = false;

    @Override
    public void onEnable() {
        instance = this;

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            this.getLogger().info("PlaceholderAPI found, hooking into it");
            PlaceholderAPI = true;
        }

        this.getLogger().info("Enabled successfully!");

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            NotCommand cmd = NotCommand.of("testgui", c -> {
                // NotGUI
                //     .title("Test GUI")

                //     .size(27) // must be a multiple of 9
                //     .rows(3)

                //     .item(0, Material.DIAMOND_SWORD, "Diamond Sword", "This is a diamond sword")
                //     .onClick(isShiftClick -> {
                //         ctx.getPlayer().sendMessage("Shift clicked!");
                //     })

                //     .pattern(List<String>)
                //     .item('#', Material.DIAMOND_SWORD, "Diamond Sword", "This is a diamond sword")
                    
                //     // material: String, name: String, lore: List<String>
                //     .item(ConfigurationSection, Material.DIAMOND_SWORD, "Diamond Sword", "This is a diamond sword")

                //     .open(player);
                NotInvHolder holder = new NotInvHolder(this);
                c.getPlayer().openInventory(holder.getInventory());
            });

            cmd.greedyStringArg("message", arg -> {
                ChatF.of(arg.get(), ChatF.C_RED).sendTo(arg.getPlayer());
            });

            commands.registrar().register(cmd.build());
        });
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Disabled successfully!");
    }

    public static NotLib getInstance() {
        return instance;
    }

    public static Boolean hasPAPI() { return PlaceholderAPI; }
}
