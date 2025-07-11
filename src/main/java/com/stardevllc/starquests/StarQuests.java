package com.stardevllc.starquests;

import com.stardevllc.eventbus.SubscribeEvent;
import com.stardevllc.helper.ReflectionHelper;
import com.stardevllc.starcore.api.StarEvents;
import com.stardevllc.starmclib.plugin.ExtendedJavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;

import java.lang.reflect.Field;
import java.util.*;

public class StarQuests extends ExtendedJavaPlugin {
    private Set<QuestAction<?>> actions = new HashSet<>();
    private Set<String> completedActions = new HashSet<>();
    
    @Override
    public void onEnable() {
        super.onEnable();
        StarEvents.subscribe(getEventBus());
        getEventBus().subscribe(this);
        
        this.actions.add(getInjector().inject(new QuestAction<>("test1", "Test 1", List.of(), BlockBreakEvent.class, e -> true)));
    }
    
    @SubscribeEvent
    public void onEvent(Event e) {
        for (QuestAction<?> action : this.actions) {
            try {
                boolean test = action.check(e) && !this.completedActions.contains(action.id);
                if (test) {
                    this.completedActions.add(action.id);
                    Set<Field> fields = ReflectionHelper.getClassFields(e.getClass());
                    for (Field field : fields) {
                        field.setAccessible(true);
                        Object fieldValue;
                        try {
                            fieldValue = field.get(e);
                        } catch (IllegalAccessException ex) {
                            throw new RuntimeException(ex);
                        }
                        
                        if (fieldValue instanceof Player player) {
                            player.sendMessage(getColors().colorLegacy("&aCompleted Quest: &b" + action.name));
                        }
                    }
                }
            } catch (Throwable ex) {}
        }
    }
}