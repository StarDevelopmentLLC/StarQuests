package com.stardevllc.starquests.registry;

import com.stardevllc.starlib.injector.FieldInjector;
import com.stardevllc.starlib.objects.registry.Registry;
import com.stardevllc.starlib.objects.registry.RegistryObject;
import com.stardevllc.starquests.actions.QuestAction;
import com.stardevllc.starquests.holder.QuestHolder;

public class ActionRegistry extends Registry<String, QuestAction<?, ?>> {
    
    private FieldInjector injector;
    
    public ActionRegistry(FieldInjector injector) {
        super(null, string -> string.toLowerCase().replace(" ", "_"), QuestAction::getId, null, null);
        this.injector = injector;
    }
    
    @Override
    public RegistryObject<String, QuestAction<?, ?>> register(String key, QuestAction<?, ?> object) {
        return super.register(key, injector.inject(object));
    }
    
    public <T, H extends QuestHolder<?>> QuestAction<T, H> register(QuestAction.Builder<T, H> builder) {
        QuestAction<T, H> action = builder.build();
        register(action);
        return action;
    }
}
