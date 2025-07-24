package com.stardevllc.starquests;

import com.stardevllc.starcore.api.StarEvents;
import com.stardevllc.starlib.eventbus.SubscribeEvent;
import com.stardevllc.starmclib.StarMCLib;
import com.stardevllc.starmclib.plugin.ExtendedJavaPlugin;
import com.stardevllc.starquests.actions.QuestAction;
import com.stardevllc.starquests.actions.QuestActionData;
import com.stardevllc.starquests.actions.function.QuestActionPredicate.Status;
import com.stardevllc.starquests.cmds.QuestCmd;
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
        
        registerCommand("quest", new QuestCmd());
        
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                handleQuestActionTrigger(player, player);
            }
        }, 1L, 1L);
        
        this.addQuest(Quest.builder()
                .name("Obtain Workbench")
                .onComplete((quest, player) -> player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 2)))
                .addAction(
                        QuestAction.builder(BlockBreakEvent.class)
                                .name("Break 4 Logs")
                                .predicate((a, e, playerData) -> {
                                    if (!e.getBlock().getType().name().contains("_LOG")) {
                                        return Status.FALSE;
                                    }
                                    
                                    Integer count = playerData.modifyData("count", c -> c + 1, 0);
                                    
                                    if (count < 4) {
                                        return Status.IN_PROGRESS;
                                    }
                                    
                                    return Status.COMPLETE;
                                })
                                .onUpdate((a, e, playerData) -> getColors().coloredLegacy(e.getPlayer(), "&eLogs Broken: &a" + playerData.getAsInt("count") + " &e/ &d4")),
                        QuestAction.builder(CraftItemEvent.class)
                                .name("Craft 4 Planks")
                                .predicate((a, e, playerData) -> {
                                    ItemStack result = e.getInventory().getResult();
                                    if (!result.getType().name().contains("_PLANK")) {
                                        return Status.FALSE;
                                    }
                                    
                                    Integer count = playerData.modifyData("count", c -> c + result.getAmount(), 0);
                                    
                                    if (count < 4) {
                                        return Status.IN_PROGRESS;
                                    }
                                    
                                    return Status.COMPLETE;
                                })
                                .prerequisiteActions("break_4_logs")
                                .onUpdate((a, e, playerData) -> getColors().coloredLegacy(e.getWhoClicked(), "&ePlanks Crafted: &a" + playerData.getAsInt("count") + " &e/ &d4")),
                        QuestAction.builder(CraftItemEvent.class)
                                .name("Craft Workbench")
                                .predicate((a, e, playerData) -> {
                                    if (e.getInventory().getResult().getType() == Material.CRAFTING_TABLE) {
                                        return Status.COMPLETE;
                                    } else {
                                        return Status.FALSE;
                                    }
                                })
                                .prerequisiteActions("craft_4_planks")
                )
                .build());
        
        this.addQuest(Quest.builder()
                .name("Obtain Wooden Pickaxe")
                .prerequisiteQuests("obtain_workbench")
                .onComplete((quest, player) -> player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 10)))
                .addAction(QuestAction.builder(CraftItemEvent.class)
                                .name("Craft 2 Sticks")
                                .predicate((a, e, playerData) -> {
                                    ItemStack result = e.getInventory().getResult();
                                    if (result.getType() != Material.STICK) {
                                        return Status.FALSE;
                                    }
                                    
                                    Integer count = playerData.modifyData("count", c -> c + result.getAmount(), 0);
                                    
                                    if (count < 2) {
                                        return Status.IN_PROGRESS;
                                    }
                                    
                                    return Status.COMPLETE;
                                })
                                .onUpdate((a, e, playerData) -> getColors().coloredLegacy(e.getWhoClicked(), "&eSticks Crafted: &a" + playerData.getAsInt("count") + " &e/ &d2")),
                        QuestAction.builder(CraftItemEvent.class)
                                .name("Craft a Wooden Pickaxe")
                                .predicate((a, e, playerData) -> {
                                    if (e.getInventory().getResult().getType() == Material.WOODEN_PICKAXE) {
                                        return Status.COMPLETE;
                                    } else {
                                        return Status.FALSE;
                                    }
                                })
                                .prerequisiteActions("craft_2_sticks")
                )
                .build());
        
        for (Quest quest : this.quests.values()) {
            getLogger().info("Quest: " + quest.getId());
            for (QuestAction<?> action : quest.getActions().values()) {
                getLogger().info("  Action: " + action.getId());
            }
        }
    }
    
    public void addQuest(Quest quest) {
        this.quests.put(quest.getId(), getInjector().inject(quest));
    }
    
    public Map<String, Quest> getQuests() {
        return new HashMap<>(quests);
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
    
    public boolean isQuestAvailble(UUID uuid, Quest quest) {
        if (isQuestComplete(uuid, quest)) {
            return false;
        }
        
        for (String pq : quest.getPrerequisiteQuests()) {
            Quest prerequisiteQuest = getQuest(pq);
            if (!isQuestComplete(uuid, prerequisiteQuest)) {
                return false;
            }
        }
        
        return true;
    }
    
    public boolean isActionAvailable(UUID uuid, QuestAction<?> action) {
        if (isActionComplete(uuid, action)) {
            return false;
        }
        
        if (action.getQuest() != null) {
            if (!isQuestAvailble(uuid, action.getQuest())) {
                return false;
            }
        }
        
        for (String pa : action.getPrerequisiteActions()) {
            QuestAction<?> prerequisiteAction;
            if (action.getQuest() != null) {
                prerequisiteAction = action.getQuest().getActions().get(pa);
            } else {
                prerequisiteAction = getQuestAction(pa);
            }
            if (prerequisiteAction != null) {
                if (!isActionComplete(uuid, prerequisiteAction)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    public void handleQuestAction(QuestAction<?> action, Object questActionObject, Player player) {
        try {
            //Ignore the action if it is not available, which does prereq and quest chests
            if (!isActionAvailable(player.getUniqueId(), action)) {
                return;
            }

            //Get or create action data
            QuestActionData actionData = getActionData(player.getUniqueId(), action);
            
            //Test the action for completion or update the quest
            Status check = action.check(questActionObject, actionData);
            if (check == Status.COMPLETE) {
                //If complete, mark it as complete
                Bukkit.getPluginManager().callEvent(new ActionCompleteEvent(action, actionData));
                action.handleOnComplete(questActionObject, actionData);
                completeAction(player.getUniqueId(), action);
                //Mainly a testing message for now
                getColors().coloredLegacy(player, "&aCompleted Quest Action: &b" + action.getName());
            }
            
            questsLoop:
            for (Quest quest : this.quests.values()) {
                boolean questComplete = isQuestComplete(player.getUniqueId(), quest);
                if (questComplete) {
                    continue;
                }
                
                for (QuestAction<?> questAction : quest.getActions().values()) {
                    if (!isActionComplete(player.getUniqueId(), questAction)) {
                        continue questsLoop;
                    }
                }
                
                completeQuest(player.getUniqueId(), quest);
                if (quest.getOnComplete() != null) {
                    quest.getOnComplete().apply(quest, player);
                }
                getColors().coloredLegacy(player, "&aCompleted Quest: &b" + action.getName());
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