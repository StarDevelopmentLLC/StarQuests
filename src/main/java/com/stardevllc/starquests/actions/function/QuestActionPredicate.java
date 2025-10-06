package com.stardevllc.starquests.actions.function;

import com.stardevllc.starquests.actions.QuestAction;
import com.stardevllc.starquests.actions.QuestActionData;
import com.stardevllc.starquests.holder.QuestHolder;

@FunctionalInterface
public interface QuestActionPredicate<T> {
    Status test(QuestAction<T> action, T triggerData, QuestHolder<?> holder, QuestActionData actionData);
    
    enum Status {
        COMPLETE, IN_PROGRESS, FALSE, ERROR
    }
}