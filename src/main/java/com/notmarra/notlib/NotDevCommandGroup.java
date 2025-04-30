package com.notmarra.notlib;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import com.notmarra.notlib.extensions.NotCommandGroup;
import com.notmarra.notlib.extensions.NotPlugin;
import com.notmarra.notlib.font.NotFontItem;
import com.notmarra.notlib.utils.ChatF;
import com.notmarra.notlib.utils.NotBossBar;
import com.notmarra.notlib.utils.command.NotCommand;
import com.notmarra.notlib.utils.command.arguments.NotLiteralArg;
import com.notmarra.notlib.utils.command.arguments.NotStringArg;
import com.notmarra.notlib.utils.gui.NotGUI;
import com.notmarra.notlib.utils.gui.NotGUIItem;
import com.notmarra.notlib.utils.gui.NotGUISlotIDs;

import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

public final class NotDevCommandGroup extends NotCommandGroup {
    public static final String ID = "notdevcommandgroup";

    public NotDevCommandGroup(NotPlugin plugin) { super(plugin); }

    @Override
    public String getId() { return ID; }

    @Override
    public List<NotCommand> notCommands() {
        return List.of(
            testGui(),
            diffgui(),
            testanimationsgui(),
            testChatFClickable(),
            testChatFClickableWithOptions(),
            testChatFClickableInfinite(),
            testChatFHoverItem(),
            testPatternGui(),
            testCustomFont(),
            testCommandSignature(),
            testCommandHelp(),
            testBossBar(),
            testScheduler()
        );
    }

    private NotCommand testChatFClickable() {
        return NotCommand.of("test-chatf-clickable", cmd -> {
            ChatF.empty()
                .appendBold("Test 1", ChatF.C_RED)

                .append("Test 2", Style.style(ChatF.C_GREEN, TextDecoration.UNDERLINED))
                .hover(ChatF.of("You hovered over me!", ChatF.C_YELLOW))
                .click(audience -> {
                    ChatF.of("You click on a text").sendTo(cmd.getPlayer());
                })

                .appendBold("Test 3", ChatF.C_BLUE)
                .sendTo(cmd.getPlayer());
        });
    }

    private NotCommand testChatFClickableWithOptions() {
        return NotCommand.of("test-chatf-clickable-with-options", cmd -> {
            ChatF.empty()
                .appendBold("Test 1", ChatF.C_RED)

                .append("Test 2", Style.style(ChatF.C_GREEN, TextDecoration.UNDERLINED))
                .hover(ChatF.of("You hovered over me!", ChatF.C_YELLOW))
                .clickWithOptions(-1, Duration.ofSeconds(10), audience -> {
                    ChatF.of("You click on a text").sendTo(cmd.getPlayer());
                })

                .appendBold("Test 3", ChatF.C_BLUE)
                .sendTo(cmd.getPlayer());
        });
    }

    private NotCommand testChatFClickableInfinite() {
        return NotCommand.of("test-chatf-clickable-infinite", cmd -> {
            ChatF.empty()
                .appendBold("Test 1", ChatF.C_RED)

                .append("Test 2", Style.style(ChatF.C_GREEN, TextDecoration.UNDERLINED))
                .hover(ChatF.of("You hovered over me!", ChatF.C_YELLOW))
                .clickInfinite(audience -> {
                    ChatF.of("You click on a text").sendTo(cmd.getPlayer());
                })

                .appendBold("Test 3", ChatF.C_BLUE)
                .sendTo(cmd.getPlayer());
        });
    }

    private NotCommand testChatFHoverItem() {
        return NotCommand.of("test-chatf-hover-item", cmd -> {
            ChatF.empty()
                .appendBold("Test 1", ChatF.C_RED)

                .append("Test 2", Style.style(ChatF.C_GREEN, TextDecoration.UNDERLINED))
                .hoverItem(
                    NotGUIItem.create(Material.DIAMOND_AXE)
                )
                // .hoverEntity(cmd.getPlayer())

                .click(audience -> {
                    ChatF.of("You click on a text").sendTo(cmd.getPlayer());
                })

                .appendBold("Test 3", ChatF.C_BLUE)
                .sendTo(cmd.getPlayer());
        });
    }

