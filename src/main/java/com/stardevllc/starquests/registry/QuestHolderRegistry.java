package com.stardevllc.starquests.registry;

import com.stardevllc.starlib.registry.StringRegistry;
import com.stardevllc.starquests.holder.QuestHolder;

public class QuestHolderRegistry extends StringRegistry<QuestHolder<?>> {
    public QuestHolderRegistry() {
        super(null, null, QuestHolder::getKey, null, null);
    }
}
