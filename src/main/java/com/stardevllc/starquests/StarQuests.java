package com.stardevllc.starquests;

import com.stardevllc.eventbus.SubscribeEvent;
import com.stardevllc.starcore.api.StarEvents;
import com.stardevllc.starmclib.plugin.ExtendedJavaPlugin;
import com.stardevllc.starquests.actions.QuestAction;
import com.stardevllc.starquests.actions.QuestActionData;
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
            
            playerData.modifyData("count", count -> count + 1, 0);
            
            Object rawCount = playerData.get("count");
            if (rawCount instanceof Integer count) {
                getColors().coloredLegacy(e.getPlayer(), "&eQuestAction: &b" + a.getName() + "&e: &a" + count + " &e/ &d4");
                return count >= 4;
            }
            
            return false;
        }, List.of())));
        
        this.actions.put("craft_4_planks", getInjector().inject(new QuestAction<>("craft_4_planks", "Craft 4 Planks", List.of(), CraftItemEvent.class, (a, e, playerData) -> {
            ItemStack result = e.getInventory().getResult();
            if (!result.getType().name().contains("_PLANK")) {
                return false;
            }
            
            playerData.modifyData("count", count -> count + result.getAmount(), 0);
            
            Object rawCount = playerData.get("count");
            if (rawCount instanceof Integer count) {
                getColors().coloredLegacy(e.getWhoClicked(), "&eQuestAction: &b" + a.getName() + "&e: &a" + count + " &e/ &d4");
                return count >= 4;
            }
            
            return false;
        }, List.of("break_4_logs"))));
        
        this.actions.put("craft_workbench", getInjector().inject(new QuestAction<>("craft_workbench", "Craft Workbench", List.of(), CraftItemEvent.class, (a, e, playerData) -> e.getInventory().getResult().getType() == Material.CRAFTING_TABLE, List.of("craft_4_planks"))));
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
        for (QuestAction<?> action : this.actions.values()) {
            try {
                QuestUtils.getPlayerFromEvent(e).ifPresent(player -> {
                    if (isActionComplete(player.getUniqueId(), action)) {
                        return;
                    }
                    
                    for (String pa : action.getPrerequisiteActions()) {
                        if (!isActionComplete(player.getUniqueId(), this.actions.get(pa))) {
                            return;
                        }
                    }
                    
                    QuestActionData actionData = getActionData(player.getUniqueId(), action);
                    
                    if (action.check(e, actionData)) {
                        completeAction(player.getUniqueId(), action);
                        getColors().coloredLegacy(player, "&aCompleted Quest Action: &b" + action.getName());
                    }
                });
            } catch (Throwable ex) {}
        }
    }
}