package com.stardevllc.starquests.actions;

import com.stardevllc.starcore.api.StarColors;
import com.stardevllc.starlib.builder.IBuilder;
import com.stardevllc.starlib.dependency.Inject;
import com.stardevllc.starlib.helper.StringHelper;
import com.stardevllc.starquests.QuestPlayer;
import com.stardevllc.starquests.actions.function.QuestActionConsumer;
import com.stardevllc.starquests.actions.function.QuestActionPredicate;
import com.stardevllc.starquests.actions.function.QuestActionPredicate.Status;
import com.stardevllc.starquests.events.ActionUpdateEvent;
import com.stardevllc.starquests.quests.Quest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.*;

/**
 * Represents an action for a quest
 */
public class QuestAction<T> {
    protected String id;
    protected String name;
    protected List<String> description = new LinkedList<>();
    protected Class<T> type;
    protected QuestActionPredicate<T> predicate;
    protected List<String> requiredActions = new ArrayList<>();
    protected QuestActionConsumer<T> onUpdate, onComplete;
    
    @Inject
    protected Quest quest;
    
    public QuestAction(String id, String name, List<String> description, Class<T> type, QuestActionPredicate<T> predicate, List<String> requiredActions, QuestActionConsumer<T> onUpdate, QuestActionConsumer<T> onComplete) {
        this.id = id;
        this.name = name;
        this.description.addAll(description);
        this.type = type;
        this.predicate = predicate;
        this.requiredActions.addAll(requiredActions);
        this.onUpdate = onUpdate;
        this.onComplete = onComplete;
    }
    
    public Status check(Object trigger, QuestActionData data) {
        try {
            if (trigger.getClass().equals(type)) {
                T t = (T) trigger;
                Status result = this.predicate.test(this, t, data);
                if (result == Status.COMPLETE || result == Status.IN_PROGRESS) {
                    Bukkit.getPluginManager().callEvent(new ActionUpdateEvent(this, data));
                    if (this.onUpdate != null) {
                        this.onUpdate.apply(this, t, data);
                    }
                }
                return result;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            return Status.ERROR;
        }
        
        return Status.FALSE;
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
    
    public List<String> getRequiredActions() {
        return requiredActions;
    }
    
    public Quest getQuest() {
        return quest;
    }
    
    public boolean isAvailable(QuestPlayer player) {
        if (player.isActionComplete(this)) {
            return false;
        }
        
        if (getQuest() != null) {
            if (!getQuest().isAvailable(player)) {
                return false;
            }
            
            for (String ra : getRequiredActions()) {
                QuestAction<?> requiredAction = getQuest().getActions().get(ra);
                if (requiredAction != null) {
                    if (!player.isActionComplete(requiredAction)) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    public static <T> Builder<T> builder(Class<T> type) {
        return new Builder<>(type);
    }
    
    public static class Builder<T> implements IBuilder<QuestAction<T>, Builder<T>> {
        protected final Class<T> type;
        protected String id;
        protected String name;
        protected List<String> description = new LinkedList<>();
        protected QuestActionPredicate<T> predicate;
        protected List<String> requiredActions = new ArrayList<>();
        protected QuestActionConsumer<T> onUpdate, onComplete;
        
        public Builder(Class<T> type) {
            this.type = type;
        }
        
        public Builder(Builder<T> builder) {
            this.type = builder.type;
            this.id = builder.id;
            this.name = builder.name;
            this.description.addAll(builder.description);
            this.predicate = builder.predicate;
            this.requiredActions = new LinkedList<>(builder.requiredActions);
            this.onUpdate = builder.onUpdate;
            this.onComplete = builder.onComplete;
        }
        
        public Builder<T> id(String id) {
            this.id = id;
            return self();
        }
        
        public Builder<T> name(String name) {
            this.name = name;
            return self();
        }
        
        public Builder<T> description(String... description) {
            if (description != null) {
                this.description = new LinkedList<>(Arrays.asList(description));
            }
            
            return self();
        }
        
        public Builder<T> predicate(QuestActionPredicate<T> predicate) {
            this.predicate = predicate;
            return self();
        }
        
        public Builder<T> requiredActions(String... prerequisiteActions) {
            if (prerequisiteActions != null) {
                this.requiredActions = new LinkedList<>(Arrays.asList(prerequisiteActions));
            }
            
            return self();
        }
        
        public Builder<T> onUpdate(QuestActionConsumer<T> onUpdate) {
            this.onUpdate = onUpdate;
            return self();
        }
        
        public Builder<T> onComplete(QuestActionConsumer<T> onComplete) {
            this.onComplete = onComplete;
            return self();
        }
        
        @Override
        public QuestAction<T> build() {
            if ((id == null || id.isBlank()) && name != null && !name.isBlank()) {
                id = ChatColor.stripColor(StarColors.color(name.toLowerCase().replace(" ", "_")));
            }
            
            if ((name == null || name.isBlank()) && id != null && !id.isBlank()) {
                name = StringHelper.titlize(this.id);
            }
            
            return new QuestAction<>(id, name, description, type, predicate, requiredActions, onUpdate, onComplete);
        }
        
        @Override
        public Builder<T> clone() {
            return new Builder<>(this);
        }
    }
}