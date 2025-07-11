package com.stardevllc.starquests.actions;

import com.stardevllc.dependency.Inject;
import org.bukkit.event.Event;

import java.util.List;
import java.util.function.BiPredicate;

/**
 * Represents an action for a quest
 */
public class QuestAction<T> {
    @Inject
    protected StarQuests plugin;
    
    protected String id;
    protected String name;
    protected List<String> description;
    protected Class<T> type;
    protected BiPredicate<QuestAction<T>, T> predicate;
    
    public QuestAction(String id, String name, List<String> description, Class<T> type, BiPredicate<QuestAction<T>, T> predicate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.predicate = predicate;
    }
    
    public boolean check(Event event) {
        try {
            if (event.getClass().equals(type)) {
                T t = (T) event;
                return this.predicate.test(this, t);
            }
        } catch (Exception exception) {
            return false;
        }
        
        return false;
    }
}