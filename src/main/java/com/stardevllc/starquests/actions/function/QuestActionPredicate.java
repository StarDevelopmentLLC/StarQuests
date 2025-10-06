package com.stardevllc.starquests.actions.function;

import com.stardevllc.starquests.actions.QuestAction;
import com.stardevllc.starquests.actions.QuestActionData;
import com.stardevllc.starquests.holder.QuestHolder;

@FunctionalInterface
public interface QuestActionPredicate<T, H extends QuestHolder<?>> {
    Status test(QuestAction<T, H> action, T triggerData, H holder, QuestActionData actionData);
    
    enum Status {
        COMPLETE, IN_PROGRESS, FALSE, WRONG_HOLDER_TYPE, ERROR
    }
}