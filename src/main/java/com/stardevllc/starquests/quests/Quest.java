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

public class Quest<H extends QuestHolder<?>> implements Comparable<Quest<H>> {
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
    
    protected Class<H> holderType;
    protected String id;
    protected String name;
    protected List<String> description = new LinkedList<>();
    protected Set<String> requiredQuests = new HashSet<>();
    protected ActionRegistry actions;
    protected QuestConsumer<H> onComplete;
    
    @Inject
    protected QuestLine questLine;
    
    protected DependencyInjector injector;
    
    public Quest(Class<H> holderType, String id, String name, List<String> description, List<String> requiredQuests, QuestConsumer<H> onComplete) {
        this(holderType, id, name, description, requiredQuests, onComplete, null);
    }
    
    public Quest(Class<H> holderType, String id, String name, List<String> description, List<String> requiredQuests, QuestConsumer<H> onComplete, Map<String, QuestAction<?, H>> actions) {
        this.holderType = holderType;
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
        return new LinkedList<>(description);
    }
    
    public List<String> getRequiredQuests() {
        return new ArrayList<>(requiredQuests);
    }
    
    public ActionRegistry getActions() {
        return actions;
    }
    
    public QuestConsumer<H> getOnComplete() {
        return onComplete;
    }
    
    public void handleOnComplete(H holder) {
        if (this.onComplete != null) {
            this.onComplete.apply(this, holder);
        }
    }
    
    public boolean isAvailable(QuestHolder<?> holder) {
        if (!holderType.isAssignableFrom(holder.getClass())) {
            return false;
        }
        
        if (holder.isQuestComplete(this)) {
            return false;
        }
        
        if (questLine != null && !questLine.isAvailable(holder)) {
            return false;
        }
        
        for (String rq : getRequiredQuests()) {
            Quest<?> requiredQuest = questRegistry.get(rq);
            if (!holder.isQuestComplete(requiredQuest)) {
                return false;
            }
        }
        
        return true;
    }
    
    public static <H extends QuestHolder<?>> Builder<H> builder(Class<H> holderType) {
        return new Builder<>(holderType);
    }
    
    @Override
    public int compareTo(Quest o) {
        return this.id.compareTo(o.id);
    }
    
    public static class Builder<H extends QuestHolder<?>> implements IBuilder<Quest<H>, Builder<H>> {
        protected final Class<H> holderType;
        protected String id;
        protected String name;
        protected List<String> description = new LinkedList<>();
        protected List<String> requiredQuests = new ArrayList<>();
        protected Map<String, QuestAction<?, H>> actions = new HashMap<>();
        protected QuestConsumer<H> onComplete;
        
        public Builder(Class<H> holderType) {
            this.holderType = holderType;
        }
        
        public Builder(Builder<H> builder) {
            this(builder.holderType);
            this.id = builder.id;
            this.name = builder.id;
            this.description.addAll(builder.description);
            this.requiredQuests.addAll(builder.requiredQuests);
            this.actions.putAll(builder.actions);
            this.onComplete = builder.onComplete;
        }
        
        public Builder<H> id(String id) {
            this.id = id;
            return self();
        }
        
        public Builder<H> name(String name) {
            this.name = name;
            return self();
        }
        
        public Builder<H> description(String... description) {
            this.description.clear();
            this.description.addAll(List.of(description));
            return self();
        }
        
        public Builder<H> requiredQuests(String... requiredQuests) {
            this.requiredQuests.clear();
            this.requiredQuests.addAll(List.of(requiredQuests));
            return self();
        }
        
        @SafeVarargs
        public final Builder<H> addAction(QuestAction<?, H> firstAction, QuestAction<?, H>... actions) {
            this.actions.put(firstAction.getId(), firstAction);
            if (actions != null) {
                for (QuestAction<?, H> action : actions) {
                    this.actions.put(action.getId(), action);
                }
            }
            
            return self();
        }
        
        @SafeVarargs
        public final Builder<H> addAction(QuestAction.Builder<?, H> actionBuilder, QuestAction.Builder<?, H>... actionBuilders) {
            QuestAction<?, H> firstAction = actionBuilder.build();
            this.actions.put(firstAction.getId(), firstAction);
            if (actionBuilders != null) {
                for (QuestAction.Builder<?, H> builder : actionBuilders) {
                    QuestAction<?, H> action = builder.build();
                    this.actions.put(action.getId(), action);
                }
            }
            
            return self();
        }
        
        public Builder<H> onComplete(QuestConsumer<H> onComplete) {
            this.onComplete = onComplete;
            return self();
        } 
        
        @Override
        public Quest<H> build() {
            if ((id == null || id.isBlank()) && name != null && !name.isBlank()) {
                id = ChatColor.stripColor(StarColors.color(name.toLowerCase().replace(" ", "_")));
            }
            
            if ((name == null || name.isBlank()) && id != null && !id.isBlank()) {
                name = StringHelper.titlize(this.id);
            }
            
            return new Quest<>(holderType, id, name, description, requiredQuests, onComplete, actions);
        }
        
        @Override
        public Builder<H> clone() {
            return new Builder<>(this);
        }
    }
}