package com.stardevllc.starquests.line.function;

import com.stardevllc.starquests.holder.QuestHolder;
import com.stardevllc.starquests.line.QuestLine;

@FunctionalInterface
public interface QuestLineConsumer<H extends QuestHolder<?>> {
    void apply(QuestLine<H> questLine, H holder);    
}