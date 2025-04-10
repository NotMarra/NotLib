package com.notmarra.notlib.quests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.notmarra.notlib.extensions.NotPlugin;
import com.notmarra.notlib.utils.ChatF;
import com.notmarra.notlib.utils.gui.NotGUI;
import com.notmarra.notlib.utils.gui.NotGUIContainer;
import com.notmarra.notlib.utils.gui.NotGUIItem;

import net.kyori.adventure.text.format.TextColor;

import com.notmarra.notlib.quests.NotQuestListener.Quest;
import com.notmarra.notlib.quests.NotQuestListener.QuestObjective;
import com.notmarra.notlib.quests.NotQuestListener.QuestProgress;

public class NotQuestGUI {
    private final NotPlugin plugin;
    private final NotQuestListener questListener;
    
    // GUI pages
    private static final int QUESTS_PER_PAGE = 45; // 9x5 grid for quests
    
    // Materials for GUI elements
    private static final Material MATERIAL_ACTIVE_QUEST = Material.BOOK;
    private static final Material MATERIAL_AVAILABLE_QUEST = Material.WRITABLE_BOOK;
    private static final Material MATERIAL_COMPLETED_QUEST = Material.ENCHANTED_BOOK;
    private static final Material MATERIAL_CATEGORY_ICON = Material.CHEST;
    private static final Material MATERIAL_OBJECTIVE_INCOMPLETE = Material.RED_STAINED_GLASS_PANE;
    private static final Material MATERIAL_OBJECTIVE_COMPLETE = Material.LIME_STAINED_GLASS_PANE;
    private static final Material MATERIAL_NEXT_PAGE = Material.ARROW;
    private static final Material MATERIAL_PREV_PAGE = Material.ARROW;
    private static final Material MATERIAL_BACK = Material.BARRIER;
    private static final Material MATERIAL_TAKE_QUEST = Material.EMERALD;
    private static final Material MATERIAL_CANCEL_QUEST = Material.REDSTONE;
    private static final Material MATERIAL_BACKGROUND = Material.GRAY_STAINED_GLASS_PANE;
    
    public NotQuestGUI(NotPlugin plugin, NotQuestListener questListener) {
        this.plugin = plugin;
        this.questListener = questListener;
    }
    
    /**
     * Main quest menu showing categories
     */
    public void openMainMenu(Player player) {
        NotGUI gui = NotGUI.create()
            .title(ChatF.of("Quest Journal", ChatF.C_GOLD))
            .rows(6);
            
        // Header with player info
        NotGUIContainer header = gui.createContainer(0, 0, 9, 1);
        
        // Add player head
        header.addButton(Material.PLAYER_HEAD, 
            ChatF.empty().appendBold(player.getName() + "'s Quests", ChatF.C_GOLD), 
            4, 0, 
            (event, container) -> {});
        
        // Categories section - get unique categories
        List<String> categories = questListener.getQuestCategories();
        
        // Create category buttons
        NotGUIContainer categoryContainer = gui.createContainer(0, 1, 9, 3);
        int slot = 0;
        
        for (String category : categories) {
            if (slot >= 9 * 3) break; // Limit to container size
            
            Material iconMaterial = getCategoryIcon(category);
            String displayName = formatCategoryName(category);
            
            categoryContainer.addButton(
                iconMaterial, 
                ChatF.empty().appendBold(displayName, ChatF.C_YELLOW), 
                slot,
                (event, container) -> {
                    openCategoryPage(player, category, 0);
                }
            );
            
            slot++;
        }
        
        // Add active quests button
        NotGUIContainer footer = gui.createContainer(0, 4, 9, 1);
        footer.addButton(
            Material.COMPASS, 
            ChatF.empty().appendBold("Your Active Quests", ChatF.C_GREEN), 
            4, 0,
            (event, container) -> {
                openActiveQuestsPage(player, 0);
            }
        );
        
        // Add available quests button 
        footer.addButton(
            Material.MAP, 
            ChatF.empty().appendBold("Available Quests", ChatF.C_AQUA), 
            2, 0,
            (event, container) -> {
                openAvailableQuestsPage(player, 0);
            }
        );
        
        // Add completed quests button
        footer.addButton(
            Material.FILLED_MAP, 
            ChatF.empty().appendBold("Completed Quests", ChatF.C_LIGHTPURPLE), 
            6, 0,
            (event, container) -> {
                openCompletedQuestsPage(player, 0);
            }
        );
        
        // Bottom info bar
        NotGUIContainer infoBar = gui.createContainer(0, 5, 9, 1);
        fillBackground(infoBar, MATERIAL_BACKGROUND);
        
        infoBar.addButton(
            Material.BOOK, 
            ChatF.empty().append("Active Quests: ", ChatF.C_WHITE)
                .append(questListener.getActiveQuestCount(player) + "/" + questListener.getMaxActiveQuests(), ChatF.C_YELLOW), 
            4, 0,
            (event, container) -> {}
        );
        
        gui.open(player);
    }
    
