package com.stardevllc.starquests;

import com.stardevllc.eventbus.SubscribeEvent;
import com.stardevllc.starcore.api.StarEvents;
import com.stardevllc.starmclib.plugin.ExtendedJavaPlugin;
import com.stardevllc.starquests.actions.QuestAction;
import com.stardevllc.starquests.actions.QuestActionData;
import com.stardevllc.starquests.events.ActionCompleteEvent;
import com.stardevllc.starquests.events.QuestEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
    
    @Override
    public void onEnable() {
        super.onEnable();
        StarEvents.subscribe(getEventBus());
        getEventBus().subscribe(this);
        
        getServer().getPluginManager().registerEvents(this, this);
        
        this.actions.put("break_4_logs", getInjector().inject(new QuestAction<>("break_4_logs", "Break 4 Logs", List.of(), BlockBreakEvent.class, (a, e, playerData) -> {
            if (!e.getBlock().getType().name().contains("_LOG")) {
                return false;
            }
            
            return playerData.modifyData("count", count -> count + 1, 0) >= 4;
        }, List.of(), (a, e, playerData) -> getColors().coloredLegacy(e.getPlayer(), "&eLogs Broken: &a" + playerData.getAsInt("count") + " &e/ &d4"), null)));
        
        this.actions.put("craft_4_planks", getInjector().inject(new QuestAction<>("craft_4_planks", "Craft 4 Planks", List.of(), CraftItemEvent.class, (a, e, playerData) -> {
            ItemStack result = e.getInventory().getResult();
            if (!result.getType().name().contains("_PLANK")) {
                return false;
            }
            
            return playerData.modifyData("count", count -> count + result.getAmount(), 0) >= 4;
        }, List.of("break_4_logs"), (a, e, playerData) -> getColors().coloredLegacy(e.getWhoClicked(), "&ePlanks Crafted: &a" + playerData.getAsInt("count") + " &e/ &d4"), null)));
        
        this.actions.put("craft_workbench", getInjector().inject(new QuestAction<>("craft_workbench", "Craft Workbench", List.of(), CraftItemEvent.class, (a, e, playerData) -> e.getInventory().getResult().getType() == Material.CRAFTING_TABLE, List.of("craft_4_planks"), null, null)));
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
        
        Set<String> playerCompletions = this.completedActions.computeIfAbsent(uuid, u -> new HashSet<>());
        return playerCompletions.contains(action.getId());
    }
    
    public void completeAction(UUID uuid, QuestAction<?> action) {
        Set<String> playerCompletions = this.completedActions.computeIfAbsent(uuid, u -> new HashSet<>());
        playerCompletions.add(action.getId());
    }
    
    @SubscribeEvent
    public void onEvent(Event e) {
        //Ignore custom events for quests as these are mainly for formatting and stuff
        if (e instanceof QuestEvent) {
            System.out.println("Ignoring QuestEvent");
            return;
        }
        
        //Loop through all actions to check them
        for (QuestAction<?> action : this.actions.values()) {
            try {
                //Get the player from the event. Some events may need to be specified, bu the method supports that thing
                QuestUtils.getPlayerFromEvent(e).ifPresent(player -> {
                    //Ignore completed actions
                    if (isActionComplete(player.getUniqueId(), action)) {
                        return;
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
                    if (action.check(e, actionData)) {
                        //If complete, mark it as complete
                        Bukkit.getPluginManager().callEvent(new ActionCompleteEvent(action, actionData));
                        action.handleOnComplete(e, actionData);
                        completeAction(player.getUniqueId(), action);
                        //Mainly a testing message for now
                        getColors().coloredLegacy(player, "&aCompleted Quest Action: &b" + action.getName());
                    }
                });
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }
}