package com.stardevllc.starquests.quests.function;

import com.stardevllc.starquests.holder.QuestHolder;
import com.stardevllc.starquests.quests.Quest;

@FunctionalInterface
public interface QuestConsumer<H extends QuestHolder<?>> {
    void apply(Quest<H> quest, H holder);    
}