package com.stardevllc.starquests.quests;

import com.stardevllc.starcore.api.StarColors;
import com.stardevllc.starlib.builder.IBuilder;
import com.stardevllc.starlib.dependency.DependencyInjector;
import com.stardevllc.starlib.dependency.Inject;
import com.stardevllc.starlib.helper.StringHelper;
import com.stardevllc.starquests.StarQuests;
import com.stardevllc.starquests.actions.QuestAction;
import com.stardevllc.starquests.quests.function.QuestConsumer;
import org.bukkit.ChatColor;

import java.util.*;

public class Quest {
    @Inject
    protected StarQuests starQuests;
    
    protected String id;
    protected String name;
    protected List<String> description = new LinkedList<>();
    protected Set<String> prerequisiteQuests = new HashSet<>();
    protected Map<String, QuestAction<?>> actions = new LinkedHashMap<>();
    protected QuestConsumer onComplete;
    
    protected DependencyInjector injector;
    
    public Quest(String id, String name, List<String> description, List<String> prerequisiteQuests, QuestConsumer onComplete) {
        this(id, name, description, prerequisiteQuests, onComplete, null);
    }
    
    public Quest(String id, String name, List<String> description, List<String> prerequisiteQuests, QuestConsumer onComplete, Map<String, QuestAction<?>> actions) {
        this.id = id;
        this.name = name;
        this.description.addAll(description);
        this.prerequisiteQuests.addAll(prerequisiteQuests);
        this.onComplete = onComplete;
        this.injector = DependencyInjector.create();
        this.injector.setInstance(this);
        if (actions != null) {
            this.actions.putAll(actions);
            actions.values().forEach(a -> injector.inject(a));
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
    
    public List<String> getPrerequisiteQuests() {
        return new ArrayList<>(prerequisiteQuests);
    }
    
    public Map<String, QuestAction<?>> getActions() {
        return new HashMap<>(actions);
    }
    
    public QuestConsumer getOnComplete() {
        return onComplete;
    }
    
    public Quest addAction(QuestAction<?> action) {
        this.actions.put(action.getId(), action);
        this.injector.inject(action);
        return this;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder implements IBuilder<Quest, Builder> {
        protected String id;
        protected String name;
        protected List<String> description = new LinkedList<>();
        protected List<String> prerequisiteQuests = new ArrayList<>();
        protected Map<String, QuestAction<?>> actions = new HashMap<>();
        protected QuestConsumer onComplete;
        
        public Builder() {}
        
        public Builder(Builder builder) {
            this.id = builder.id;
            this.name = builder.id;
            this.description.addAll(builder.description);
            this.prerequisiteQuests.addAll(builder.prerequisiteQuests);
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
        
        public Builder prerequisiteQuests(String... prerequisiteQuests) {
            this.prerequisiteQuests.clear();
            this.prerequisiteQuests.addAll(List.of(prerequisiteQuests));
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
            
            return new Quest(id, name, description, prerequisiteQuests, onComplete, actions);
        }
        
        @Override
        public Builder clone() {
            return null;
        }
    }
}