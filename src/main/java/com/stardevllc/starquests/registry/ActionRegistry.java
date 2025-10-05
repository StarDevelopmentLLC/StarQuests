package com.stardevllc.starquests.registry;

import com.stardevllc.starlib.dependency.DependencyInjector;
import com.stardevllc.starlib.registry.RegistryObject;
import com.stardevllc.starlib.registry.StringRegistry;
import com.stardevllc.starquests.actions.QuestAction;

public class ActionRegistry extends StringRegistry<QuestAction<?>> {
    
    private DependencyInjector injector;
    
    public ActionRegistry(DependencyInjector injector) {
        super(null, string -> string.toLowerCase().replace(" ", "_"), QuestAction::getId, null, null);
        this.injector = injector;
    }
    
    @Override
    public RegistryObject<String, QuestAction<?>> register(String key, QuestAction<?> object) {
        return super.register(key, injector.inject(object));
    }
    
    public QuestAction<?> register(QuestAction.Builder<?> builder) {
        return register(builder.build()).getObject();
    }
}
