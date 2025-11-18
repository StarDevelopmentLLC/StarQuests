package com.stardevllc.starquests.registry;

import com.stardevllc.starlib.objects.registry.Registry;
import com.stardevllc.starquests.holder.QuestHolder;

public class QuestHolderRegistry extends Registry<String, QuestHolder<?>> {
    public QuestHolderRegistry() {
        super(null, null, QuestHolder::getKey, null, null);
    }
}
