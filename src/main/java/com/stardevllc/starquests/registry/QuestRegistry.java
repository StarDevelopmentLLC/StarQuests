package com.stardevllc.starquests.registry;

import com.stardevllc.starlib.injector.FieldInjector;
import com.stardevllc.starlib.objects.registry.Registry;
import com.stardevllc.starlib.objects.registry.RegistryObject;
import com.stardevllc.starquests.holder.QuestHolder;
import com.stardevllc.starquests.quests.Quest;

import java.util.ArrayList;
import java.util.List;

public class QuestRegistry extends Registry<String, Quest<?>> {
    
    private FieldInjector injector;
    
    public QuestRegistry(FieldInjector injector) {
        super(null, string -> string.toLowerCase().replace(" ", "_"), Quest::getId, null, null);
        this.injector = injector;
    }
    
    public <H extends QuestHolder<?>> List<Quest<H>> getObjects(Class<H> holderType) {
        List<Quest<H>> quests = new ArrayList<>();
        for (Quest<?> quest : this.values()) {
            if (quest.getHolderType().isAssignableFrom(holderType)) {
                quests.add((Quest<H>) quest);
            }
        }
        
        return quests;
    }
    
    @Override
    public RegistryObject<String, Quest<?>> register(String key, Quest<?> object) {
        return super.register(key, injector.inject(object));
    }
    
    public <H extends QuestHolder<?>> Quest<H> register(Quest.Builder<H> builder) {
        Quest<H> quest = builder.build();
        register(quest);
        return quest;
    }
}
