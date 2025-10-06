package com.stardevllc.starquests.actions.function;

import com.stardevllc.starquests.actions.QuestAction;
import com.stardevllc.starquests.actions.QuestActionData;
import com.stardevllc.starquests.holder.QuestHolder;

@FunctionalInterface
public interface QuestActionConsumer<T, H extends QuestHolder<?>> {
    void apply(QuestAction<T, H> action, T triggerData, H holder, QuestActionData actionData);
}