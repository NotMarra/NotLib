package com.notmarra.notlib;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import com.notmarra.notlib.database.NotDatabase;
import com.notmarra.notlib.database.source.NotMySQL;
import com.notmarra.notlib.database.source.NotSQLite;
import com.notmarra.notlib.database.structure.NotRecord;
import com.notmarra.notlib.extensions.NotListener;
import com.notmarra.notlib.utils.gui.NotGUI;
import com.notmarra.notlib.utils.ChatF;
import com.notmarra.notlib.utils.command.NotCommand;
import com.notmarra.notlib.utils.command.arguments.NotLiteralArg;
import com.notmarra.notlib.utils.command.arguments.NotPlayerArg;
import com.notmarra.notlib.utils.command.arguments.NotStringArg;

class NotDevListener extends NotListener {
    public NotDevListener(NotLib plugin) {
        super(plugin);
    }

    @Override
    public String getId() {
        return "NotDevListener";
    }

    @Override
    public List<NotCommand> notCommands() {
        return List.of(
            testSkullTextureCommand(),
            notLibDbTestCommand(),
            testSqlStuff()
        );
    }

    private NotCommand testSkullTextureCommand() {
        return NotCommand.of("testskulltexture", cmd -> {
            NotGUI.create("Skull Texture Test")
                .rows(1)
                .createItem(Material.PLAYER_HEAD)
                    .withSkullTexture("1aec2a159f62d2ace9e1d3e5057a7f8d1ec3ffdfe92433e0a09a9837cadf2083")
                    .addToGUI(0)
                    .gui()
                .createItem(Material.DIAMOND_AXE)
                    .onClick((event, c) -> {
                        ChatF.of("Clicked on skull texture!").sendTo(cmd.getPlayer());
                    })
                    .addToGUI(1)
                    .gui()
                .open(cmd.getPlayer());
        });
    }

    private NotDevTestMySQL getTestMySQLDB() { return (NotDevTestMySQL)plugin.db(NotDevTestMySQL.ID); }

    private NotCommand notLibDbTestCommand() {
        String commandName = "notlib-db-test";

        NotCommand command = NotCommand.of(commandName, cmd -> {
            ChatF.of("Test MySQL command executed!").sendTo(cmd.getPlayer());
        });

        NotLiteralArg queryArg = command.literalArg("query", arg -> {
            ChatF.of("/" + commandName + " query <db-id> <query>").sendTo(arg.getPlayer());
        });

        NotStringArg queryDbIdArg = queryArg.stringArg("dbId");
        queryDbIdArg.setSuggestions(plugin.db().getDatabaseIds());
        queryDbIdArg.onExecute(arg -> {
            ChatF.of("/" + commandName + " query " + arg.get() + " <query>").sendTo(arg.getPlayer());
        });

        queryDbIdArg.greedyStringArg("query", arg -> {
            NotDatabase db = plugin.db(queryDbIdArg.get());

            if (db == null) {
                ChatF.of("Database with ID " + queryDbIdArg.get() + " not found.").sendTo(arg.getPlayer());
                return;
            }

            String query = arg.get();

            if (db instanceof NotMySQL || db instanceof NotSQLite) {
                if (query.contains(";")) {
                    ChatF.of("Stacked queries are not supported.", ChatF.C_RED).sendTo(arg.getPlayer());
                    ChatF.of("NOTE: Remove any semicolon!", ChatF.C_LIGHTRED).sendTo(arg.getPlayer());
                    return;
                }

                if (query.toLowerCase().trim().startsWith("select")) {
                    List<NotRecord> results = db.getQueryExecutor().select(query);
                    if (results.isEmpty()) {
                        ChatF.of("No results found.").sendTo(arg.getPlayer());
                    } else {
                        ChatF.of("Found " + results.size() + " results:").sendTo(arg.getPlayer());
                        for (NotRecord record : results) {
                            ChatF.of(record.getData().toString()).sendTo(arg.getPlayer());
                        }
                    }
                } else if (
                    query.toLowerCase().trim().startsWith("insert") ||
                    query.toLowerCase().trim().startsWith("update") ||
                    query.toLowerCase().trim().startsWith("delete")
                ) {
                    int affectedRows = db.getQueryExecutor().update(query);
                    ChatF.of("Executed query. Affected rows: " + affectedRows).sendTo(arg.getPlayer());
                } else {
                    ChatF.of("Unsupported query type.", ChatF.C_RED).sendTo(arg.getPlayer());
                    ChatF.of("Only SELECT, INSERT, UPDATE, and DELETE queries are supported.", ChatF.C_RED).sendTo(arg.getPlayer());
                }
            } else {
                ChatF.of("Database of type " + db.getClass().getSimpleName() + " does not support queries.").sendTo(arg.getPlayer());
                return;
            }
        });

        return command;
    }

