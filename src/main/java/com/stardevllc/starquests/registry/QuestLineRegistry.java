package com.stardevllc.starquests.registry;

import com.stardevllc.starlib.injector.FieldInjector;
import com.stardevllc.starlib.objects.registry.Registry;
import com.stardevllc.starlib.objects.registry.RegistryObject;
import com.stardevllc.starquests.holder.QuestHolder;
import com.stardevllc.starquests.line.QuestLine;

import java.util.ArrayList;
import java.util.List;

public class QuestLineRegistry extends Registry<String, QuestLine<?>> {
    
    private FieldInjector injector;
    
    public QuestLineRegistry(FieldInjector injector) {
        super(null, string -> string.toLowerCase().replace(" ", "_"), QuestLine::getId, null, null);
        this.injector = injector;
    }
    
    public <H extends QuestHolder<?>> List<QuestLine<H>> getObjects(Class<H> holderType) {
        List<QuestLine<H>> quests = new ArrayList<>();
        for (QuestLine<?> questLine : this.values()) {
            if (questLine.getHolderType().isAssignableFrom(holderType)) {
                quests.add((QuestLine<H>) questLine);
            }
        }
        
        return quests;
    }
    
    @Override
    public RegistryObject<String, QuestLine<?>> register(String key, QuestLine<?> object) {
        return super.register(key, injector.inject(object));
    }
    
    public <H extends QuestHolder<?>> QuestLine<H> register(QuestLine.Builder<H> builder) {
        QuestLine<H> questLine = builder.build();
        register(questLine);
        return questLine;
    }
}
