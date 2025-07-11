package com.stardevllc.starquests;

import com.stardevllc.eventbus.SubscribeEvent;
import com.stardevllc.starcore.api.StarEvents;
import com.stardevllc.starmclib.plugin.ExtendedJavaPlugin;
import com.stardevllc.starquests.actions.QuestAction;
import com.stardevllc.starquests.actions.QuestActionData;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.*;

public class StarQuests extends ExtendedJavaPlugin {
    private Set<QuestAction<?>> actions = new HashSet<>();
    private Map<UUID, Map<String, QuestActionData>> actionData = new HashMap<>();
    private Map<UUID, Set<String>> completedActions = new HashMap<>();
    
    @Override
    public void onEnable() {
        super.onEnable();
        StarEvents.subscribe(getEventBus());
        getEventBus().subscribe(this);
        
        this.actions.add(getInjector().inject(new QuestAction<>("test1", "Test 1", List.of(), BlockBreakEvent.class, (a, e) -> {
            QuestActionData playerData = getActionData(e.getPlayer().getUniqueId(), a);
            
            if (isActionComplete(e.getPlayer().getUniqueId(), a)) {
                return false;
            }
            
            playerData.modifyData("count", count -> count + 1, 0);
            
            Object rawCount = playerData.get("count");
            if (rawCount instanceof Integer count) {
                getColors().coloredLegacy(e.getPlayer(), "&e" + a.name + ": &b" + count + " &e/ 5");
                return count >= 5;
            }
            
            return false;
        })));
    }
    
    public QuestActionData getActionData(UUID uuid, QuestAction<?> action) {
        Map<String, QuestActionData> data = actionData.getOrDefault(uuid, new HashMap<>());
        actionData.put(uuid, data);
        QuestActionData playerData = data.getOrDefault(action.id, new QuestActionData(action.id));
        data.put(action.id, playerData);
        return playerData;
    }
    
    public boolean isActionComplete(UUID uuid, QuestAction<?> action) {
        Set<String> playerCompletions = this.completedActions.getOrDefault(uuid, new HashSet<>());
        this.completedActions.put(uuid, playerCompletions);
        return playerCompletions.contains(action.id);
    }
    
    public void completeAction(UUID uuid, QuestAction<?> action) {
        Set<String> playerCompletions = this.completedActions.getOrDefault(uuid, new HashSet<>());
        this.completedActions.put(uuid, playerCompletions);
        playerCompletions.add(action.id);
    }
    
    @SubscribeEvent
    public void onEvent(Event e) {
        for (QuestAction<?> action : this.actions) {
            try {
                boolean test = action.check(e);
                if (test) {
                    QuestUtils.getPlayerFromEvent(e).ifPresent(player -> {
                        completeAction(player.getUniqueId(), action);
                        getColors().coloredLegacy(player, "&aCompleted Quest: &b" + action.name);
                    });
                }
            } catch (Throwable ex) {}
        }
    }
}