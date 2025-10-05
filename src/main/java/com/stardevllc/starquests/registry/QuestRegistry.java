package com.stardevllc.starquests.registry;

import com.stardevllc.starlib.dependency.DependencyInjector;
import com.stardevllc.starlib.registry.RegistryObject;
import com.stardevllc.starlib.registry.StringRegistry;
import com.stardevllc.starquests.quests.Quest;

public class QuestRegistry extends StringRegistry<Quest> {
    
    private DependencyInjector injector;
    
    public QuestRegistry(DependencyInjector injector) {
        super(null, string -> string.toLowerCase().replace(" ", "_"), Quest::getId, null, null);
        this.injector = injector;
    }
    
    @Override
    public RegistryObject<String, Quest> register(String key, Quest object) {
        return super.register(key, injector.inject(object));
    }
    
    public Quest register(Quest.Builder builder) {
        return this.register(builder.build()).getObject();
    }
}
