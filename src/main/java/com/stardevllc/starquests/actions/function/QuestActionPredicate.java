package com.stardevllc.starquests.actions.function;

import com.stardevllc.starquests.actions.QuestAction;
import com.stardevllc.starquests.actions.QuestActionData;

@FunctionalInterface
public interface QuestActionPredicate<T> {
    boolean test(QuestAction<T> action, T triggerData, QuestActionData data);
}