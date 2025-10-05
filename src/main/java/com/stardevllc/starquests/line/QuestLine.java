package com.stardevllc.starquests.line;

import com.stardevllc.starcore.api.StarColors;
import com.stardevllc.starlib.builder.IBuilder;
import com.stardevllc.starlib.dependency.DependencyInjector;
import com.stardevllc.starlib.dependency.Inject;
import com.stardevllc.starlib.helper.StringHelper;
import com.stardevllc.starquests.QuestPlayer;
import com.stardevllc.starquests.StarQuests;
import com.stardevllc.starquests.line.function.QuestLineConsumer;
import com.stardevllc.starquests.quests.Quest;
import com.stardevllc.starquests.registry.QuestLineRegistry;
import com.stardevllc.starquests.registry.QuestRegistry;
import org.bukkit.ChatColor;

import java.util.*;

public class QuestLine {
    @Inject
    protected StarQuests starQuests;
    
    @Inject
    protected QuestLineRegistry questLineRegistry;
    
    @Inject
    protected QuestRegistry mainQuestRegistry;
    
    protected String id;
    protected String name;
    protected List<String> description = new LinkedList<>();
    protected Set<String> requiredLines = new HashSet<>();
    protected QuestRegistry quests;
    protected QuestLineConsumer onComplete;
    
    protected DependencyInjector injector;
    
    public QuestLine(String id, String name, List<String> description, Set<String> requiredLines, QuestLineConsumer onComplete) {
        this(id, name, description, requiredLines, onComplete, null);
    }
    
    public QuestLine(String id, String name, List<String> description, Set<String> requiredLines, QuestLineConsumer onComplete, Map<String, Quest> quests) {
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
            quests.forEach((qid, quest) -> this.quests.register(quest));
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
    
    public Set<String> getRequiredLines() {
        return requiredLines;
    }
    
    public QuestRegistry getQuests() {
        return quests;
    }
    
    public QuestLineConsumer getOnComplete() {
        return onComplete;
    }
    
    public boolean isAvailable(QuestPlayer player) {
        if (player.isQuestLineComplete(this)) {
            return false;
        }
        
        for (String rq : getRequiredLines()) {
            QuestLine requiredQuestLine = questLineRegistry.get(rq);
            if (!player.isQuestLineComplete(requiredQuestLine)) {
                return false;
            }
        }
        
        return true;
    }
    
    public static class Builder implements IBuilder<QuestLine, Builder> {
        private String id;
        private String name;
        private List<String> description = new LinkedList<>();
        private Set<String> requiredLines = new HashSet<>();
        private Map<String, Quest> quests = new HashMap<>();
        private QuestLineConsumer onComplete;
        
        public Builder() {}
        
        public Builder(Builder builder) {
            this.id = builder.id;
            this.name = builder.name;
            this.description.addAll(builder.description);
            this.requiredLines.addAll(builder.requiredLines);
            this.quests.putAll(builder.quests);
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
        
        public Builder requiredQuests(String... requiredLines) {
            this.requiredLines.clear();
            this.requiredLines.addAll(List.of(requiredLines));
            return self();
        }
        
        public Builder addQuest(Quest firstQuest, Quest... quests) {
            this.quests.put(firstQuest.getId(), firstQuest);
            if (quests != null) {
                for (Quest quest : quests) {
                    this.quests.put(quest.getId(), quest);
                }
            }
            
            return self();
        }
        
        public Builder addQuest(Quest.Builder questBuilder, Quest.Builder... questBuilders) {
            Quest firstQuest = questBuilder.build();
            this.quests.put(firstQuest.getId(), firstQuest);
            if (questBuilders != null) {
                for (Quest.Builder builder : questBuilders) {
                    Quest quest = builder.build();
                    this.quests.put(quest.getId(), quest);
                }
            }
            
            return self();
        }
        
        public Builder onComplete(QuestLineConsumer onComplete) {
            this.onComplete = onComplete;
            return self();
        }
        
        @Override
        public QuestLine build() {
            if ((id == null || id.isBlank()) && name != null && !name.isBlank()) {
                id = ChatColor.stripColor(StarColors.color(name.toLowerCase().replace(" ", "_")));
            }
            
            if ((name == null || name.isBlank()) && id != null && !id.isBlank()) {
                name = StringHelper.titlize(this.id);
            }
            
            return new QuestLine(this.id, this.name, this.description, this.requiredLines, this.onComplete, this.quests);
        }
        
        @Override
        public Builder clone() {
            return new Builder(this);
        }
    }
}