package com.notmarra.notlib.quests;

import com.notmarra.notlib.extensions.NotListener;
import com.notmarra.notlib.extensions.NotPlugin;
import com.notmarra.notlib.quests.NotQuestGUI.QuestStatus;
import com.notmarra.notlib.utils.ChatF;
import com.notmarra.notlib.utils.command.NotCommand;
import com.notmarra.notlib.utils.command.arguments.NotLiteralArg;
import com.notmarra.notlib.utils.command.arguments.NotPlayerArg;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class NotQuestListener extends NotListener {
    public static final String ID = "quests";

    // Permissions
    public static final String PERMISSION_QUESTS = "notquests.use";
    public static final String PERMISSION_ADMIN = "notquests.admin";
    public static final String PERMISSION_RELOAD = "notquests.reload";

    // Player quest data - stores active quests and progress for each player
    private final Map<UUID, Map<String, QuestProgress>> playerQuestData = new ConcurrentHashMap<>();
    
    // Quest definitions loaded from config
    private final Map<String, Quest> quests = new HashMap<>();
    
    // Cache for quest categories
    private final Map<String, List<String>> questCategories = new HashMap<>();
    
    // Quest configuration
    private boolean notifyOnProgress;
    private boolean soundOnComplete;
    private String completionSound;
    private float soundVolume;
    private float soundPitch;
    private int maxActiveQuests;
    private boolean autoAssignDailyQuests;

    // NamespacedKey for storing quest data on player
    private final NamespacedKey questDataKey;

    private NotQuestGUI questGUI;

    public NotQuestListener(NotPlugin plugin) {
        super(plugin);
        this.questDataKey = new NamespacedKey(plugin, "quest_data");
        this.questGUI = new NotQuestGUI(plugin, this);
        registerConfigurable();
    }

    @Override
    public List<NotCommand> notCommands() {
        return List.of(
            questCommand(),
            questGUICommand()
        );
    }

    public NotCommand questGUICommand() {
        return NotCommand.of("questgui", cmd -> {
            Player player = cmd.getPlayer();
            
            if (!player.hasPermission(PERMISSION_QUESTS)) {
                ChatF.empty()
                    .appendBold("You don't have permission to use quests!", ChatF.C_RED)
                    .sendTo(player);
                return;
            }
            
            questGUI.openMainMenu(player);
        });
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<String> getConfigPaths() {
        return List.of("quests.yml");
    }

    @Override
    public void onConfigReload(List<String> reloadedConfigs) {
        FileConfiguration config = getConfig(getConfigPaths().get(0));
        loadQuestSettings(config);
        loadQuestDefinitions(config);
    }

    private void loadQuestSettings(FileConfiguration config) {
        notifyOnProgress = config.getBoolean("settings.notify-on-progress", true);
        soundOnComplete = config.getBoolean("settings.sound-on-complete", true);
        completionSound = config.getString("settings.completion-sound", "entity.player.levelup");
        soundVolume = (float) config.getDouble("settings.sound-volume", 1.0);
        soundPitch = (float) config.getDouble("settings.sound-pitch", 1.0);
        maxActiveQuests = config.getInt("settings.max-active-quests", 5);
        autoAssignDailyQuests = config.getBoolean("settings.auto-assign-daily", true);
    }

    private void loadQuestDefinitions(FileConfiguration config) {
        quests.clear();
        questCategories.clear();
        
        ConfigurationSection questsSection = config.getConfigurationSection("quests");
        if (questsSection == null) return;
        
        for (String questId : questsSection.getKeys(false)) {
            ConfigurationSection questSection = questsSection.getConfigurationSection(questId);
            if (questSection == null) continue;
            
            String title = questSection.getString("title", "Unnamed Quest");
            String description = questSection.getString("description", "");
            String category = questSection.getString("category", "default");
            boolean repeatable = questSection.getBoolean("repeatable", false);
            int cooldownHours = questSection.getInt("cooldown-hours", 24);
            
            // Add quest to category
            questCategories.computeIfAbsent(category, k -> new ArrayList<>()).add(questId);
            
            // Parse rewards
            Map<String, Object> rewards = new HashMap<>();
            ConfigurationSection rewardsSection = questSection.getConfigurationSection("rewards");
            if (rewardsSection != null) {
                for (String rewardType : rewardsSection.getKeys(false)) {
                    rewards.put(rewardType, rewardsSection.get(rewardType));
                }
            }
            
            // Parse objectives
            List<QuestObjective> objectives = new ArrayList<>();
            ConfigurationSection objectivesSection = questSection.getConfigurationSection("objectives");
            if (objectivesSection != null) {
                for (String objectiveId : objectivesSection.getKeys(false)) {
                    ConfigurationSection objSection = objectivesSection.getConfigurationSection(objectiveId);
                    if (objSection == null) continue;
                    
                    String type = objSection.getString("type", "");
                    String targetId = objSection.getString("target", "");
                    int amount = objSection.getInt("amount", 1);
                    String objectiveDescription = objSection.getString("description", "Complete objective");
                    
                    objectives.add(new QuestObjective(objectiveId, type, targetId, amount, objectiveDescription));
                }
            }
            
            Quest quest = new Quest(questId, title, description, category, objectives, rewards, repeatable, cooldownHours);
            quests.put(questId, quest);
        }
        
        getLogger().info("Loaded " + quests.size() + " quests in " + questCategories.size() + " categories");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        loadPlayerQuestData(player);
        
        // Auto-assign daily quests if enabled
        if (autoAssignDailyQuests) {
            assignDailyQuests(player);
        }
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        
        Player player = event.getEntity().getKiller();
        EntityType entityType = event.getEntityType();
        
        updateQuestProgress(player, "kill", entityType.toString().toLowerCase(), 1);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();
        
        updateQuestProgress(player, "break", blockType.toString().toLowerCase(), 1);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) return; // TODO: replace the deprecation
        
        Player player = event.getPlayer();
        
        // This could track interactions with blocks, NPCs, etc.
        if (event.getClickedBlock() != null) {
            Material blockType = event.getClickedBlock().getType();
            updateQuestProgress(player, "interact", blockType.toString().toLowerCase(), 1);
        }
    }

    private void updateQuestProgress(Player player, String objectiveType, String targetId, int amount) {
        UUID playerId = player.getUniqueId();
        if (!playerQuestData.containsKey(playerId)) return;
        
        Map<String, QuestProgress> activeQuests = playerQuestData.get(playerId);
        boolean anyUpdated = false;
        
        for (Map.Entry<String, QuestProgress> entry : activeQuests.entrySet()) {
            String questId = entry.getKey();
            QuestProgress progress = entry.getValue();
            Quest quest = quests.get(questId);
            
            if (quest == null) continue;
            
            boolean updated = false;
            
            for (QuestObjective objective : quest.getObjectives()) {
                if (objective.getType().equals(objectiveType) && objective.getTargetId().equals(targetId)) {
                    int currentProgress = progress.getObjectiveProgress(objective.getId());
                    int newProgress = Math.min(currentProgress + amount, objective.getAmount());
                    
                    if (newProgress > currentProgress) {
                        progress.setObjectiveProgress(objective.getId(), newProgress);
                        updated = true;
                        anyUpdated = true;
                        
                        // Notify player of progress
                        if (notifyOnProgress) {
                            ChatF.empty()
                                .append("[Quest] ", ChatF.C_GOLD)
                                .append(quest.getTitle(), ChatF.C_YELLOW)
                                .append(": ", ChatF.C_WHITE)
                                .append(objective.getDescription(), ChatF.C_GRAY)
                                .append(" - ", ChatF.C_WHITE)
                                .append(newProgress + "/" + objective.getAmount(), newProgress >= objective.getAmount() ? ChatF.C_GREEN : ChatF.C_YELLOW)
                                .sendTo(player);
                        }
                    }
                }
            }
            
            // Check if quest is complete
            if (updated && isQuestComplete(quest, progress)) {
                completeQuest(player, questId);
            }
        }
        
        if (anyUpdated) {
            savePlayerQuestData(player);
        }
    }

    private boolean isQuestComplete(Quest quest, QuestProgress progress) {
        for (QuestObjective objective : quest.getObjectives()) {
            int currentProgress = progress.getObjectiveProgress(objective.getId());
            if (currentProgress < objective.getAmount()) {
                return false;
            }
        }
        return true;
    }

    private void completeQuest(Player player, String questId) {
        Quest quest = quests.get(questId);
        if (quest == null) return;
        
        // Give rewards
        giveQuestRewards(player, quest);
        
        // Play sound
        if (soundOnComplete) {
            try {
                player.playSound(player.getLocation(), completionSound, soundVolume, soundPitch);
            } catch (Exception e) {
                getLogger().warn("Failed to play quest completion sound: " + completionSound);
            }
        }
        
        // Send completion message
        ChatF.empty()
            .append("[Quest Complete] ", ChatF.C_GOLD)
            .appendBold(quest.getTitle(), ChatF.C_GREEN)
            .nl()
            .append(quest.getDescription(), ChatF.C_GRAY)
            .sendTo(player);
        
        // Remove quest from active quests
        UUID playerId = player.getUniqueId();
        playerQuestData.get(playerId).remove(questId);
        
        // If repeatable, add to completed quests with timestamp
        if (quest.isRepeatable()) {
            playerQuestData.get(playerId).put("completed:" + questId, new QuestProgress(questId, System.currentTimeMillis()));
        }
        
        // Save player data
        savePlayerQuestData(player);
    }

    private void giveQuestRewards(Player player, Quest quest) {
        Map<String, Object> rewards = quest.getRewards();
        
        // Process different reward types
        for (Map.Entry<String, Object> entry : rewards.entrySet()) {
            String rewardType = entry.getKey();
            Object rewardValue = entry.getValue();
            
            switch (rewardType) {
                case "money":
                    if (rewardValue instanceof Number) {
                        double amount = ((Number) rewardValue).doubleValue();
                        // Hook into your economy plugin here
                        ChatF.empty()
                            .append("[Reward] ", ChatF.C_GREEN)
                            .append("$" + amount, ChatF.C_YELLOW)
                            .sendTo(player);
                    }
                    break;
                case "xp":
                    if (rewardValue instanceof Number) {
                        int amount = ((Number) rewardValue).intValue();
                        player.giveExp(amount);
                        ChatF.empty()
                            .append("[Reward] ", ChatF.C_GREEN)
                            .append(amount + " XP", ChatF.C_YELLOW)
                            .sendTo(player);
                    }
                    break;
                case "command":
                    if (rewardValue instanceof String) {
                        String command = ((String) rewardValue).replace("%player%", player.getName());
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    } else if (rewardValue instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<String> commands = (List<String>) rewardValue;
                        for (String command : commands) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
                        }
                    }
                    break;
                case "item":
                    // This is simplified - in a real implementation, you'd parse item configs
                    if (rewardValue instanceof String) {
                        try {
                            Material material = Material.valueOf(((String) rewardValue).toUpperCase());
                            player.getInventory().addItem(new ItemStack(material));
                            ChatF.empty()
                                .append("[Reward] ", ChatF.C_GREEN)
                                .append("1x " + material.toString(), ChatF.C_YELLOW)
                                .sendTo(player);
                        } catch (IllegalArgumentException e) {
                            getLogger().warn("Invalid material for quest reward: " + rewardValue);
                        }
                    }
                    break;
            }
        }
    }

    private void loadPlayerQuestData(Player player) {
        UUID playerId = player.getUniqueId();
        Map<String, QuestProgress> questData = new HashMap<>();
        
        // This is a simple implementation - you might want to use a database instead
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        if (pdc.has(questDataKey, PersistentDataType.STRING)) {
            try {
                String serializedData = pdc.get(questDataKey, PersistentDataType.STRING);
                if (serializedData != null && !serializedData.isEmpty()) {
                    // Deserialize quest data - this is a simplified version
                    String[] entries = serializedData.split(";");
                    for (String entry : entries) {
                        String[] parts = entry.split(":");
                        if (parts.length >= 2) {
                            String questId = parts[0];
                            QuestProgress progress;
                            
                            if (questId.startsWith("completed:")) {
                                // Format: completed:questId:timestamp
                                long timestamp = Long.parseLong(parts[2]);
                                progress = new QuestProgress(questId.substring(10), timestamp);
                            } else {
                                // Format: questId:obj1Id=progress,obj2Id=progress,...
                                progress = new QuestProgress(questId);
                                if (parts.length > 1 && !parts[1].isEmpty()) {
                                    String[] objectives = parts[1].split(",");
                                    for (String obj : objectives) {
                                        String[] objParts = obj.split("=");
                                        if (objParts.length == 2) {
                                            progress.setObjectiveProgress(objParts[0], Integer.parseInt(objParts[1]));
                                        }
                                    }
                                }
                            }
                            
                            questData.put(questId, progress);
                        }
                    }
                }
            } catch (Exception e) {
                getLogger().error("Failed to load quest data for player " + player.getName(), e);
            }
        }
        
        playerQuestData.put(playerId, questData);
    }

    private void savePlayerQuestData(Player player) {
        UUID playerId = player.getUniqueId();
        if (!playerQuestData.containsKey(playerId)) return;
        
        Map<String, QuestProgress> questData = playerQuestData.get(playerId);
        
        // Serialize quest data - this is a simplified version
        StringBuilder serialized = new StringBuilder();
        for (Map.Entry<String, QuestProgress> entry : questData.entrySet()) {
            String questId = entry.getKey();
            QuestProgress progress = entry.getValue();
            
            if (serialized.length() > 0) {
                serialized.append(";");
            }
            
            if (questId.startsWith("completed:")) {
                // Format: completed:questId:timestamp
                serialized.append(questId).append(":").append(progress.getCompletionTimestamp());
            } else {
                // Format: questId:obj1Id=progress,obj2Id=progress,...
                serialized.append(questId).append(":");
                
                StringBuilder objData = new StringBuilder();
                for (Map.Entry<String, Integer> objEntry : progress.getObjectiveProgress().entrySet()) {
                    if (objData.length() > 0) {
                        objData.append(",");
                    }
                    objData.append(objEntry.getKey()).append("=").append(objEntry.getValue());
                }
                
                serialized.append(objData);
            }
        }
        
        // Save to player's persistent data container
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.set(questDataKey, PersistentDataType.STRING, serialized.toString());
    }

    private void assignDailyQuests(Player player) {
        // Get daily quest category
        List<String> dailyQuests = questCategories.getOrDefault("daily", Collections.emptyList());
        if (dailyQuests.isEmpty()) return;
        
        UUID playerId = player.getUniqueId();
        Map<String, QuestProgress> playerQuests = playerQuestData.getOrDefault(playerId, new HashMap<>());
        
        // Count active quests
        int activeQuestCount = (int) playerQuests.keySet().stream()
            .filter(id -> !id.startsWith("completed:"))
            .count();
        
        // Calculate how many quests we can assign
        int slotsAvailable = Math.max(0, maxActiveQuests - activeQuestCount);
        if (slotsAvailable == 0) return;
        
        // Get quests the player can take (not already active or on cooldown)
        List<String> availableQuests = dailyQuests.stream()
            .filter(questId -> {
                // Not already active
                if (playerQuests.containsKey(questId)) return false;
                
                // Not on cooldown
                Quest quest = quests.get(questId);
                if (quest == null) return false;
                
                // Check completion timestamp
                String completedKey = "completed:" + questId;
                if (!quest.isRepeatable() && playerQuests.containsKey(completedKey)) {
                    return false;
                }
                
                if (playerQuests.containsKey(completedKey)) {
                    QuestProgress progress = playerQuests.get(completedKey);
                    long completionTime = progress.getCompletionTimestamp();
                    long cooldownMs = quest.getCooldownHours() * 60 * 60 * 1000L;
                    
                    if (System.currentTimeMillis() - completionTime < cooldownMs) {
                        return false;
                    }
                }
                
                return true;
            })
            .collect(Collectors.toList());
        
        // Shuffle available quests to get some randomness
        Collections.shuffle(availableQuests);
        
        // Assign quests
        int assignedCount = 0;
        for (String questId : availableQuests) {
            if (assignedCount >= slotsAvailable) break;
            
            Quest quest = quests.get(questId);
            playerQuests.put(questId, new QuestProgress(questId));
            
            ChatF.empty()
                .append("[Daily Quest] ", ChatF.C_GOLD)
                .appendBold(quest.getTitle(), ChatF.C_YELLOW)
                .nl()
                .append(quest.getDescription(), ChatF.C_GRAY)
                .sendTo(player);
            
            assignedCount++;
        }
        
        if (assignedCount > 0) {
            playerQuestData.put(playerId, playerQuests);
            savePlayerQuestData(player);
        }
    }

    NotCommand questCommand() {
        NotCommand command = NotCommand.of("quest", cmd -> {
            Player player = cmd.getPlayer();
            
            if (!player.hasPermission(PERMISSION_QUESTS)) {
                ChatF.empty()
                    .appendBold("You don't have permission to use quests!", ChatF.C_RED)
                    .sendTo(player);
                return;
            }
            
            showQuestList(player);
        });
        
        // Show active quests
        command.literalArg("list", arg -> {
            Player player = arg.getPlayer();
            
            if (!player.hasPermission(PERMISSION_QUESTS)) {
                ChatF.empty()
                    .appendBold("You don't have permission to use quests!", ChatF.C_RED)
                    .sendTo(player);
                return;
            }
            
            showQuestList(player);
        });
        
        // Show quest details
        NotLiteralArg infoArg = command.literalArg("info", info -> {
            ChatF.empty()
                .appendBold("Usage: /quest info <questId>", ChatF.C_RED)
                .sendTo(info.getPlayer());
        });

        infoArg.stringArg("questId", questIdArg -> {
            Player player = questIdArg.getPlayer();
            String questId = questIdArg.get();
            
            if (!player.hasPermission(PERMISSION_QUESTS)) {
                ChatF.empty()
                    .appendBold("You don't have permission to use quests!", ChatF.C_RED)
                    .sendTo(player);
                return;
            }
            
            Quest quest = quests.get(questId);
            if (quest == null) {
                ChatF.empty()
                    .appendBold("Quest not found: " + questId, ChatF.C_RED)
                    .sendTo(player);
                return;
            }
            
            showQuestDetails(player, quest);
        });
        
        // Assign a quest to player
        NotLiteralArg takeArg = command.literalArg("take", take -> {
            ChatF.empty()
                .appendBold("Usage: /quest take <questId>", ChatF.C_RED)
                .sendTo(take.getPlayer());
        });

        takeArg.stringArg("questId", questIdArg -> {
            Player player = questIdArg.getPlayer();
            String questId = questIdArg.get();
            
            if (!player.hasPermission(PERMISSION_QUESTS)) {
                ChatF.empty()
                    .appendBold("You don't have permission to use quests!", ChatF.C_RED)
                    .sendTo(player);
                return;
            }
            
            assignQuestToPlayer(player, questId);
        });
        
        // Admin commands
        NotLiteralArg adminArg = command.literalArg("admin", admin -> {
            ChatF.empty()
                .appendBold("Quest Admin Commands:")
                .nl()
                .append("/quest admin give <player> <questId>", ChatF.C_YELLOW)
                .nl()
                .append("/quest admin complete <player> <questId>", ChatF.C_YELLOW)
                .nl()
                .append("/quest admin reset <player>", ChatF.C_YELLOW)
                .nl()
                .append("/quest admin reload", ChatF.C_YELLOW)
                .sendTo(admin.getPlayer());
        });

        NotLiteralArg adminGiveArg = adminArg.literalArg("give", give -> {
            ChatF.empty()
                .appendBold("Usage: /quest admin give <player> <questId>", ChatF.C_RED)
                .sendTo(give.getPlayer());
        });

        NotPlayerArg adminGivePlayerArg = adminGiveArg.playerArg("player", playerArg -> {
            ChatF.empty()
                .appendBold("Usage: /quest admin give <player> <questId>", ChatF.C_RED)
                .sendTo(playerArg.getPlayer());
        });

        adminGivePlayerArg.stringArg("questId", questIdArg -> {
            Player sender = questIdArg.getPlayer();
            Player target = adminGivePlayerArg.get();
            String questId = questIdArg.get();
            
            if (!sender.hasPermission(PERMISSION_ADMIN)) {
                ChatF.empty()
                    .appendBold("You don't have permission!", ChatF.C_RED)
                    .sendTo(sender);
                return;
            }
            
            if (assignQuestToPlayer(target, questId)) {
                ChatF.empty()
                    .appendBold("Quest assigned to " + target.getName(), ChatF.C_GREEN)
                    .sendTo(sender);
            }
        });
        
        // Admin - complete quest for player
        NotLiteralArg adminCompleteArg = adminArg.literalArg("complete", complete -> {
            ChatF.empty()
                .appendBold("Usage: /quest admin complete <player> <questId>", ChatF.C_RED)
                .sendTo(complete.getPlayer());
        });

        NotPlayerArg adminCompletePlayerArg = adminCompleteArg.playerArg("player", playerArg -> {
            ChatF.empty()
                .appendBold("Usage: /quest admin complete <player> <questId>", ChatF.C_RED)
                .sendTo(playerArg.getPlayer());
        });

        adminCompletePlayerArg.stringArg("questId", questIdArg -> {
            Player sender = questIdArg.getPlayer();
            Player target = adminCompletePlayerArg.get();
            String questId = questIdArg.get();
            
            if (!sender.hasPermission(PERMISSION_ADMIN)) {
                ChatF.empty()
                    .appendBold("You don't have permission!", ChatF.C_RED)
                    .sendTo(sender);
                return;
            }
            
            UUID targetId = target.getUniqueId();
            if (!playerQuestData.containsKey(targetId) || 
                !playerQuestData.get(targetId).containsKey(questId)) {
                ChatF.empty()
                    .appendBold("Player doesn't have that quest active!", ChatF.C_RED)
                    .sendTo(sender);
                return;
            }
            
            completeQuest(target, questId);
            ChatF.empty()
                .appendBold("Quest completed for " + target.getName(), ChatF.C_GREEN)
                .sendTo(sender);
        });
        
        // Admin - reset player quests
        NotLiteralArg adminResetArg = adminArg.literalArg("reset", reset -> {
            ChatF.empty()
                .appendBold("Usage: /quest admin reset <player>", ChatF.C_RED)
                .sendTo(reset.getPlayer());
        });

        adminResetArg.playerArg("player", playerArg -> {
            Player sender = playerArg.getPlayer();
            Player target = playerArg.get();
            
            if (!sender.hasPermission(PERMISSION_ADMIN)) {
                ChatF.empty()
                    .appendBold("You don't have permission!", ChatF.C_RED)
                    .sendTo(sender);
                return;
            }
            
            UUID targetId = target.getUniqueId();
            playerQuestData.remove(targetId);
            target.getPersistentDataContainer().remove(questDataKey);
            
            ChatF.empty()
                .appendBold("Reset all quests for " + target.getName(), ChatF.C_GREEN)
                .sendTo(sender);
        });
        
        // Reload configuration
        command.literalArg("reload", arg -> {
            Player player = arg.getPlayer();
            
            if (!player.hasPermission(PERMISSION_RELOAD)) {
                ChatF.empty()
                    .appendBold("You don't have permission to reload quests!", ChatF.C_RED)
                    .sendTo(player);
                return;
            }
            
            reloadWithFiles();
            
            ChatF.empty()
                .appendBold("Quests configuration reloaded!", ChatF.C_GREEN)
                .sendTo(player);
        });
        
        return command;
    }

    private void showQuestList(Player player) {
        UUID playerId = player.getUniqueId();
        Map<String, QuestProgress> playerQuests = playerQuestData.getOrDefault(playerId, Collections.emptyMap());
        
        // Get active quests
        List<Map.Entry<String, QuestProgress>> activeQuests = playerQuests.entrySet().stream()
            .filter(entry -> !entry.getKey().startsWith("completed:"))
            .collect(Collectors.toList());
        
        ChatF message = ChatF.empty()
            .appendBold("[Quests] ", ChatF.C_GOLD)
            .append("Your active quests: ", ChatF.C_YELLOW);
        
        if (activeQuests.isEmpty()) {
            message.append("None", ChatF.C_GRAY);
        } else {
            message.nl();
            
            for (Map.Entry<String, QuestProgress> entry : activeQuests) {
                String questId = entry.getKey();
                Quest quest = quests.get(questId);
                if (quest == null) continue;
                
                // Calculate overall progress
                QuestProgress progress = entry.getValue();
                int completedObjectives = 0;
                int totalObjectives = quest.getObjectives().size();
                
                for (QuestObjective objective : quest.getObjectives()) {
                    if (progress.getObjectiveProgress(objective.getId()) >= objective.getAmount()) {
                        completedObjectives++;
                    }
                }
                
                // Add to message
                message.append("• ", ChatF.C_GOLD)
                    .append(quest.getTitle(), ChatF.C_YELLOW)
                    .append(" - ", ChatF.C_WHITE)
                    .append(completedObjectives + "/" + totalObjectives + " objectives", 
                        completedObjectives == totalObjectives ? ChatF.C_GREEN : ChatF.C_GRAY)
                    .append(" [/quest info " + questId + "]", ChatF.C_AQUA)
                    .nl();
            }
        }
        
        message.append("Type ", ChatF.C_YELLOW)
            .append("/quest take <id>", ChatF.C_AQUA)
            .append(" to take a new quest.", ChatF.C_YELLOW);
        
        message.sendTo(player);
    }

    private void showQuestDetails(Player player, Quest quest) {
        UUID playerId = player.getUniqueId();
        QuestProgress progress = null;
        
        if (playerQuestData.containsKey(playerId)) {
            progress = playerQuestData.get(playerId).get(quest.getId());
        }
        
        ChatF message = ChatF.empty()
            .appendBold("[Quest] ", ChatF.C_GOLD)
            .appendBold(quest.getTitle(), ChatF.C_YELLOW)
            .nl()
            .append(quest.getDescription(), ChatF.C_GRAY)
            .nl().nl()
            .appendBold("Objectives:", ChatF.C_YELLOW)
            .nl();
        
        for (QuestObjective objective : quest.getObjectives()) {
            int currentProgress = progress != null ? progress.getObjectiveProgress(objective.getId()) : 0;
            boolean completed = currentProgress >= objective.getAmount();
            
            message.append("• ", ChatF.C_GOLD)
                .append(objective.getDescription(), completed ? ChatF.C_GREEN : ChatF.C_GRAY)
                .append(" - ", ChatF.C_WHITE)
                .append(currentProgress + "/" + objective.getAmount(), completed ? ChatF.C_GREEN : ChatF.C_YELLOW)
                .nl();
        }
        
        message.nl().appendBold("Rewards:", ChatF.C_YELLOW).nl();
        
        Map<String, Object> rewards = quest.getRewards();
        if (rewards.isEmpty()) {
            message.append("• None", ChatF.C_GRAY).nl();
        } else {
            for (Map.Entry<String, Object> entry : rewards.entrySet()) {
                String rewardType = entry.getKey();
                Object rewardValue = entry.getValue();
                
                message.append("• ", ChatF.C_GOLD);
                
                switch (rewardType) {
                    case "money":
                        message.append("Money: $" + rewardValue, ChatF.C_GREEN);
                        break;
                    case "xp":
                        message.append("XP: " + rewardValue, ChatF.C_GREEN);
                        break;
                    case "item":
                        if (rewardValue instanceof String) {
                            message.append("Item: " + rewardValue, ChatF.C_GREEN);
                        } else if (rewardValue instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<String> items = (List<String>) rewardValue;
                            message.append("Items: " + String.join(", ", items), ChatF.C_GREEN);
                        }
                        break;
                    case "command":
                        message.append("Special reward", ChatF.C_GREEN);
                        break;
                    default:
                        message.append(rewardType + ": " + rewardValue, ChatF.C_GREEN);
                }
                
                message.nl();
            }
        }
        
        if (quest.isRepeatable()) {
            message.nl()
                .append("This quest is repeatable", ChatF.C_YELLOW)
                .append(" (Cooldown: " + quest.getCooldownHours() + " hours)", ChatF.C_GRAY);
        }
        
        message.sendTo(player);
    }

    public boolean assignQuestToPlayer(Player player, String questId) {
        Quest quest = quests.get(questId);
        if (quest == null) {
            ChatF.empty()
                .appendBold("Quest not found: " + questId, ChatF.C_RED)
                .sendTo(player);
            return false;
        }
        
        UUID playerId = player.getUniqueId();
        Map<String, QuestProgress> playerQuests = playerQuestData.computeIfAbsent(playerId, k -> new HashMap<>());
        
        // Check if already has this quest
        if (playerQuests.containsKey(questId)) {
            ChatF.empty()
                .appendBold("You already have this quest!", ChatF.C_RED)
                .sendTo(player);
            return false;
        }
        
        // Check max active quests
        int activeQuestCount = (int) playerQuests.keySet().stream()
            .filter(id -> !id.startsWith("completed:"))
            .count();
        
        if (activeQuestCount >= maxActiveQuests) {
            ChatF.empty()
                .appendBold("You have reached the maximum number of active quests (" + maxActiveQuests + ")!", ChatF.C_RED)
                .sendTo(player);
            return false;
        }
        
        // Check if on cooldown
        String completedKey = "completed:" + questId;
        if (playerQuests.containsKey(completedKey)) {
            if (!quest.isRepeatable()) {
                ChatF.empty()
                    .appendBold("You have already completed this quest!", ChatF.C_RED)
                    .sendTo(player);
                return false;
            }
            
            QuestProgress completedProgress = playerQuests.get(completedKey);
            long completionTime = completedProgress.getCompletionTimestamp();
            long cooldownMs = quest.getCooldownHours() * 60 * 60 * 1000L;
            
            if (System.currentTimeMillis() - completionTime < cooldownMs) {
                long remainingMs = cooldownMs - (System.currentTimeMillis() - completionTime);
                long remainingHours = remainingMs / (60 * 60 * 1000);
                long remainingMinutes = (remainingMs % (60 * 60 * 1000)) / (60 * 1000);
                
                ChatF.empty()
                    .appendBold("This quest is on cooldown!", ChatF.C_RED)
                    .nl()
                    .append("Available again in: " + remainingHours + "h " + remainingMinutes + "m", ChatF.C_GRAY)
                    .sendTo(player);
                return false;
            }
        }
        
        // Assign quest
        playerQuests.put(questId, new QuestProgress(questId));
        savePlayerQuestData(player);
        
        ChatF.empty()
            .append("[Quest Accepted] ", ChatF.C_GOLD)
            .appendBold(quest.getTitle(), ChatF.C_GREEN)
            .nl()
            .append(quest.getDescription(), ChatF.C_GRAY)
            .sendTo(player);
        
        return true;
    }

    // New methods to support the GUI functionality
    
    public Quest getQuest(String questId) {
        return quests.get(questId);
    }
    
    public List<String> getQuestCategories() {
        return new ArrayList<>(questCategories.keySet());
    }
    
    public List<Quest> getQuestsByCategory(String category) {
        List<String> questIds = questCategories.getOrDefault(category, Collections.emptyList());
        return questIds.stream()
            .map(this::getQuest)
            .filter(quest -> quest != null)
            .collect(Collectors.toList());
    }
    
    public int getActiveQuestCount(Player player) {
        UUID playerId = player.getUniqueId();
        if (!playerQuestData.containsKey(playerId)) {
            return 0;
        }
        
        return (int) playerQuestData.get(playerId).keySet().stream()
            .filter(id -> !id.startsWith("completed:"))
            .count();
    }
    
    public int getMaxActiveQuests() {
        return maxActiveQuests;
    }
    
    public List<Map.Entry<String, QuestProgress>> getActiveQuests(Player player) {
        UUID playerId = player.getUniqueId();
        if (!playerQuestData.containsKey(playerId)) {
            return Collections.emptyList();
        }
        
        return playerQuestData.get(playerId).entrySet().stream()
            .filter(entry -> !entry.getKey().startsWith("completed:"))
            .collect(Collectors.toList());
    }
    
    public List<Map.Entry<String, QuestProgress>> getCompletedQuests(Player player) {
        UUID playerId = player.getUniqueId();
        if (!playerQuestData.containsKey(playerId)) {
            return Collections.emptyList();
        }
        
        return playerQuestData.get(playerId).entrySet().stream()
            .filter(entry -> entry.getKey().startsWith("completed:"))
            .collect(Collectors.toList());
    }
    
    public List<Quest> getAvailableQuests(Player player) {
        UUID playerId = player.getUniqueId();
        if (!playerQuestData.containsKey(playerId)) {
            loadPlayerQuestData(player);
        }
        
        Map<String, QuestProgress> playerQuests = playerQuestData.get(playerId);
        
        return quests.values().stream()
            .filter(quest -> {
                String questId = quest.getId();
                
                // Not already active
                if (playerQuests.containsKey(questId)) {
                    return false;
                }
                
                // Not completed (for non-repeatable quests)
                String completedKey = "completed:" + questId;
                if (!quest.isRepeatable() && playerQuests.containsKey(completedKey)) {
                    return false;
                }
                
                // Not on cooldown
                if (playerQuests.containsKey(completedKey)) {
                    QuestProgress progress = playerQuests.get(completedKey);
                    long completionTime = progress.getCompletionTimestamp();
                    long cooldownMs = quest.getCooldownHours() * 60 * 60 * 1000L;
                    
                    if (System.currentTimeMillis() - completionTime < cooldownMs) {
                        return false;
                    }
                }
                
                return true;
            })
            .collect(Collectors.toList());
    }
    
    public QuestStatus getQuestStatus(Player player, String questId) {
        UUID playerId = player.getUniqueId();
        if (!playerQuestData.containsKey(playerId)) {
            return QuestStatus.AVAILABLE;
        }
        
        Map<String, QuestProgress> playerQuests = playerQuestData.get(playerId);
        
        // Check if active
        if (playerQuests.containsKey(questId)) {
            return QuestStatus.ACTIVE;
        }
        
        // Check if completed or on cooldown
        String completedKey = "completed:" + questId;
        if (playerQuests.containsKey(completedKey)) {
            Quest quest = getQuest(questId);
            if (quest == null) {
                return QuestStatus.COMPLETED;
            }
            
            if (!quest.isRepeatable()) {
                return QuestStatus.COMPLETED;
            }
            
            QuestProgress progress = playerQuests.get(completedKey);
            long completionTime = progress.getCompletionTimestamp();
            long cooldownMs = quest.getCooldownHours() * 60 * 60 * 1000L;
            
            if (System.currentTimeMillis() - completionTime < cooldownMs) {
                return QuestStatus.COOLDOWN;
            }
        }
        
        // Otherwise it's available
        return QuestStatus.AVAILABLE;
    }
    
    public QuestProgress getQuestProgress(Player player, String questId) {
        UUID playerId = player.getUniqueId();
        if (!playerQuestData.containsKey(playerId)) {
            return null;
        }
        
        Map<String, QuestProgress> playerQuests = playerQuestData.get(playerId);
        return playerQuests.get(questId);
    }
    
    public boolean isQuestOnCooldown(Player player, String questId) {
        UUID playerId = player.getUniqueId();
        if (!playerQuestData.containsKey(playerId)) {
            return false;
        }
        
        Map<String, QuestProgress> playerQuests = playerQuestData.get(playerId);
        String completedKey = "completed:" + questId;
        
        if (!playerQuests.containsKey(completedKey)) {
            return false;
        }
        
        Quest quest = getQuest(questId);
        if (quest == null || !quest.isRepeatable()) {
            return false;
        }
        
        QuestProgress progress = playerQuests.get(completedKey);
        long completionTime = progress.getCompletionTimestamp();
        long cooldownMs = quest.getCooldownHours() * 60 * 60 * 1000L;
        
        return System.currentTimeMillis() - completionTime < cooldownMs;
    }
    
    public long getQuestCooldownRemaining(Player player, String questId) {
        UUID playerId = player.getUniqueId();
        if (!playerQuestData.containsKey(playerId)) {
            return 0;
        }
        
        Map<String, QuestProgress> playerQuests = playerQuestData.get(playerId);
        String completedKey = "completed:" + questId;
        
        if (!playerQuests.containsKey(completedKey)) {
            return 0;
        }
        
        Quest quest = getQuest(questId);
        if (quest == null) {
            return 0;
        }
        
        QuestProgress progress = playerQuests.get(completedKey);
        long completionTime = progress.getCompletionTimestamp();
        long cooldownMs = quest.getCooldownHours() * 60 * 60 * 1000L;
        long elapsedMs = System.currentTimeMillis() - completionTime;
        
        return Math.max(0, cooldownMs - elapsedMs);
    }
    
    public boolean abandonQuest(Player player, String questId) {
        UUID playerId = player.getUniqueId();
        if (!playerQuestData.containsKey(playerId)) {
            return false;
        }
        
        Map<String, QuestProgress> playerQuests = playerQuestData.get(playerId);
        
        if (!playerQuests.containsKey(questId)) {
            ChatF.empty()
                .appendBold("You don't have that quest!", ChatF.C_RED)
                .sendTo(player);
            return false;
        }
        
        Quest quest = getQuest(questId);
        if (quest == null) {
            return false;
        }
        
        playerQuests.remove(questId);
        savePlayerQuestData(player);
        
        ChatF.empty()
            .append("[Quest Abandoned] ", ChatF.C_RED)
            .appendBold(quest.getTitle(), ChatF.C_YELLOW)
            .sendTo(player);
        
        return true;
    }
    
    public void trackQuest(Player player, String questId) {
        UUID playerId = player.getUniqueId();
        if (!playerQuestData.containsKey(playerId)) {
            return;
        }
        
        Map<String, QuestProgress> playerQuests = playerQuestData.get(playerId);
        
        if (!playerQuests.containsKey(questId)) {
            return;
        }
        
        Quest quest = getQuest(questId);
        if (quest == null) {
            return;
        }
        
        QuestProgress progress = playerQuests.get(questId);
        
        ChatF message = ChatF.empty()
            .append("[Quest Tracking] ", ChatF.C_GOLD)
            .appendBold(quest.getTitle(), ChatF.C_YELLOW)
            .nl();
        
        for (QuestObjective objective : quest.getObjectives()) {
            int currentProgress = progress.getObjectiveProgress(objective.getId());
            boolean completed = currentProgress >= objective.getAmount();
            
            message.append("• ", ChatF.C_GOLD)
                .append(objective.getDescription(), completed ? ChatF.C_GREEN : ChatF.C_GRAY)
                .append(" - ", ChatF.C_WHITE)
                .append(currentProgress + "/" + objective.getAmount(), completed ? ChatF.C_GREEN : ChatF.C_YELLOW)
                .nl();
        }
        
        message.sendTo(player);
    }

    // Quest data model classes
    public static class Quest {
        private final String id;
        private final String title;
        private final String description;
        private final String category;
        private final List<QuestObjective> objectives;
        private final Map<String, Object> rewards;
        private final boolean repeatable;
        private final int cooldownHours;
        
        public Quest(String id, String title, String description, String category, 
                    List<QuestObjective> objectives, Map<String, Object> rewards,
                    boolean repeatable, int cooldownHours) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.category = category;
            this.objectives = objectives;
            this.rewards = rewards;
            this.repeatable = repeatable;
            this.cooldownHours = cooldownHours;
        }
        
        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getCategory() { return category; }
        public List<QuestObjective> getObjectives() { return objectives; }
        public Map<String, Object> getRewards() { return rewards; }
        public boolean isRepeatable() { return repeatable; }
        public int getCooldownHours() { return cooldownHours; }
    }
    
    public static class QuestObjective {
        private final String id;
        private final String type;
        private final String targetId;
        private final int amount;
        private final String description;
        
        public QuestObjective(String id, String type, String targetId, int amount, String description) {
            this.id = id;
            this.type = type;
            this.targetId = targetId;
            this.amount = amount;
            this.description = description;
        }
        
        public String getId() { return id; }
        public String getType() { return type; }
        public String getTargetId() { return targetId; }
        public int getAmount() { return amount; }
        public String getDescription() { return description; }
    }
    
    public static class QuestProgress {
        private final String questId;
        private final Map<String, Integer> objectiveProgress;
        private final long completionTimestamp;
        
        public QuestProgress(String questId) {
            this.questId = questId;
            this.objectiveProgress = new HashMap<>();
            this.completionTimestamp = 0;
        }
        
        public QuestProgress(String questId, long completionTimestamp) {
            this.questId = questId;
            this.objectiveProgress = new HashMap<>();
            this.completionTimestamp = completionTimestamp;
        }
        
        public String getQuestId() { return questId; }
        
        public Map<String, Integer> getObjectiveProgress() { return objectiveProgress; }
        
        public int getObjectiveProgress(String objectiveId) {
            return objectiveProgress.getOrDefault(objectiveId, 0);
        }
        
        public void setObjectiveProgress(String objectiveId, int progress) {
            objectiveProgress.put(objectiveId, progress);
        }
        
        public long getCompletionTimestamp() { return completionTimestamp; }
    }
}