    private NotCommand testGui() {
        return NotCommand.of("testgui", cmd -> {
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
                        c.getRootAndItsChildren().forEach(ch -> start.put(ch.id(), ch.pos().x));

                        Map<UUID, Integer> end = new HashMap<>();
                        c.getRootAndItsChildren().forEach(ch -> end.put(ch.id(), ch.pos().x + c.gui().rowSize()));

                        c.gui().aProgress(20L, 10).start((progress) -> {
                            c.getRootAndItsChildren().forEach(ch -> {
                                int startX = start.get(ch.id());
                                int endX = end.get(ch.id());
                                int currentX = startX + Math.round(progress * (endX - startX));
                                ch.position(currentX, ch.pos().y);
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
    }

    private NotCommand diffgui() {
        return NotCommand.of("diffgui", cmd -> {
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
                        ChatF.of("Closing GUI...").sendTo(player);
                        c.gui().close(player);
                    })
                    .addButton(Material.GOLD_INGOT, "Bank", 3, 0, (event, c) -> {
                        Map<UUID, Integer> start = new HashMap<>();
                        c.getRootAndItsChildren().forEach(ch -> start.put(ch.id(), ch.pos().x));

                        Map<UUID, Integer> end = new HashMap<>();
                        c.getRootAndItsChildren().forEach(ch -> end.put(ch.id(), ch.pos().x + c.gui().rowSize()));

                        c.gui().aInfinite(20L, 10).start((progress) -> {
                            c.getRootAndItsChildren().forEach(ch -> {
                                int startX = start.get(ch.id());
                                int endX = end.get(ch.id());
                                int currentX = startX + Math.round(progress * (endX - startX));
                                ch.position(currentX, ch.pos().y);
                            });
                        });
                    })
                .gui()
                .onClose(event -> {
                    Player player = (Player) event.getPlayer();
                    ChatF.of("You closed the GUI!").sendTo(player);
                })
                .open(cmd.getPlayer());
        });
    }

    private NotCommand testanimationsgui() {
        return NotCommand.of("testanimationsgui", cmd -> {
            NotGUI.create("Example GUI")
                .rows(1)
                .addButton(Material.COMPASS, "Navigation", 0, (event, c) -> {
                    int start = c.pos().x;
                    int end = start + c.gui().rowSize();

                    c.gui().aPulse(20L, 50).inf().start(progress -> {
                        int newX = start + Math.round(progress * (end - start));
                        c.position(newX, c.pos().y);
                    });

                    // c.gui().aBounce(20L, 50, 5, (progress) -> {
                    //     int newX = start + Math.round(progress * (end - start));
                    //     c.position(newX, c.pos().y);
                    // });
                })
                .createItem(Material.DISPENSER)
                    .name(ChatF.empty())
                    .lore(List.of(ChatF.empty()))
                    .addToGUI(1)
                    .gui()
                .onClose(event -> {
                    Player player = (Player) event.getPlayer();
                    ChatF.of("You closed the GUI!").sendTo(player);
                })
                .open(cmd.getPlayer());
        });
    }

    private NotCommand testPatternGui() {
        List<String> patternList = List.of(
            "#########",
            "###X#X###",
            "##X#X#X##",
            "###X#X###",
            "####X####",
            "#########"
        );

        return NotCommand.of("test-pattern-gui", cmd -> {
            NotGUI.create("TITLE")
                .pattern(patternList)
                .emptySlotChars(List.of('#'))
                .onPatternMatch(info -> {
                    if (info.ch == 'X') {
                        return info.gui.createItem(Material.DIAMOND_AXE).name(ChatF.of("COUNT: " + info.count + " TOTAL: " + info.total));
                        // OR
                        // return NotGUIItem.create(info.gui, Material.DIAMOND_AXE);
                    }
                    return null;
                })
                .onOpen(e -> {
                    ChatF.of("You have open the GUI!").sendTo(e.getPlayer());
                })
                .onClose(e -> {
                    ChatF.of("You have closed the GUI!").sendTo(e.getPlayer());
                })
                .open(cmd.getPlayer());
        });
    }

    private NotCommand testCustomFont() {
        return NotCommand.of("test-custom-font", cmd -> {
            ChatF.of("You got a ").append(NotFontItem.DIAMOND).append("!").sendTo(cmd.getPlayer());
        });
    }

    private NotCommand testCommandSignature() {
        return NotCommand.of("test-command-signature", cmd -> {
            NotCommand command = NotCommand.of("test");
            NotStringArg arg1 = command.stringArg("arg1");
            NotStringArg arg2 = arg1.stringArg("arg2");
            ChatF.of("--- SIGNATURE ---").sendTo(cmd.getPlayer());
            ChatF.of(arg2.getSignature()).sendTo(cmd.getPlayer());
            ChatF.of("--- SIGNATURE WITH SUFFIX ---").sendTo(cmd.getPlayer());
            ChatF.of(arg2.getSignatureWith("player", "other")).sendTo(cmd.getPlayer());
        });
    }

    private NotCommand testCommandHelp() {
        return NotCommand.of("test-command-help", cmd -> {
            NotCommand command = NotCommand.of("test");
            command.setDescription(ChatF.of("This command does a lot of things!", ChatF.C_RED));

            NotLiteralArg createArg = command.literalArg("create");
            createArg.setDescription("This argument is very important!");

            NotStringArg arg2 = createArg.stringArg("arg2");
            arg2.setDescription("This argument is also very important!");

            NotLiteralArg setArg = command.literalArg("set");
            setArg.setDescription("This argument is very important!");

            NotStringArg setArg2 = setArg.stringArg("arg2");
            setArg2.setDescription("This argument is also very important!");

            ChatF.of("--- HELP ---").sendTo(cmd.getPlayer());
            command.getHelpFor(List.of("create")).sendTo(cmd.getPlayer());

            ChatF.of("--- PATH TO setArg2 ---").sendTo(cmd.getPlayer());
            ChatF.of(setArg2.getPath()).sendTo(cmd.getPlayer());
        });
    }

    NotBossBar bossBar;
    private NotCommand testBossBar() {
        return NotCommand.of("test-bossbar", cmd -> {
            if (bossBar != null) {
                cmd.getPlayer().hideBossBar(bossBar.get());
                cmd.getPlayer().sendActionBar(ChatF.of("BossBar removed!").build());
                bossBar = null;
            } else {
                bossBar = NotBossBar.create(ChatF.of("Test BossBar").build());
                cmd.getPlayer().showBossBar(bossBar.get());
                cmd.getPlayer().sendActionBar(ChatF.of("BossBar shown!").build());
            }
        });
    }

    private NotCommand testScheduler() {
        return NotCommand.of("test-scheduler", cmd -> {
            plugin.scheduler().runTaskAsync(() -> {
                ChatF.of("This is a test!").sendTo(cmd.getPlayer());
            });
            plugin.scheduler().runTaskLater(() -> {
                ChatF.of("This is a test! 2 sec later!!!!!!!!!").sendTo(cmd.getPlayer());
            }, 40L);
        });
    }
}
