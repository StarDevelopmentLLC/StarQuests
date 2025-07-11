package com.stardevllc.starquests.actions;

import com.stardevllc.dependency.Inject;
import com.stardevllc.starquests.StarQuests;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an action for a quest
 */
public class QuestAction<T> {
    @Inject
    protected StarQuests starQuests;
    
    protected String id;
    protected String name;
    protected List<String> description;
    protected Class<T> type;
    protected QuestActionPredicate<T> predicate;
    protected List<String> prerequisiteActions = new ArrayList<>();
    
    public QuestAction(String id, String name, List<String> description, Class<T> type, QuestActionPredicate<T> predicate, List<String> prerequisiteActions) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.predicate = predicate;
        this.prerequisiteActions.addAll(prerequisiteActions);
    }
    
    public boolean check(Event event, QuestActionData data) {
        try {
            if (event.getClass().equals(type)) {
                T t = (T) event;
                return this.predicate.test(this, t, data);
            }
        } catch (Exception exception) {
            return false;
        }
        
        return false;
    }
    
    public StarQuests getStarQuests() {
        return starQuests;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public List<String> getDescription() {
        return description;
    }
    
    public Class<T> getType() {
        return type;
    }
    
    public QuestActionPredicate<T> getPredicate() {
        return predicate;
    }
    
    public List<String> getPrerequisiteActions() {
        return prerequisiteActions;
    }
}