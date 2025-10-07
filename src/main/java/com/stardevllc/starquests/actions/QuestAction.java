package com.stardevllc.starquests.actions;

import com.stardevllc.starcore.api.StarColors;
import com.stardevllc.starlib.builder.IBuilder;
import com.stardevllc.starlib.dependency.Inject;
import com.stardevllc.starlib.helper.StringHelper;
import com.stardevllc.starquests.actions.function.*;
import com.stardevllc.starquests.actions.function.QuestActionAvailablePredicate.Availability;
import com.stardevllc.starquests.actions.function.QuestActionTriggerPredicate.Status;
import com.stardevllc.starquests.events.ActionUpdateEvent;
import com.stardevllc.starquests.holder.QuestHolder;
import com.stardevllc.starquests.quests.Quest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.*;

/**
 * Represents an action for a quest
 */
public class QuestAction<T, H extends QuestHolder<?>> implements Comparable<QuestAction<T, H>> {
    protected Class<H> holderType;
    protected String id;
    protected String name;
    protected List<String> description = new LinkedList<>();
    protected Class<T> type;
    protected QuestActionAvailablePredicate<T, H> availablePredicate;
    protected QuestActionTriggerPredicate<T, H> triggerPredicate;
    protected List<String> requiredActions = new ArrayList<>();
    protected QuestActionConsumer<T, H> onUpdate, onComplete;
    protected boolean markCompleteForHolder = true;
    
    @Inject
    protected Quest<H> quest;
    
    public QuestAction(Class<H> holderType, String id, String name, List<String> description, Class<T> type, QuestActionAvailablePredicate<T, H> availablePredicate, QuestActionTriggerPredicate<T, H> triggerPredicate, List<String> requiredActions, QuestActionConsumer<T, H> onUpdate, QuestActionConsumer<T, H> onComplete, boolean markCompleteForHolder) {
        this.holderType = holderType;
        this.id = id;
        this.name = name;
        this.description.addAll(description);
        this.type = type;
        this.availablePredicate = availablePredicate;
        this.triggerPredicate = triggerPredicate;
        this.requiredActions.addAll(requiredActions);
        this.onUpdate = onUpdate;
        this.onComplete = onComplete;
        this.markCompleteForHolder = markCompleteForHolder;
    }
    
