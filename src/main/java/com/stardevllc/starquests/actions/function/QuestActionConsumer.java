package com.stardevllc.starquests.actions.function;

import com.stardevllc.starquests.actions.QuestAction;
import com.stardevllc.starquests.actions.QuestActionData;

@FunctionalInterface
public interface QuestActionConsumer<T> {
    void apply(QuestAction<T> action, T triggerData, QuestActionData actionData);
}