    private NotCommand testSqlStuff() {
        NotCommand command = NotCommand.of("test-sql-stuff", cmd -> {
            ChatF.of("Test SQL Stuff command executed!").sendTo(cmd.getPlayer());
        });

        // add xp

        NotLiteralArg addXpArg = command.literalArg("addXp", arg -> {
            ChatF.of("/test-sql-stuff addXp <player> <xp>").sendTo(arg.getPlayer());
        });

        NotPlayerArg addXpPlayerArg = addXpArg.playerArg("player", arg -> {
            ChatF.of("/test-sql-stuff addXp " + arg.get() + " <xp>").sendTo(arg.getPlayer());
        });

        addXpPlayerArg.intArg("xp", arg -> {
            NotDevTestMySQL db = getTestMySQLDB();

            if (db == null) {
                ChatF.of("Database NotDevTestMySQL not found.").sendTo(arg.getPlayer());
                return;
            }

            Player player = addXpPlayerArg.get();
            int xp = arg.get();
            if (db.addXp(player, xp)) {
                ChatF.of("Added " + xp + " XP to player " + player.getName()).sendTo(arg.getPlayer());
            } else {
                ChatF.of("Failed to add XP to player " + player.getName()).sendTo(arg.getPlayer());
            }
        });

        // remove xp

        NotLiteralArg removeXpArg = command.literalArg("removeXp", arg -> {
            ChatF.of("/test-sql-stuff removeXp <player> <xp>").sendTo(arg.getPlayer());
        });

        NotPlayerArg removeXpPlayerArg = removeXpArg.playerArg("player", arg -> {
            ChatF.of("/test-sql-stuff removeXp " + arg.get() + " <xp>").sendTo(arg.getPlayer());
        });

        removeXpPlayerArg.intArg("xp", arg -> {
            NotDevTestMySQL db = getTestMySQLDB();

            if (db == null) {
                ChatF.of("Database NotDevTestMySQL not found.").sendTo(arg.getPlayer());
                return;
            }

            Player player = removeXpPlayerArg.get();
            int xp = arg.get();
            if (db.removeXp(player, xp)) {
                ChatF.of("Added " + xp + " XP to player " + player.getName()).sendTo(arg.getPlayer());
            } else {
                ChatF.of("Failed to add XP to player " + player.getName()).sendTo(arg.getPlayer());
            }
        });

        command.literalArg("testResult", arg -> {
            NotDevTestMySQL db = getTestMySQLDB();

            if (db == null) {
                ChatF.of("Database NotDevTestMySQL not found.").sendTo(arg.getPlayer());
                return;
            }

            int deleted = db.getTable(db.T_USERS)
                .delete(b -> b.where(db.T_USERS_C_PLAYER_NAME, "=", "test"));

            ChatF.of("Deleted " + deleted + " rows.").sendTo(arg.getPlayer());
        });

        return command;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        NotDevTestMySQL db = getTestMySQLDB();

        if (db == null) {
            getLogger().error("Database NotDevTestMySQL not found.");
            return;
        }
        
        if (db.existsPlayer(player)) {
            getLogger().info("Player " + player.getName() + " already exists in the database.");
            return;
        }

        if (!db.insertPlayer(player)) {
            getLogger().error("Failed to insert player " + player.getName() + " into the database.");
            return;
        }

        getLogger().info("Inserted player " + player.getName() + " into the database.");
    }
}