package com.stardevllc.starquests;

import com.stardevllc.starcore.api.StarEvents;
import com.stardevllc.starlib.eventbus.SubscribeEvent;
import com.stardevllc.starmclib.StarMCLib;
import com.stardevllc.starmclib.plugin.ExtendedJavaPlugin;
import com.stardevllc.starquests.actions.QuestAction;
import com.stardevllc.starquests.actions.QuestActionData;
import com.stardevllc.starquests.events.ActionCompleteEvent;
import com.stardevllc.starquests.events.QuestEvent;
import com.stardevllc.starquests.quests.Quest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class StarQuests extends ExtendedJavaPlugin implements Listener {
    private Map<String, QuestAction<?>> actions = new HashMap<>();
    private Map<UUID, Map<String, QuestActionData>> actionData = new HashMap<>();
    private Map<UUID, Set<String>> completedActions = new HashMap<>();
    private Map<String, Quest> quests = new HashMap<>();
    private Map<UUID, Set<String>> completedQuests = new HashMap<>();
    
    @Override
    public void onEnable() {
        super.onEnable();
        StarMCLib.registerPluginEventBus(getEventBus());
        StarMCLib.registerPluginInjector(this, getInjector());
        StarEvents.addChildBus(getEventBus());
        getEventBus().subscribe(this);
        
        registerListeners(this);
        
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                handleQuestActionTrigger(player, player);
            }
        }, 1L, 1L);
        
        this.quests.put("obtain_workbench", getInjector().inject(new Quest("obtain_workbench", "Obtain Workbench", List.of(), List.of(), (quest, player) -> player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 2)))
                .addAction(new QuestAction<>("break_1_log", "Break 1 Log", List.of(), BlockBreakEvent.class, (a, e, playerData) -> {
                    if (!e.getBlock().getType().name().contains("_LOG")) {
                        return false;
                    }
                    
                    return playerData.modifyData("count", count -> count + 1, 0) >= 4;
                }, List.of(), (a, e, playerData) -> getColors().coloredLegacy(e.getPlayer(), "&eLogs Broken: &a" + playerData.getAsInt("count") + " &e/ &d4"), null))
                .addAction(new QuestAction<>("craft_4_planks", "Craft 4 Planks", List.of(), CraftItemEvent.class, (a, e, playerData) -> {
                    ItemStack result = e.getInventory().getResult();
                    if (!result.getType().name().contains("_PLANK")) {
                        return false;
                    }
                    
                    return playerData.modifyData("count", count -> count + result.getAmount(), 0) >= 4;
                }, List.of("break_1_logs"), (a, e, playerData) -> getColors().coloredLegacy(e.getWhoClicked(), "&ePlanks Crafted: &a" + playerData.getAsInt("count") + " &e/ &d4"), null))
                .addAction(new QuestAction<>("craft_workbench", "Craft Workbench", List.of(), CraftItemEvent.class, (a, e, playerData) -> e.getInventory().getResult().getType() == Material.CRAFTING_TABLE, List.of("craft_4_planks"), null, null))
        ));
        
        this.quests.put("obtain_wooden_pickaxe", getInjector().inject(new Quest("obtain_wodden_pickaxe", "Obtain Wooden Pickaxe", List.of(), List.of("obtain_workbench"), (quest, player) -> player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 10)))
                .addAction(new QuestAction<>("craft_two_sticks", "Craft 2 Sticks", List.of(), CraftItemEvent.class, (a, e, playerData) -> {
                    ItemStack result = e.getInventory().getResult();
                    if (result.getType() != Material.STICK) {
                        return false;
                    }
                    
                    return playerData.modifyData("count", count -> count + result.getAmount(), 0) >= 2;
                }, List.of(), (a, e, playerData) -> getColors().coloredLegacy(e.getWhoClicked(), "&eSticks Crafted: &a" + playerData.getAsInt("count") + " &e/ &d2"), null))        
                .addAction(new QuestAction<>("craft_wooden_pickaxe", "Craft a Wooden Pickaxe", List.of(), CraftItemEvent.class, (a, e, playerData) -> e.getInventory().getResult().getType() == Material.WOODEN_PICKAXE, List.of("craft_two_sticks"), null, null))
        ));
    }
    
    public void addAction(QuestAction<?> action) {
        if (action == null) {
            return;
        }
        
        this.actions.put(action.getId(), action);
    }
    
    public boolean isQuestComplete(UUID uuid, Quest quest) {
        if (quest == null) {
            return false;
        }
        
        Set<String> playerCompletions = this.completedQuests.computeIfAbsent(uuid, u -> new HashSet<>());
        boolean contains = playerCompletions.contains(quest.getId());
        
        if (!contains) {
            for (String prerequisiteQuest : quest.getPrerequisiteQuests()) {
                Quest q = getQuest(prerequisiteQuest);
                if (!isQuestComplete(uuid, q)) {
                    return false;
                }
            }
            
            for (QuestAction<?> action : quest.getActions().values()) {
                if (!isActionComplete(uuid, action)) {
                    return false;
                }
            }
        }
        
        return contains;
    }
    
    public Quest getQuest(String id) {
        return this.quests.get(id);
    }
    
    public QuestAction<?> getQuestAction(String id) {
        return this.actions.get(id);
    }
    
    public QuestActionData getActionData(UUID uuid, QuestAction<?> action) {
        Map<String, QuestActionData> data = actionData.computeIfAbsent(uuid, u -> new HashMap<>());
        QuestActionData playerData = data.computeIfAbsent(action.getId(), id -> new QuestActionData(action.getId()));
        data.put(action.getId(), playerData);
        return playerData;
    }
    
    public boolean isActionComplete(UUID uuid, QuestAction<?> action) {
        if (action == null) {
            return false;
        }
        Set<String> actionCompletions = this.completedActions.computeIfAbsent(uuid, u -> new HashSet<>());
        return actionCompletions.contains(action.getId());
    }
    
    public void completeAction(UUID uuid, QuestAction<?> action) {
        Set<String> playerCompletions = this.completedActions.computeIfAbsent(uuid, u -> new HashSet<>());
        playerCompletions.add(action.getId());
    }
    
    public void completeQuest(UUID uuid, Quest quest) {
        Set<String> playerCompletions = this.completedQuests.computeIfAbsent(uuid, u -> new HashSet<>());
        playerCompletions.add(quest.getId());
    }
    
    public void handleQuestAction(QuestAction<?> action, Object questActionObject, Player player) {
        try {
            //Ignore completed actions
            if (isActionComplete(player.getUniqueId(), action)) {
                return;
            }
            
            //Check to see if there is a quest set for the action
            if (action.getQuest() != null) {
                //Ignore the action if the quest is complete
                if (isQuestComplete(player.getUniqueId(), action.getQuest())) {
                    return;
                }
                
                //Check for prerequisite quests
                for (String pq : action.getQuest().getPrerequisiteQuests()) {
                    Quest prerequisiteQuest = getQuest(pq);
                    //If the prerequisite quest is not complete, ignore this action
                    if (!isQuestComplete(player.getUniqueId(), prerequisiteQuest)) {
                        return;
                    }
                }
            }
            
            //Check action prerequisites
            for (String pa : action.getPrerequisiteActions()) {
                if (!isActionComplete(player.getUniqueId(), this.actions.get(pa))) {
                    return;
                }
            }
            
            //Get or create action data
            QuestActionData actionData = getActionData(player.getUniqueId(), action);
            
            //Test the action for completion or update the quest
            if (action.check(questActionObject, actionData)) {
                //If complete, mark it as complete
                Bukkit.getPluginManager().callEvent(new ActionCompleteEvent(action, actionData));
                action.handleOnComplete(questActionObject, actionData);
                completeAction(player.getUniqueId(), action);
                //Mainly a testing message for now
                getColors().coloredLegacy(player, "&aCompleted Quest Action: &b" + action.getName());
                
                for (Quest quest : this.quests.values()) {
                    for (QuestAction<?> questAction : quest.getActions().values()) {
                        if (questAction.getId().equalsIgnoreCase(action.getId())) {
                            boolean questComplete = isQuestComplete(player.getUniqueId(), quest);
                            if (questComplete) {
                                completeQuest(player.getUniqueId(), quest);
                                if (quest.getOnComplete() != null) {
                                    quest.getOnComplete().apply(quest, player);
                                }
                                getColors().coloredLegacy(player, "&aCompleted Quest: &b" + action.getName());
                            }
                        }
                    }
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }
    
    public void handleQuestActionTrigger(Object questActionObject, Player player) {
        //Loop through all actions to check them
        for (QuestAction<?> action : this.actions.values()) {
            handleQuestAction(action, questActionObject, player);
        }
        
        for (Quest quest : this.quests.values()) {
            for (QuestAction<?> action : quest.getActions().values()) {
                handleQuestAction(action, questActionObject, player);
            }
        }
    }
    
    @SubscribeEvent
    public void onEvent(Event e) {
        //Ignore custom events for quests as these are mainly for formatting and stuff
        if (e instanceof QuestEvent) {
            return;
        }
        
        QuestUtils.getPlayerFromEvent(e).ifPresent(player -> handleQuestActionTrigger(e, player));
    }
}