    /**
     * Shows all quests in a specific category
     */
    private void openCategoryPage(Player player, String category, int page) {
        List<Quest> categoryQuests = questListener.getQuestsByCategory(category);
        
        NotGUI gui = NotGUI.create()
            .title(ChatF.of("Quests: " + formatCategoryName(category), ChatF.C_GOLD))
            .rows(6);
        
        // Header
        NotGUIContainer header = gui.createContainer(0, 0, 9, 1);
        header.addButton(
            getCategoryIcon(category),
            ChatF.empty().appendBold(formatCategoryName(category) + " Quests", ChatF.C_GOLD),
            4, 0,
            (event, container) -> {}
        );
        
        // Main content area with paginated quests
        NotGUIContainer content = gui.createContainer(0, 1, 9, 4);
        
        int startIndex = page * QUESTS_PER_PAGE;
        int endIndex = Math.min(startIndex + QUESTS_PER_PAGE, categoryQuests.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            Quest quest = categoryQuests.get(i);
            int slot = i - startIndex;
            
            QuestStatus status = questListener.getQuestStatus(player, quest.getId());
            addQuestButton(content, quest, slot, status, player, (event, container) -> {
                openQuestDetails(player, quest.getId());
            });
        }
        
        // Footer with navigation
        NotGUIContainer footer = gui.createContainer(0, 5, 9, 1);
        fillBackground(footer, MATERIAL_BACKGROUND);
        
        // Back button
        footer.addButton(
            MATERIAL_BACK,
            ChatF.empty().append("Back to Categories", ChatF.C_RED),
            0, 0,
            (event, container) -> openMainMenu(player)
        );
        
        // Pagination
        int totalPages = (int) Math.ceil((double) categoryQuests.size() / QUESTS_PER_PAGE);
        
        if (page > 0) {
            footer.addButton(
                MATERIAL_PREV_PAGE,
                ChatF.empty().append("Previous Page", ChatF.C_YELLOW),
                3, 0,
                (event, container) -> openCategoryPage(player, category, page - 1)
            );
        }
        
        if (page < totalPages - 1) {
            footer.addButton(
                MATERIAL_NEXT_PAGE,
                ChatF.empty().append("Next Page", ChatF.C_YELLOW),
                5, 0,
                (event, container) -> openCategoryPage(player, category, page + 1)
            );
        }
        
        // Page indicator
        footer.addButton(
            Material.PAPER,
            ChatF.empty().append("Page " + (page + 1) + "/" + Math.max(1, totalPages), ChatF.C_WHITE),
            4, 0,
            (event, container) -> {}
        );
        
        gui.open(player);
    }
    
