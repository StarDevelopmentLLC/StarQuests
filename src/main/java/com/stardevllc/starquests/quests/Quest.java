package com.stardevllc.starquests.quests;

import com.stardevllc.dependency.DependencyInjector;
import com.stardevllc.dependency.Inject;
import com.stardevllc.starquests.StarQuests;
import com.stardevllc.starquests.actions.QuestAction;
import com.stardevllc.starquests.quests.function.QuestConsumer;

import java.util.*;

public class Quest {
    @Inject
    protected StarQuests starQuests;
    
    protected String id;
    protected String name;
    protected List<String> description;
    protected List<String> prerequisiteQuests = new ArrayList<>();
    protected Map<String, QuestAction<?>> actions = new HashMap<>();
    protected QuestConsumer onComplete;
    
    protected DependencyInjector injector;
    
    public Quest(String id, String name, List<String> description, List<String> prerequisiteQuests, QuestConsumer onComplete) {
        this.id = id;
        this.name = name;
        this.description.addAll(description);
        this.prerequisiteQuests.addAll(prerequisiteQuests);
        this.onComplete = onComplete;
        this.injector = DependencyInjector.create();
        this.injector.setInstance(this);
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
}