package com.stardevllc.starquests.actions;

@FunctionalInterface
public interface QuestActionPredicate<T> {
    boolean test(QuestAction<T> action, T triggerData, QuestActionData data);
}