    /**
     * Shows active quests for the player
     */
    private void openActiveQuestsPage(Player player, int page) {
        List<Map.Entry<String, QuestProgress>> activeQuests = questListener.getActiveQuests(player);
        
        NotGUI gui = NotGUI.create()
            .title(ChatF.of("Active Quests", ChatF.C_GREEN))
            .rows(6);
        
        // Header
        NotGUIContainer header = gui.createContainer(0, 0, 9, 1);
        header.addButton(
            Material.COMPASS,
            ChatF.empty().appendBold("Your Active Quests", ChatF.C_GREEN),
            4, 0,
            (event, container) -> {}
        );
        
        // Main content
        NotGUIContainer content = gui.createContainer(0, 1, 9, 4);
        
        // Handle pagination
        int startIndex = page * QUESTS_PER_PAGE;
        int endIndex = Math.min(startIndex + QUESTS_PER_PAGE, activeQuests.size());
        
        // Add quests to the GUI
        if (activeQuests.isEmpty()) {
            content.addButton(
                Material.BARRIER,
                ChatF.empty().append("You have no active quests", ChatF.C_GRAY),
                4, 2,
                (event, container) -> {}
            );
        } else {
            for (int i = startIndex; i < endIndex; i++) {
                Map.Entry<String, QuestProgress> entry = activeQuests.get(i);
                String questId = entry.getKey();
                int slot = i - startIndex;
                
                Quest quest = questListener.getQuest(questId);
                if (quest == null) continue;
                
                addQuestButton(content, quest, slot, QuestStatus.ACTIVE, player, (event, container) -> {
                    openQuestDetails(player, questId);
                });
            }
        }
        
        // Footer with navigation
        NotGUIContainer footer = gui.createContainer(0, 5, 9, 1);
        fillBackground(footer, MATERIAL_BACKGROUND);
        
        // Back button
        footer.addButton(
            MATERIAL_BACK,
            ChatF.empty().append("Back to Main Menu", ChatF.C_RED),
            0, 0,
            (event, container) -> openMainMenu(player)
        );
        
        // Pagination
        int totalPages = (int) Math.ceil((double) activeQuests.size() / QUESTS_PER_PAGE);
        
        if (page > 0) {
            footer.addButton(
                MATERIAL_PREV_PAGE,
                ChatF.empty().append("Previous Page", ChatF.C_YELLOW),
                3, 0,
                (event, container) -> openActiveQuestsPage(player, page - 1)
            );
        }
        
        if (page < totalPages - 1) {
            footer.addButton(
                MATERIAL_NEXT_PAGE,
                ChatF.empty().append("Next Page", ChatF.C_YELLOW),
                5, 0,
                (event, container) -> openActiveQuestsPage(player, page + 1)
            );
        }
        
        // Page indicator
        footer.addButton(
            Material.PAPER,
            ChatF.empty().append("Page " + (page + 1) + "/" + Math.max(1, totalPages), ChatF.C_WHITE),
            4, 0,
            (event, container) -> {}
        );
        
        gui.open(player);
    }
    
    /**
     * Shows available quests that player can accept
     */
    private void openAvailableQuestsPage(Player player, int page) {
        List<Quest> availableQuests = questListener.getAvailableQuests(player);
        
        NotGUI gui = NotGUI.create()
            .title(ChatF.of("Available Quests", ChatF.C_AQUA))
            .rows(6);
        
        // Header
        NotGUIContainer header = gui.createContainer(0, 0, 9, 1);
        header.addButton(
            Material.MAP,
            ChatF.empty().appendBold("Available Quests", ChatF.C_AQUA),
            4, 0,
            (event, container) -> {}
        );
        
        // Main content
        NotGUIContainer content = gui.createContainer(0, 1, 9, 4);
        
        // Handle pagination
        int startIndex = page * QUESTS_PER_PAGE;
        int endIndex = Math.min(startIndex + QUESTS_PER_PAGE, availableQuests.size());
        
        // Add quests to the GUI
        if (availableQuests.isEmpty()) {
            content.addButton(
                Material.BARRIER,
                ChatF.empty().append("No available quests", ChatF.C_GRAY),
                4, 2,
                (event, container) -> {}
            );
        } else {
            for (int i = startIndex; i < endIndex; i++) {
                Quest quest = availableQuests.get(i);
                int slot = i - startIndex;
                
                addQuestButton(content, quest, slot, QuestStatus.AVAILABLE, player, (event, container) -> {
                    openQuestDetails(player, quest.getId());
                });
            }
        }
        
        // Footer with navigation
        NotGUIContainer footer = gui.createContainer(0, 5, 9, 1);
        fillBackground(footer, MATERIAL_BACKGROUND);
        
        // Back button
        footer.addButton(
            MATERIAL_BACK,
            ChatF.empty().append("Back to Main Menu", ChatF.C_RED),
            0, 0,
            (event, container) -> openMainMenu(player)
        );
        
        // Pagination
        int totalPages = (int) Math.ceil((double) availableQuests.size() / QUESTS_PER_PAGE);
        
        if (page > 0) {
            footer.addButton(
                MATERIAL_PREV_PAGE,
                ChatF.empty().append("Previous Page", ChatF.C_YELLOW),
                3, 0,
                (event, container) -> openAvailableQuestsPage(player, page - 1)
            );
        }
        
        if (page < totalPages - 1) {
            footer.addButton(
                MATERIAL_NEXT_PAGE,
                ChatF.empty().append("Next Page", ChatF.C_YELLOW),
                5, 0,
                (event, container) -> openAvailableQuestsPage(player, page + 1)
            );
        }
        
        // Page indicator
        footer.addButton(
            Material.PAPER,
            ChatF.empty().append("Page " + (page + 1) + "/" + Math.max(1, totalPages), ChatF.C_WHITE),
            4, 0,
            (event, container) -> {}
        );
        
        gui.open(player);
    }
    
