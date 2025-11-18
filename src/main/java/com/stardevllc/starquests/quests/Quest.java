package com.stardevllc.starquests.quests;

import com.stardevllc.starcore.api.StarColors;
import com.stardevllc.starlib.helper.StringHelper;
import com.stardevllc.starlib.injector.FieldInjector;
import com.stardevllc.starlib.injector.Inject;
import com.stardevllc.starlib.objects.builder.IBuilder;
import com.stardevllc.starquests.StarQuests;
import com.stardevllc.starquests.actions.QuestAction;
import com.stardevllc.starquests.holder.QuestHolder;
import com.stardevllc.starquests.line.QuestLine;
import com.stardevllc.starquests.quests.function.QuestAvailablePredicate;
import com.stardevllc.starquests.quests.function.QuestAvailablePredicate.Availability;
import com.stardevllc.starquests.quests.function.QuestConsumer;
import com.stardevllc.starquests.registry.ActionRegistry;
import com.stardevllc.starquests.registry.QuestRegistry;
import org.bukkit.ChatColor;

import java.util.*;
import java.util.function.Consumer;

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
    protected QuestAvailablePredicate<H> availablePredicate;
    protected QuestConsumer<H> onComplete;
    protected boolean markCompleteForHolder = true;
    
    @Inject
    protected QuestLine<H> questLine;
    
    protected FieldInjector injector;
    
    public Quest(Class<H> holderType, String id, String name, List<String> description, List<String> requiredQuests, QuestAvailablePredicate<H> availablePredicate, QuestConsumer<H> onComplete, boolean markCompleteForHolder) {
        this(holderType, id, name, description, requiredQuests, availablePredicate, onComplete, markCompleteForHolder, null);
    }
    
    public Quest(Class<H> holderType, String id, String name, List<String> description, List<String> requiredQuests, QuestAvailablePredicate<H> availablePredicate, QuestConsumer<H> onComplete, boolean markCompleteForHolder, Map<String, QuestAction<?, H>> actions) {
        this.holderType = holderType;
        this.id = id;
        this.name = name;
        this.description.addAll(description);
        this.requiredQuests.addAll(requiredQuests);
        this.availablePredicate = availablePredicate;
        this.onComplete = onComplete;
        this.injector = FieldInjector.create();
        this.injector.set(this);
        this.actions = new ActionRegistry(this.injector);
        this.markCompleteForHolder = markCompleteForHolder;
        if (actions != null) {
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
    
    public QuestAvailablePredicate<H> getAvailablePredicate() {
        return availablePredicate;
    }
    
    public QuestConsumer<H> getOnComplete() {
        return onComplete;
    }
    
    public void handleOnComplete(H holder) {
        if (this.onComplete != null) {
            this.onComplete.apply(this, holder);
        }
        
        if (markCompleteForHolder) {
            holder.completeQuest(this);
        }
    }
    
    public Availability isAvailable(QuestHolder<?> holder) {
        if (!this.holderType.isAssignableFrom(holder.getClass())) {
            return Availability.WRONG_HOLDER_TYPE;
        }
        
        if (this.availablePredicate != null) {
            return this.availablePredicate.test(this, (H) holder);
        }
        
        if (holder.isQuestComplete(this)) {
            return Availability.COMPLETE;
        }
        
        if (questLine != null && !questLine.isAvailable(holder).asBoolean()) {
            return Availability.LOCKED;
        }
        
        for (String rq : getRequiredQuests()) {
            Quest<?> requiredQuest = questRegistry.get(rq);
            if (!holder.isQuestComplete(requiredQuest)) {
                return Availability.LOCKED;
            }
        }
        
        return Availability.AVAILABLE;
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
        protected QuestAvailablePredicate<H> availablePredicate;
        protected QuestConsumer<H> onComplete;
        protected boolean markCompleteForHolder = true;
        
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
            this.availablePredicate = builder.availablePredicate;
            this.onComplete = builder.onComplete;
            this.markCompleteForHolder = builder.markCompleteForHolder;
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
        
        public <T> Builder<H> createAction(Class<T> type, Consumer<QuestAction.Builder<T, H>> consumer) {
            QuestAction.Builder<T, H> builder = QuestAction.builder(type, holderType);
            consumer.accept(builder);
            return addAction(builder);
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
        
        public Builder<H> availablePredicate(QuestAvailablePredicate<H> predicate) {
            this.availablePredicate = predicate;
            return self();
        }
        
        public Builder<H> onComplete(QuestConsumer<H> onComplete) {
            this.onComplete = onComplete;
            return self();
        }
        
        public Builder<H> markCompleteForHolder(boolean markCompleteForHolder) {
            this.markCompleteForHolder = markCompleteForHolder;
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
            
            return new Quest<>(holderType, id, name, description, requiredQuests, availablePredicate, onComplete, markCompleteForHolder, actions);
        }
        
        @Override
        public Builder<H> clone() {
            return new Builder<>(this);
        }
    }
}