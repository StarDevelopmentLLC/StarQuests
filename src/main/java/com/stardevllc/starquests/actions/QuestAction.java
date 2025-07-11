package com.stardevllc.starquests.actions;

import com.stardevllc.dependency.Inject;
import com.stardevllc.starquests.StarQuests;
import com.stardevllc.starquests.actions.function.QuestActionConsumer;
import com.stardevllc.starquests.actions.function.QuestActionPredicate;
import com.stardevllc.starquests.events.ActionUpdateEvent;
import org.bukkit.Bukkit;

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
    protected QuestActionConsumer<T> onUpdate, onComplete;
    
    public QuestAction(String id, String name, List<String> description, Class<T> type, QuestActionPredicate<T> predicate, List<String> prerequisiteActions, QuestActionConsumer<T> onUpdate, QuestActionConsumer<T> onComplete) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.predicate = predicate;
        this.prerequisiteActions.addAll(prerequisiteActions);
        this.onUpdate = onUpdate;
        this.onComplete = onComplete;
    }
    
    public boolean check(Object trigger, QuestActionData data) {
        try {
            if (trigger.getClass().equals(type)) {
                T t = (T) trigger;
                boolean result = this.predicate.test(this, t, data);
                Bukkit.getPluginManager().callEvent(new ActionUpdateEvent(this, data));
                if (this.onUpdate != null) {
                    this.onUpdate.apply(this, t, data);
                }
                return result;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
        
        return false;
    }
    
    
    public void handleOnComplete(Object trigger, QuestActionData data) {
        if (this.onComplete != null) {
            try {
                if (trigger.getClass().equals(type)) {
                    T t = (T) trigger;
                    this.onComplete.apply(this, t, data);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
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