    /**
     * Shows completed quests
     */
    private void openCompletedQuestsPage(Player player, int page) {
        List<Map.Entry<String, QuestProgress>> completedQuests = questListener.getCompletedQuests(player);
        
        NotGUI gui = NotGUI.create()
            .title(ChatF.of("Completed Quests", ChatF.C_LIGHTPURPLE))
            .rows(6);
        
        // Header
        NotGUIContainer header = gui.createContainer(0, 0, 9, 1);
        header.addButton(
            Material.FILLED_MAP,
            ChatF.empty().appendBold("Completed Quests", ChatF.C_LIGHTPURPLE),
            4, 0,
            (event, container) -> {}
        );
        
        // Main content
        NotGUIContainer content = gui.createContainer(0, 1, 9, 4);
        
        // Handle pagination
        int startIndex = page * QUESTS_PER_PAGE;
        int endIndex = Math.min(startIndex + QUESTS_PER_PAGE, completedQuests.size());
        
        // Add quests to the GUI
        if (completedQuests.isEmpty()) {
            content.addButton(
                Material.BARRIER,
                ChatF.empty().append("You haven't completed any quests", ChatF.C_GRAY),
                4, 2,
                (event, container) -> {}
            );
        } else {
            for (int i = startIndex; i < endIndex; i++) {
                Map.Entry<String, QuestProgress> entry = completedQuests.get(i);
                String questId = entry.getKey().replace("completed:", "");
                QuestProgress progress = entry.getValue();
                int slot = i - startIndex;
                
                Quest quest = questListener.getQuest(questId);
                if (quest == null) continue;
                
                // Create completed quest button with completion time
                long completionTimestamp = progress.getCompletionTimestamp();
                List<Object> lore = new ArrayList<>();
                lore.add(ChatF.of(quest.getDescription(), ChatF.C_GRAY).toString());
                lore.add("");
                lore.add(ChatF.of("Completed: " + formatTime(completionTimestamp), ChatF.C_GREEN).toString());
                if (quest.isRepeatable()) {
                    boolean onCooldown = questListener.isQuestOnCooldown(player, questId);
                    if (onCooldown) {
                        long cooldownRemaining = questListener.getQuestCooldownRemaining(player, questId);
                        lore.add(ChatF.of("Available again in: " + formatTimeRemaining(cooldownRemaining), ChatF.C_RED).toString());
                    } else {
                        lore.add(ChatF.of("Available to take again", ChatF.C_GREEN).toString());
                    }
                } else {
                    lore.add(ChatF.of("One-time quest completed", ChatF.C_GRAY).toString());
                }
                
                NotGUIItem item = content.createItem(MATERIAL_COMPLETED_QUEST, slot % 9, slot / 9)
                    .name(ChatF.empty().append(quest.getTitle(), ChatF.C_YELLOW))
                    .lore(lore);
                
                content.registerClickHandler(item.id(), (event, container) -> {
                    openQuestDetails(player, questId);
                });
            }
        }
        
        // Footer with navigation
        NotGUIContainer footer = gui.createContainer(0, 5, 9, 1);
        fillBackground(footer, MATERIAL_BACKGROUND);
        
        // Back button
        footer.addButton(
            MATERIAL_BACK,
            ChatF.empty().append("Back to Main Menu", ChatF.C_RED),
            0, 0,
            (event, container) -> openMainMenu(player)
        );
        
        // Pagination
        int totalPages = (int) Math.ceil((double) completedQuests.size() / QUESTS_PER_PAGE);
        
        if (page > 0) {
            footer.addButton(
                MATERIAL_PREV_PAGE,
                ChatF.empty().append("Previous Page", ChatF.C_YELLOW),
                3, 0,
                (event, container) -> openCompletedQuestsPage(player, page - 1)
            );
        }
        
        if (page < totalPages - 1) {
            footer.addButton(
                MATERIAL_NEXT_PAGE,
                ChatF.empty().append("Next Page", ChatF.C_YELLOW),
                5, 0,
                (event, container) -> openCompletedQuestsPage(player, page + 1)
            );
        }
        
        // Page indicator
        footer.addButton(
            Material.PAPER,
            ChatF.empty().append("Page " + (page + 1) + "/" + Math.max(1, totalPages), ChatF.C_WHITE),
            4, 0,
            (event, container) -> {}
        );
        
        gui.open(player);
    }
    
