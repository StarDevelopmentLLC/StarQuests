package com.stardevllc.starquests.quests;

import com.stardevllc.starcore.api.StarColors;
import com.stardevllc.starlib.builder.IBuilder;
import com.stardevllc.starlib.dependency.DependencyInjector;
import com.stardevllc.starlib.dependency.Inject;
import com.stardevllc.starlib.helper.StringHelper;
import com.stardevllc.starquests.*;
import com.stardevllc.starquests.actions.QuestAction;
import com.stardevllc.starquests.holder.QuestHolder;
import com.stardevllc.starquests.line.QuestLine;
import com.stardevllc.starquests.quests.function.QuestConsumer;
import com.stardevllc.starquests.registry.ActionRegistry;
import com.stardevllc.starquests.registry.QuestRegistry;
import org.bukkit.ChatColor;

import java.util.*;

public class Quest implements Comparable<Quest> {
    private static ActionRegistry primaryActionRegistry;
    public static void setPrimaryActionRegistry(ActionRegistry registry) {
        if (primaryActionRegistry == null) {
            primaryActionRegistry = registry;
        }
    }
    
    @Inject
    protected StarQuests starQuests;
    
    @Inject
    protected QuestRegistry questRegistry;
    
    protected String id;
    protected String name;
    protected List<String> description = new LinkedList<>();
    protected Set<String> requiredQuests = new HashSet<>();
    protected ActionRegistry actions;
    protected QuestConsumer onComplete;
    
    @Inject
    protected QuestLine questLine;
    
    protected DependencyInjector injector;
    
    public Quest(String id, String name, List<String> description, List<String> requiredQuests, QuestConsumer onComplete) {
        this(id, name, description, requiredQuests, onComplete, null);
    }
    
    public Quest(String id, String name, List<String> description, List<String> requiredQuests, QuestConsumer onComplete, Map<String, QuestAction<?>> actions) {
        this.id = id;
        this.name = name;
        this.description.addAll(description);
        this.requiredQuests.addAll(requiredQuests);
        this.onComplete = onComplete;
        this.injector = DependencyInjector.create();
        this.injector.setInstance(this);
        this.actions = new ActionRegistry(this.injector);
        if (actions != null) {
            this.actions.putAll(actions);
            actions.values().forEach(a -> {
                this.actions.register(a);
                primaryActionRegistry.register(a);
            });
        }
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public List<String> getDescription() {
        return new LinkedList<>(description);
    }
    
    public List<String> getRequiredQuests() {
        return new ArrayList<>(requiredQuests);
    }
    
    public ActionRegistry getActions() {
        return actions;
    }
    
    public QuestConsumer getOnComplete() {
        return onComplete;
    }
    
    public boolean isAvailable(QuestHolder<?> holder) {
        if (holder.isQuestComplete(this)) {
            return false;
        }
        
        if (questLine != null && !questLine.isAvailable(holder)) {
            return false;
        }
        
        for (String rq : getRequiredQuests()) {
            Quest requiredQuest = questRegistry.get(rq);
            if (!holder.isQuestComplete(requiredQuest)) {
                return false;
            }
        }
        
        return true;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @Override
    public int compareTo(Quest o) {
        return this.id.compareTo(o.id);
    }
    
    public static class Builder implements IBuilder<Quest, Builder> {
        protected String id;
        protected String name;
        protected List<String> description = new LinkedList<>();
        protected List<String> requiredQuests = new ArrayList<>();
        protected Map<String, QuestAction<?>> actions = new HashMap<>();
        protected QuestConsumer onComplete;
        
        public Builder() {}
        
        public Builder(Builder builder) {
            this.id = builder.id;
            this.name = builder.id;
            this.description.addAll(builder.description);
            this.requiredQuests.addAll(builder.requiredQuests);
            this.actions.putAll(builder.actions);
            this.onComplete = builder.onComplete;
        }
        
        public Builder id(String id) {
            this.id = id;
            return self();
        }
        
        public Builder name(String name) {
            this.name = name;
            return self();
        }
        
        public Builder description(String... description) {
            this.description.clear();
            this.description.addAll(List.of(description));
            return self();
        }
        
        public Builder requiredQuests(String... requiredQuests) {
            this.requiredQuests.clear();
            this.requiredQuests.addAll(List.of(requiredQuests));
            return self();
        }
        
        public Builder addAction(QuestAction<?> firstAction, QuestAction<?>... actions) {
            this.actions.put(firstAction.getId(), firstAction);
            if (actions != null) {
                for (QuestAction<?> action : actions) {
                    this.actions.put(action.getId(), action);
                }
            }
            
            return self();
        }
        
        public Builder addAction(QuestAction.Builder<?> actionBuilder, QuestAction.Builder<?>... actionBuilders) {
            QuestAction<?> firstAction = actionBuilder.build();
            this.actions.put(firstAction.getId(), firstAction);
            if (actionBuilders != null) {
                for (QuestAction.Builder<?> builder : actionBuilders) {
                    QuestAction<?> action = builder.build();
                    this.actions.put(action.getId(), action);
                }
            }
            
            return self();
        }
        
        public Builder onComplete(QuestConsumer onComplete) {
            this.onComplete = onComplete;
            return self();
        } 
        
        @Override
        public Quest build() {
            if ((id == null || id.isBlank()) && name != null && !name.isBlank()) {
                id = ChatColor.stripColor(StarColors.color(name.toLowerCase().replace(" ", "_")));
            }
            
            if ((name == null || name.isBlank()) && id != null && !id.isBlank()) {
                name = StringHelper.titlize(this.id);
            }
            
            return new Quest(id, name, description, requiredQuests, onComplete, actions);
        }
        
        @Override
        public Builder clone() {
            return new Builder(this);
        }
    }
}