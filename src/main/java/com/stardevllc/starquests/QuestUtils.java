package com.stardevllc.starquests;

import com.stardevllc.starlib.helper.ReflectionHelper;
import com.stardevllc.starquests.holder.QuestHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;

public final class QuestUtils {
    
    private static final Map<Class<? extends Event>, Set<Field>> eventFields = new HashMap<>();
    private static final Map<Class<? extends Event>, Set<Method>> eventMethods = new HashMap<>();
    
    private static final Map<Class<? extends Event>, Function<Event, Player>> eventPlayerMappers = new HashMap<>();
    
    private static final List<Function<Object, QuestHolder<?>>> holderMappers = new ArrayList<>();
    
    public static <T> void addHolderMapper(Function<T, QuestHolder<?>> mapper) {
        holderMappers.add((Function<Object, QuestHolder<?>>) mapper);
    }
    
    public static <T extends Event> void addEventPlayerMapper(Class<T> eventType, Function<T, Player> mapper) {
        eventPlayerMappers.put(eventType, (Function<Event, Player>) mapper);
    }
    
    public static List<QuestHolder<?>> getHoldersFromTrigger(Object trigger) {
        List<QuestHolder<?>> holders = new ArrayList<>();
        for (Function<Object, QuestHolder<?>> holderMapper : holderMappers) {
            QuestHolder<?> holder = holderMapper.apply(trigger);
            if (holder != null) {
                holders.add(holder);
            }
        }
        
        return holders;
    }
    
    public static Optional<Player> getPlayerFromEvent(Event event) {
        if (eventPlayerMappers.containsKey(event.getClass())) {
            return Optional.ofNullable(eventPlayerMappers.get(event.getClass()).apply(event));
        }
        
        Set<Field> fields;
        if (eventFields.containsKey(event.getClass())) {
            fields = eventFields.get(event.getClass());
        } else {
            fields = new HashSet<>(ReflectionHelper.getClassFields(event.getClass()).values());
            eventFields.put(event.getClass(), Collections.unmodifiableSet(fields));
        }
        
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(event);
                if (fieldValue instanceof Player player) {
                    return Optional.of(player);
                }
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }
        
        Set<Method> methods;
        if (eventMethods.containsKey(event.getClass())) {
            methods = eventMethods.get(event.getClass());
        } else {
            methods = ReflectionHelper.getClassMethods(event.getClass());
            eventMethods.put(event.getClass(), Collections.unmodifiableSet(methods));
        }
        
        for (Method method : methods) {
            if (method.getParameterCount() == 0) {
                try {
                    method.setAccessible(true);
                    Object methodValue = method.invoke(event);
                    if (methodValue instanceof Player player) {
                        return Optional.of(player);
                    }
                } catch (Throwable e) {}
            }
        }
        
        return Optional.empty();
    }
}