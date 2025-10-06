package com.stardevllc.starquests.actions.function;

import com.stardevllc.starquests.actions.QuestAction;
import com.stardevllc.starquests.actions.QuestActionData;
import com.stardevllc.starquests.holder.QuestHolder;

@FunctionalInterface
public interface QuestActionConsumer<T> {
    void apply(QuestAction<T> action, T triggerData, QuestHolder<?> holder, QuestActionData actionData);
}