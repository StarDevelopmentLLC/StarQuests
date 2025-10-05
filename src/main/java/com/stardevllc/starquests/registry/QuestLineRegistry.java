package com.stardevllc.starquests.registry;

import com.stardevllc.starlib.dependency.DependencyInjector;
import com.stardevllc.starlib.registry.RegistryObject;
import com.stardevllc.starlib.registry.StringRegistry;
import com.stardevllc.starquests.line.QuestLine;

public class QuestLineRegistry extends StringRegistry<QuestLine> {
    
    private DependencyInjector injector;
    
    public QuestLineRegistry(DependencyInjector injector) {
        super(null, string -> string.toLowerCase().replace(" ", "_"), QuestLine::getId, null, null);
        this.injector = injector;
    }
    
    @Override
    public RegistryObject<String, QuestLine> register(String key, QuestLine object) {
        return super.register(key, injector.inject(object));
    }
    
    public QuestLine register(QuestLine.Builder builder) {
        return this.register(builder.build()).getObject();
    }
}
