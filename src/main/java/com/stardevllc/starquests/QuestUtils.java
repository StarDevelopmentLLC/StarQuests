package com.stardevllc.starquests;

import com.stardevllc.helper.ReflectionHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.lang.reflect.Field;
import java.util.*;

public final class QuestUtils {
    
    private static final Map<Class<? extends Event>, Set<Field>> eventFields = new HashMap<>();
    
    public static Optional<Player> getPlayerFromEvent(Event event) {
        Set<Field> fields;
        if (eventFields.containsKey(event.getClass())) {
            fields = eventFields.get(event.getClass());
        } else {
            fields = ReflectionHelper.getClassFields(event.getClass());
            eventFields.put(event.getClass(), Collections.unmodifiableSet(fields));
        }
        for (Field field : fields) {
            field.setAccessible(true);
            Object fieldValue;
            try {
                fieldValue = field.get(event);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
            
            if (fieldValue instanceof Player player) {
                return Optional.of(player);
            }
        }
        
        return Optional.empty();
    }
}