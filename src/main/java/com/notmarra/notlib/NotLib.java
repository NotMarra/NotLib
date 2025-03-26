package com.notmarra.notlib;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.java.JavaPlugin;

import com.notmarra.notlib.utils.ChatF;
import com.notmarra.notlib.utils.command.NotCommand;
import com.notmarra.notlib.utils.gui.NotGUI;
import com.notmarra.notlib.utils.gui.NotGUIListener;
import com.notmarra.notlib.utils.gui.NotGUISlotIDs;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

public final class NotLib extends JavaPlugin {
    private static NotLib instance;
    private static Boolean PlaceholderAPI = false;
    private NotGUIListener guiListener;

    @Override
    public void onEnable() {
        instance = this;
        guiListener = new NotGUIListener(this);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            this.getLogger().info("PlaceholderAPI found, hooking into it");
            PlaceholderAPI = true;
        }

        this.getLogger().info("Enabled successfully!");

        guiListener.register();

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            NotCommand testgui = NotCommand.of("testgui", cmd -> {
                NotGUI.create("Example GUI")
                    .rows(6)
                    .addButton(Material.COMPASS, "Navigation", 4, (event, c) -> {
                        Player player = (Player) event.getWhoClicked();
                        ChatF.of("You clicked the navigation button!").sendTo(player);
                    })
                    .createContainer(1, 1, 7, 2)
                        .addButton(Material.REDSTONE, "Settings", 0, 0, (event, c) -> {
                            Player player = (Player) event.getWhoClicked();
                            ChatF.of("Opening settings...").sendTo(player);
                        })
                        .addButton(Material.PAPER, "Profile", 2, 0, (event, c) -> {
                            Player player = (Player) event.getWhoClicked();
                            ChatF.of("Opening profile...").sendTo(player);
                        })
                        .addButton(Material.EMERALD, "Shop", 4, 0, (event, c) -> {
                            Player player = (Player) event.getWhoClicked();
                            ChatF.of("Opening shop...").sendTo(player);
                        })
                        .addButton(Material.GOLD_INGOT, "Bank", 6, 0, (event, c) -> {
                            Map<UUID, Integer> start = new HashMap<>();
                            c.getRootAndItsChildren().forEach(ch -> start.put(ch.id(), ch.getPosition().x));

                            Map<UUID, Integer> end = new HashMap<>();
                            c.getRootAndItsChildren().forEach(ch -> end.put(ch.id(), ch.getPosition().x + c.gui().rowSize()));

                            c.gui().animate(20L, 10, (progress) -> {
                                c.getRootAndItsChildren().forEach(ch -> {
                                    int startX = start.get(ch.id());
                                    int endX = end.get(ch.id());
                                    int currentX = startX + Math.round(progress * (endX - startX));
                                    ch.position(currentX, ch.getPosition().y);
                                });
                            });
                        })
                    .gui()
                        .createContainer(1, 2, 7, 2)
                            .createItem(Material.BOOK, 3, 0)
                                .name("Inventory")
                                .lore(List.of("Your personal items", "Click an item to use it"))
                        .parent()
                            .createItem(Material.DIAMOND_SWORD, 0, 1)
                                .name("Mythical Sword")
                                .amount(1)
                        .parent()
                            .createItem(Material.GOLDEN_APPLE, 2, 1)
                                .name("Golden Apple")
                                .amount(5)
                        .parent()
                            .createItem(Material.POTION, 4, 1)
                                .name("Health Potion")
                                .amount(3)
                        .parent()
                            .createItem(Material.ENDER_PEARL, 6, 1)
                                .name("Ender Pearl")
                                .amount(16)
                    .gui()
                        .createContainer(1, 4, 7, 1)
                            .createItem(Material.PLAYER_HEAD, 3, 0)
                                .name("Friends List")
                    .gui()
                        .createContainer()
                            .position(0, 5)
                            .size(3, 1)
                                .createItem(Material.EMERALD_BLOCK, 1, 0)
                                    .name("Online (3)")
                    .gui()
                        .createContainer()
                            .position(6, 5)
                            .size(3, 1)
                                .createItem(Material.REDSTONE_BLOCK, 1, 0)
                                    .name("Offline (7)")
                    .gui()
                        .addButton(Material.BARRIER, "Close", 4, 5, (event, c) -> {
                            event.getWhoClicked().closeInventory();
                        })
                    .open(cmd.getPlayer());
            });

            testgui.greedyStringArg("message", arg -> {
                ChatF.of(arg.get(), ChatF.C_RED).sendTo(arg.getPlayer());
            });

            commands.registrar().register(testgui.build());
        });

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            NotCommand diffgui = NotCommand.of("diffgui", cmd -> {
                NotGUI.create("Example GUI")
                    .type(InventoryType.HOPPER)
                    .addButton(Material.COMPASS, "Navigation", NotGUISlotIDs.HopperSlots.FIRST, (event, c) -> {
                        Player player = (Player) event.getWhoClicked();
                        ChatF.of("You clicked the navigation button!").sendTo(player);
                    })
                    .createContainer(1, 0, 4, 1)
                        .addButton(Material.REDSTONE, "Settings", 0, 0, (event, c) -> {
                            Player player = (Player) event.getWhoClicked();
                            ChatF.of("Opening settings...").sendTo(player);
                        })
                        .addButton(Material.PAPER, "Profile", 1, 0, (event, c) -> {
                            Player player = (Player) event.getWhoClicked();
                            ChatF.of("Opening profile...").sendTo(player);
                        })
                        .addButton(Material.EMERALD, "Shop", 2, 0, (event, c) -> {
                            Player player = (Player) event.getWhoClicked();
                            ChatF.of("Opening shop...").sendTo(player);
                        })
                        .addButton(Material.GOLD_INGOT, "Bank", 3, 0, (event, c) -> {
                            Map<UUID, Integer> start = new HashMap<>();
                            c.getRootAndItsChildren().forEach(ch -> start.put(ch.id(), ch.getPosition().x));

                            Map<UUID, Integer> end = new HashMap<>();
                            c.getRootAndItsChildren().forEach(ch -> end.put(ch.id(), ch.getPosition().x + c.gui().rowSize()));

                            c.gui().animate(20L, 10, (progress) -> {
                                c.getRootAndItsChildren().forEach(ch -> {
                                    int startX = start.get(ch.id());
                                    int endX = end.get(ch.id());
                                    int currentX = startX + Math.round(progress * (endX - startX));
                                    ch.position(currentX, ch.getPosition().y);
                                });
                            });
                        })
                    .gui()
                    .open(cmd.getPlayer());
            });

            commands.registrar().register(diffgui.build());
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

    public NotGUIListener getGUIListener() {
        return guiListener;
    }
}