    /**
     * Shows detailed information about a specific quest
     */
    private void openQuestDetails(Player player, String questId) {
        Quest quest = questListener.getQuest(questId);
        if (quest == null) {
            // Quest not found, go back to main menu
            openMainMenu(player);
            return;
        }
        
        QuestStatus status = questListener.getQuestStatus(player, questId);
        QuestProgress progress = questListener.getQuestProgress(player, questId);
        
        NotGUI gui = NotGUI.create()
            .title(ChatF.of("Quest: " + quest.getTitle(), ChatF.C_GOLD))
            .rows(6);
        
        // Header with quest title and status
        NotGUIContainer header = gui.createContainer(0, 0, 9, 1);
        
        Material questMaterial;
        ChatF statusText;
        
        switch (status) {
            case ACTIVE:
                questMaterial = MATERIAL_ACTIVE_QUEST;
                statusText = ChatF.empty().append("Status: ", ChatF.C_WHITE).append("In Progress", ChatF.C_GREEN);
                break;
            case AVAILABLE:
                questMaterial = MATERIAL_AVAILABLE_QUEST;
                statusText = ChatF.empty().append("Status: ", ChatF.C_WHITE).append("Available", ChatF.C_AQUA);
                break;
            case COMPLETED:
                questMaterial = MATERIAL_COMPLETED_QUEST;
                statusText = ChatF.empty().append("Status: ", ChatF.C_WHITE).append("Completed", ChatF.C_LIGHTPURPLE);
                break;
            case COOLDOWN:
                questMaterial = MATERIAL_COMPLETED_QUEST;
                long cooldownRemaining = questListener.getQuestCooldownRemaining(player, questId);
                statusText = ChatF.empty()
                    .append("Status: ", ChatF.C_WHITE)
                    .append("On Cooldown (", ChatF.C_RED)
                    .append(formatTimeRemaining(cooldownRemaining), ChatF.C_YELLOW)
                    .append(")", ChatF.C_RED);
                break;
            default:
                questMaterial = MATERIAL_BACKGROUND;
                statusText = ChatF.empty().append("Status: ", ChatF.C_WHITE).append("Unknown", ChatF.C_GRAY);
                break;
        }
        
        header.addButton(questMaterial, 
            ChatF.empty().appendBold(quest.getTitle(), ChatF.C_YELLOW), 
            4, 0,
            (event, container) -> {}
        );
        
        // Quest description section
        NotGUIContainer descriptionContainer = gui.createContainer(0, 1, 9, 1);
        
        List<Object> descLore = new ArrayList<>();
        descLore.add(ChatF.of(quest.getDescription()));
        descLore.add(ChatF.newline());
        descLore.add(statusText);
        
        if (quest.isRepeatable()) {
            descLore.add(ChatF.of("This quest is repeatable (Cooldown: " + quest.getCooldownHours() + " hours)", ChatF.C_YELLOW));
        } else {
            descLore.add(ChatF.of("This is a one-time quest", ChatF.C_GRAY));
        }
        
        descriptionContainer.createItem(Material.PAPER, 4, 0)
            .name(ChatF.empty().append("Description", ChatF.C_WHITE))
            .lore(descLore);
        
        // Objectives section
        NotGUIContainer objectivesContainer = gui.createContainer(0, 2, 9, 2);
        List<QuestObjective> objectives = quest.getObjectives();
        
        for (int i = 0; i < objectives.size(); i++) {
            if (i >= 9) break; // Limit to 9 objectives displayed
            
            QuestObjective objective = objectives.get(i);
            boolean completed = status == QuestStatus.COMPLETED || 
                (status == QuestStatus.ACTIVE && progress != null && 
                progress.getObjectiveProgress(objective.getId()) >= objective.getAmount());
            
            Material objMaterial = completed ? MATERIAL_OBJECTIVE_COMPLETE : MATERIAL_OBJECTIVE_INCOMPLETE;
            ChatF objName = ChatF.empty().append(objective.getDescription(), completed ? ChatF.C_GREEN : ChatF.C_GRAY);
            
            List<Object> objLore = new ArrayList<>();
            
            if (status == QuestStatus.ACTIVE && progress != null) {
                int currentProgress = progress.getObjectiveProgress(objective.getId());
                objLore.add(ChatF.of("Progress: " + currentProgress + "/" + objective.getAmount(), 
                    completed ? ChatF.C_GREEN : ChatF.C_YELLOW));
            } else if (status == QuestStatus.COMPLETED) {
                objLore.add(ChatF.of("Completed", ChatF.C_GREEN));
            } else {
                objLore.add(ChatF.of("Required: " + objective.getAmount(), ChatF.C_GRAY));
            }
            
            objectivesContainer.createItem(objMaterial, i, 0)
                .name(objName)
                .lore(objLore);
        }
        
        // Rewards section
        NotGUIContainer rewardsContainer = gui.createContainer(0, 4, 9, 1);
        Map<String, Object> rewards = quest.getRewards();
        
        int rewardSlot = 0;
        
        // Show rewards
        if (rewards.isEmpty()) {
            rewardsContainer.createItem(Material.BARRIER, 4, 0)
                .name(ChatF.empty().append("No Rewards", ChatF.C_GRAY));
        } else {
            for (Map.Entry<String, Object> entry : rewards.entrySet()) {
                if (rewardSlot >= 9) break; // Limit to 9 rewards displayed
                
                String rewardType = entry.getKey();
                Object rewardValue = entry.getValue();
                
                Material rewardMaterial = getRewardMaterial(rewardType);
                ChatF rewardName = getRewardName(rewardType, rewardValue);
                List<Object> rewardLore = getRewardLore(rewardType, rewardValue);
                
                rewardsContainer.createItem(rewardMaterial, rewardSlot, 0)
                    .name(rewardName)
                    .lore(rewardLore);
                
                rewardSlot++;
            }
        }
        
        // Action buttons (take/cancel/back)
        NotGUIContainer actionsContainer = gui.createContainer(0, 5, 9, 1);
        fillBackground(actionsContainer, MATERIAL_BACKGROUND);
        
        // Back button
        actionsContainer.addButton(
            MATERIAL_BACK,
            ChatF.empty().append("Back", ChatF.C_RED),
            0, 0,
            (event, container) -> {
                // Go back to previous screen based on quest status
                switch (status) {
                    case ACTIVE:
                        openActiveQuestsPage(player, 0);
                        break;
                    case COMPLETED:
                    case COOLDOWN:
                        openCompletedQuestsPage(player, 0);
                        break;
                    case AVAILABLE:
                        openAvailableQuestsPage(player, 0);
                        break;
                    default:
                        openMainMenu(player);
                        break;
                }
            }
        );
        
        // Action buttons based on quest status
        if (status == QuestStatus.AVAILABLE) {
            // Take quest button
            actionsContainer.addButton(
                MATERIAL_TAKE_QUEST,
                ChatF.empty().appendBold("Accept Quest", ChatF.C_GREEN),
                4, 0,
                (event, container) -> {
                    boolean success = questListener.assignQuestToPlayer(player, questId);
                    if (success) {
                        player.closeInventory();
                        // Re-open the quest details to show it as active
                        openQuestDetails(player, questId);
                    }
                }
            );
        } else if (status == QuestStatus.ACTIVE) {
            // Cancel quest button
            actionsContainer.addButton(
                MATERIAL_CANCEL_QUEST,
                ChatF.empty().appendBold("Abandon Quest", ChatF.C_RED),
                4, 0,
                (event, container) -> {
                    boolean success = questListener.abandonQuest(player, questId);
                    if (success) {
                        player.closeInventory();
                        openMainMenu(player);
                    }
                }
            );
            
            // Track quest button (sends a reminder of objectives)
            actionsContainer.addButton(
                Material.COMPASS,
                ChatF.empty().append("Track Quest", ChatF.C_AQUA),
                6, 0,
                (event, container) -> {
                    questListener.trackQuest(player, questId);
                    player.closeInventory();
                }
            );
        } else if (status == QuestStatus.COOLDOWN) {
            // Show cooldown info
            long cooldownRemaining = questListener.getQuestCooldownRemaining(player, questId);
            
            actionsContainer.addButton(
                Material.CLOCK,
                ChatF.empty()
                    .append("Available again in: ", ChatF.C_RED)
                    .append(formatTimeRemaining(cooldownRemaining), ChatF.C_YELLOW),
                4, 0,
                (event, container) -> {}
            );
        }
        
        // Open main menu button
        actionsContainer.addButton(
            Material.OAK_DOOR,
            ChatF.empty().append("Main Menu", ChatF.C_WHITE),
            8, 0,
            (event, container) -> openMainMenu(player)
        );
        
        gui.open(player);
    }
    