    public Status check(Object trigger, QuestHolder<?> holder, QuestActionData data) {
        if (!holderType.isAssignableFrom(holder.getClass())) {
            return Status.WRONG_HOLDER_TYPE;
        }
        
        try {
            if (trigger.getClass().equals(type)) {
                T t = (T) trigger;
                Status result = this.triggerPredicate.test(this, t, (H) holder, data);
                if (result == Status.COMPLETE || result == Status.IN_PROGRESS) {
                    Bukkit.getPluginManager().callEvent(new ActionUpdateEvent(this, data));
                    if (this.onUpdate != null) {
                        this.onUpdate.apply(this, t, (H) holder, data);
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
    
    
    public void handleOnComplete(Object trigger, QuestHolder<?> holder, QuestActionData data) {
        if (this.onComplete != null) {
            if (!holderType.isAssignableFrom(holder.getClass())) {
                return;
            }
            
            try {
                if (trigger.getClass().equals(type)) {
                    T t = (T) trigger;
                    this.onComplete.apply(this, t, (H) holder, data);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        if (markCompleteForHolder) {
            holder.completeAction(this);
        }
    }
    
    public Class<H> getHolderType() {
        return holderType;
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
    
    public QuestActionTriggerPredicate<T, H> getTriggerPredicate() {
        return triggerPredicate;
    }
    
    public QuestActionAvailablePredicate<T, H> getAvailablePredicate() {
        return availablePredicate;
    }
    
    public List<String> getRequiredActions() {
        return requiredActions;
    }
    
    public Quest<H> getQuest() {
        return quest;
    }
    
    public Availability isAvailable(QuestHolder<?> holder, QuestActionData data) {
        if (!holderType.isAssignableFrom(holder.getClass())) {
            return Availability.WRONG_HOLDER_TYPE;
        }
        
        if (this.availablePredicate != null) {
            return this.availablePredicate.test(this, (H) holder, data);
        }
        
        if (holder.isActionComplete(this)) {
            return Availability.COMPLETE;
        }
        
        if (getQuest() != null) {
            if (!getQuest().isAvailable(holder).asBoolean()) {
                return Availability.LOCKED;
            }
            
            for (String ra : getRequiredActions()) {
                QuestAction<?, ?> requiredAction = getQuest().getActions().get(ra);
                if (requiredAction != null) {
                    if (!holder.isActionComplete(requiredAction)) {
                        return Availability.LOCKED;
                    }
                }
            }
        }
        
        return Availability.AVAILABLE;
    }
    
    public Availability isAvailable(QuestHolder<?> holder) {
        return isAvailable(holder, null);
    }
    
    public static <T, H extends QuestHolder<?>> Builder<T, H> builder(Class<T> type, Class<H> holderType) {
        return new Builder<>(type, holderType);
    }
    
    @Override
    public int compareTo(QuestAction<T, H> o) {
        //If this action has the other action as a requirement, then this action is less than the other action
        if (this.requiredActions.contains(o.getId())) {
            return -1;
        }
        
        //If the other action has this quest as a requirement, then this action is greater than the other action
        if (o.requiredActions.contains(this.id)) {
            return 1;
        }
        
        //Otherwise, just sort the actions based on the id
        return this.id.compareTo(o.id);
    }
    
    public static class Builder<T, H extends QuestHolder<?>> implements IBuilder<QuestAction<T, H>, Builder<T, H>> {
        protected final Class<T> type;
        protected final Class<H> holderType;
        protected String id;
        protected String name;
        protected List<String> description = new LinkedList<>();
        protected QuestActionAvailablePredicate<T, H> availablePredicate;
        protected QuestActionTriggerPredicate<T, H> triggerPredicate;
        protected List<String> requiredActions = new ArrayList<>();
        protected QuestActionConsumer<T, H> onUpdate, onComplete;
        protected boolean markCompleteForHolder = true;
        
        public Builder(Class<T> type, Class<H> holderType) {
            this.type = type;
            this.holderType = holderType;
        }
        
        public Builder(Builder<T, H> builder) {
            this(builder.type, builder.holderType);
            this.id = builder.id;
            this.name = builder.name;
            this.description.addAll(builder.description);
            this.availablePredicate = builder.availablePredicate;
            this.triggerPredicate = builder.triggerPredicate;
            this.requiredActions = new LinkedList<>(builder.requiredActions);
            this.onUpdate = builder.onUpdate;
            this.onComplete = builder.onComplete;
            this.markCompleteForHolder = builder.markCompleteForHolder;
        }
        
        public Builder<T, H> id(String id) {
            this.id = id;
            return self();
        }
        
        public Builder<T, H> name(String name) {
            this.name = name;
            return self();
        }
        
        public Builder<T, H> description(String... description) {
            if (description != null) {
                this.description = new LinkedList<>(Arrays.asList(description));
            }
            
            return self();
        }
        
        public Builder<T, H> availablePredicate(QuestActionAvailablePredicate<T, H> predicate) {
            this.availablePredicate = predicate;
            return self();
        }
        
        public Builder<T, H> triggerPredicate(QuestActionTriggerPredicate<T, H> predicate) {
            this.triggerPredicate = predicate;
            return self();
        }
        
        public Builder<T, H> requiredActions(String... prerequisiteActions) {
            if (prerequisiteActions != null) {
                this.requiredActions = new LinkedList<>(Arrays.asList(prerequisiteActions));
            }
            
            return self();
        }
        
        public Builder<T, H> onUpdate(QuestActionConsumer<T, H> onUpdate) {
            this.onUpdate = onUpdate;
            return self();
        }
        
        public Builder<T, H> onComplete(QuestActionConsumer<T, H> onComplete) {
            this.onComplete = onComplete;
            return self();
        }
        
        public Builder<T, H> markCompleteForHolder(boolean markCompleteForHolder) {
            this.markCompleteForHolder = markCompleteForHolder;
            return self();
        }
        
        @Override
        public QuestAction<T, H> build() {
            if ((id == null || id.isBlank()) && name != null && !name.isBlank()) {
                id = ChatColor.stripColor(StarColors.color(name.toLowerCase().replace(" ", "_")));
            }
            
            if ((name == null || name.isBlank()) && id != null && !id.isBlank()) {
                name = StringHelper.titlize(this.id);
            }
            
            return new QuestAction<>(holderType, id, name, description, type, availablePredicate, triggerPredicate, requiredActions, onUpdate, onComplete, markCompleteForHolder);
        }
        
        @Override
        public Builder<T, H> clone() {
            return new Builder<>(this);
        }
    }
}