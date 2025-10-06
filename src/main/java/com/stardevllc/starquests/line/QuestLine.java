package com.stardevllc.starquests.line;

import com.stardevllc.starcore.api.StarColors;
import com.stardevllc.starlib.builder.IBuilder;
import com.stardevllc.starlib.dependency.DependencyInjector;
import com.stardevllc.starlib.dependency.Inject;
import com.stardevllc.starlib.helper.StringHelper;
import com.stardevllc.starquests.StarQuests;
import com.stardevllc.starquests.holder.QuestHolder;
import com.stardevllc.starquests.line.function.QuestLineConsumer;
import com.stardevllc.starquests.quests.Quest;
import com.stardevllc.starquests.registry.QuestLineRegistry;
import com.stardevllc.starquests.registry.QuestRegistry;
import org.bukkit.ChatColor;

import java.util.*;

public class QuestLine<H extends QuestHolder<?>> {
    private static QuestRegistry primaryQuestRegistry;
    
    public static void setPrimaryQuestRegistry(QuestRegistry registry) {
        if (primaryQuestRegistry == null) {
            primaryQuestRegistry = registry;
        }
    }
    
    @Inject
    protected StarQuests starQuests;
    
    @Inject
    protected QuestLineRegistry questLineRegistry;
    
    protected final Class<H> holderType;
    protected String id;
    protected String name;
    protected List<String> description = new LinkedList<>();
    protected Set<String> requiredLines = new HashSet<>();
    protected QuestRegistry quests;
    protected QuestLineConsumer<H> onComplete;
    
    protected DependencyInjector injector;
    
    public QuestLine(Class<H> holderType, String id, String name, List<String> description, Set<String> requiredLines, QuestLineConsumer<H> onComplete) {
        this(holderType, id, name, description, requiredLines, onComplete, null);
    }
    
    public QuestLine(Class<H> holderType, String id, String name, List<String> description, Set<String> requiredLines, QuestLineConsumer<H> onComplete, Map<String, Quest<H>> quests) {
        this.holderType = holderType;
        this.id = id;
        this.name = name;
        this.description.addAll(description);
        this.requiredLines.addAll(requiredLines);
        this.onComplete = onComplete;
        this.injector = DependencyInjector.create();
        this.quests = new QuestRegistry(this.injector);
        this.injector.setInstance(this);
        this.injector.setInstance(this.quests);
        
        if (quests != null) {
            quests.forEach((qid, quest) -> {
                this.quests.register(quest);
                primaryQuestRegistry.register(quest);
            });
        }
    }
    
    public Class<H> getHolderType() {
        return holderType;
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
    
    public Set<String> getRequiredLines() {
        return requiredLines;
    }
    
    public QuestRegistry getQuests() {
        return quests;
    }
    
    public QuestLineConsumer<H> getOnComplete() {
        return onComplete;
    }
    
    public void handleOnComplete(H holder) {
        if (this.onComplete != null) {
            this.onComplete.apply(this, holder);
        }
    }
    
    public boolean isAvailable(QuestHolder<?> holder) {
        if (holder.isQuestLineComplete(this)) {
            return false;
        }
        
        for (String rq : getRequiredLines()) {
            QuestLine<?> requiredQuestLine = questLineRegistry.get(rq);
            if (!holder.isQuestLineComplete(requiredQuestLine)) {
                return false;
            }
        }
        
        return true;
    }
    
    public static <H extends QuestHolder<?>> Builder<H> builder(Class<H> holderType) {
        return new Builder<>(holderType);
    }
    
    public static class Builder<H extends QuestHolder<?>> implements IBuilder<QuestLine<H>, Builder<H>> {
        private final Class<H> holderType;
        private String id;
        private String name;
        private List<String> description = new LinkedList<>();
        private Set<String> requiredLines = new HashSet<>();
        private Map<String, Quest<H>> quests = new HashMap<>();
        private QuestLineConsumer<H> onComplete;
        
        public Builder(Class<H> holderType) {
            this.holderType = holderType;
        }
        
        public Builder(Builder<H> builder) {
            this(builder.holderType);
            this.id = builder.id;
            this.name = builder.name;
            this.description.addAll(builder.description);
            this.requiredLines.addAll(builder.requiredLines);
            this.quests.putAll(builder.quests);
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
        
        public Builder<H> requiredLines(String... requiredLines) {
            this.requiredLines.clear();
            this.requiredLines.addAll(List.of(requiredLines));
            return self();
        }
        
        @SafeVarargs
        public final Builder<H> addQuest(Quest<H> firstQuest, Quest<H>... quests) {
            this.quests.put(firstQuest.getId(), firstQuest);
            if (quests != null) {
                for (Quest<H> quest : quests) {
                    this.quests.put(quest.getId(), quest);
                }
            }
            
            return self();
        }
        
        @SafeVarargs
        public final Builder<H> addQuest(Quest.Builder<H> questBuilder, Quest.Builder<H>... questBuilders) {
            Quest<H> firstQuest = questBuilder.build();
            this.quests.put(firstQuest.getId(), firstQuest);
            if (questBuilders != null) {
                for (Quest.Builder<H> builder : questBuilders) {
                    Quest<H> quest = builder.build();
                    this.quests.put(quest.getId(), quest);
                }
            }
            
            return self();
        }
        
        public Builder<H> onComplete(QuestLineConsumer<H> onComplete) {
            this.onComplete = onComplete;
            return self();
        }
        
        @Override
        public QuestLine<H> build() {
            if ((id == null || id.isBlank()) && name != null && !name.isBlank()) {
                id = ChatColor.stripColor(StarColors.color(name.toLowerCase().replace(" ", "_")));
            }
            
            if ((name == null || name.isBlank()) && id != null && !id.isBlank()) {
                name = StringHelper.titlize(this.id);
            }
            
            return new QuestLine<>(this.holderType, this.id, this.name, this.description, this.requiredLines, this.onComplete, this.quests);
        }
        
        @Override
        public Builder<H> clone() {
            return new Builder<>(this);
        }
    }
}