    // Helper methods
    
    private void addQuestButton(NotGUIContainer container, Quest quest, int slot, QuestStatus status, Player player, 
                                BiConsumer<InventoryClickEvent, NotGUIContainer> clickAction) {
        
        Material material;
        TextColor nameColor;
        
        switch (status) {
            case ACTIVE:
                material = MATERIAL_ACTIVE_QUEST;
                nameColor = ChatF.C_GREEN;
                break;
            case AVAILABLE:
                material = MATERIAL_AVAILABLE_QUEST;
                nameColor = ChatF.C_AQUA;
                break;
            case COMPLETED:
            case COOLDOWN:
                material = MATERIAL_COMPLETED_QUEST;
                nameColor = ChatF.C_LIGHTPURPLE;
                break;
            default:
                material = Material.PAPER;
                nameColor = ChatF.C_WHITE;
                break;
        }
        
        List<Object> lore = new ArrayList<>();
        lore.add(ChatF.of(quest.getDescription(), ChatF.C_GRAY));
        lore.add(ChatF.newline());
        
        // Add status-specific info to lore
        switch (status) {
            case ACTIVE:
                QuestProgress progress = questListener.getQuestProgress(player, quest.getId());
                int completedObjectives = 0;
                int totalObjectives = quest.getObjectives().size();
                
                if (progress != null) {
                    for (QuestObjective objective : quest.getObjectives()) {
                        if (progress.getObjectiveProgress(objective.getId()) >= objective.getAmount()) {
                            completedObjectives++;
                        }
                    }
                }
                
                lore.add(ChatF.of("Status: ", ChatF.C_WHITE).append("In Progress", ChatF.C_GREEN));
                lore.add(ChatF.of("Progress: ", ChatF.C_WHITE)
                    .append(completedObjectives + "/" + totalObjectives, ChatF.C_YELLOW)
                    );
                break;
                
            case AVAILABLE:
                lore.add(ChatF.of("Status: ", ChatF.C_WHITE).append("Available", ChatF.C_AQUA));
                if (quest.isRepeatable()) {
                    lore.add(ChatF.of("Repeatable: Yes (Cooldown: " + quest.getCooldownHours() + "h)", ChatF.C_YELLOW));
                } else {
                    lore.add(ChatF.of("Repeatable: No", ChatF.C_GRAY));
                }
                break;
                
            case COMPLETED:
                lore.add(ChatF.of("Status: ", ChatF.C_WHITE).append("Completed", ChatF.C_LIGHTPURPLE));
                break;
                
            case COOLDOWN:
                long cooldownRemaining = questListener.getQuestCooldownRemaining(player, quest.getId());
                lore.add(ChatF.of("Status: ", ChatF.C_WHITE).append("On Cooldown", ChatF.C_RED));
                lore.add(ChatF.of("Available again in: ", ChatF.C_WHITE)
                    .append(formatTimeRemaining(cooldownRemaining), ChatF.C_YELLOW)
                    );
                break;
        }
        
        lore.add(ChatF.newline());
        lore.add(ChatF.of("Click to view details", ChatF.C_DARKGRAY));
        
        NotGUIItem item = container.createItem(material, slot % 9, slot / 9)
            .name(ChatF.empty().append(quest.getTitle(), nameColor))
            .lore(lore);
        
        container.registerClickHandler(item.id(), clickAction);
    }
    
    private void fillBackground(NotGUIContainer container, Material material) {
        for (int slot = 0; slot < container.totalSize(); slot++) {
            container.addButton(
                material,
                ChatF.empty(),
                slot,
                (event, cont) -> {}
            );
        }
    }
    
    private Material getCategoryIcon(String category) {
        // Return different icons based on category
        switch (category.toLowerCase()) {
            case "daily":
                return Material.CLOCK;
            case "mining":
                return Material.IRON_PICKAXE;
            case "combat":
                return Material.IRON_SWORD;
            case "farming":
                return Material.WHEAT;
            case "exploration":
                return Material.COMPASS;
            case "fishing":
                return Material.FISHING_ROD;
            case "special":
                return Material.NETHER_STAR;
            default:
                return MATERIAL_CATEGORY_ICON;
        }
    }
    
    private String formatCategoryName(String category) {
        // Convert category name to title case with spaces
        if (category == null || category.isEmpty()) {
            return "Unknown";
        }
        
        String[] words = category.split("_");
        StringBuilder sb = new StringBuilder();
        
        for (String word : words) {
            if (word.isEmpty()) continue;
            sb.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                sb.append(word.substring(1).toLowerCase());
            }
            sb.append(" ");
        }
        
        return sb.toString().trim();
    }
    
    private Material getRewardMaterial(String rewardType) {
        switch (rewardType.toLowerCase()) {
            case "money":
                return Material.GOLD_INGOT;
            case "xp":
                return Material.EXPERIENCE_BOTTLE;
            case "item":
                return Material.CHEST;
            case "command":
                return Material.COMMAND_BLOCK;
            default:
                return Material.PAPER;
        }
    }
    
    private ChatF getRewardName(String rewardType, Object rewardValue) {
        switch (rewardType.toLowerCase()) {
            case "money":
                return ChatF.empty().append("Money Reward: $" + rewardValue, ChatF.C_YELLOW);
            case "xp":
                return ChatF.empty().append("XP Reward: " + rewardValue + " XP", ChatF.C_GREEN);
            case "item":
                if (rewardValue instanceof String) {
                    return ChatF.empty().append("Item Reward: " + rewardValue, ChatF.C_AQUA);
                } else if (rewardValue instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> items = (List<String>) rewardValue;
                    return ChatF.empty().append("Item Rewards (" + items.size() + ")", ChatF.C_AQUA);
                } else {
                    return ChatF.empty().append("Item Reward", ChatF.C_AQUA);
                }
            case "command":
                return ChatF.empty().append("Special Reward", ChatF.C_LIGHTPURPLE);
            default:
                return ChatF.empty().append(rewardType + " Reward", ChatF.C_WHITE);
        }
    }
    
    private List<Object> getRewardLore(String rewardType, Object rewardValue) {
        List<Object> lore = new ArrayList<>();
        
        switch (rewardType.toLowerCase()) {
            case "money":
                lore.add(ChatF.of("Receive $" + rewardValue + " when completed", ChatF.C_GRAY));
                break;
            case "xp":
                lore.add(ChatF.of("Gain " + rewardValue + " experience points", ChatF.C_GRAY));
                break;
            case "item":
                if (rewardValue instanceof String) {
                    lore.add(ChatF.of("Receive: " + rewardValue, ChatF.C_GRAY));
                } else if (rewardValue instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> items = (List<String>) rewardValue;
                    lore.add(ChatF.of("Receive:", ChatF.C_GRAY));
                    for (String item : items) {
                        lore.add(ChatF.of("- " + item, ChatF.C_GRAY));
                    }
                }
                break;
            case "command":
                lore.add(ChatF.of("A special reward awaits you", ChatF.C_GRAY));
                break;
            default:
                lore.add(ChatF.of(rewardType + ": " + rewardValue, ChatF.C_GRAY));
                break;
        }
        
        return lore;
    }
    
    private String formatTime(long timestamp) {
        java.util.Date date = new java.util.Date(timestamp);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm");
        return sdf.format(date);
    }
    
    private String formatTimeRemaining(long millisRemaining) {
        long hours = millisRemaining / (60 * 60 * 1000);
        long minutes = (millisRemaining % (60 * 60 * 1000)) / (60 * 1000);
        
        return hours + "h " + minutes + "m";
    }
    
    // Quest status enum
    public enum QuestStatus {
        AVAILABLE,
        ACTIVE,
        COMPLETED,
        COOLDOWN
    }
}