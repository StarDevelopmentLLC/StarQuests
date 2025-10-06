package com.stardevllc.starquests.events;

import com.stardevllc.starquests.actions.QuestAction;
import com.stardevllc.starquests.actions.QuestActionData;

public class ActionUpdateEvent extends QuestEvent {
    private QuestAction<?, ?> action;
    private QuestActionData actionData;
    
    public ActionUpdateEvent(QuestAction<?, ?> action, QuestActionData actionData) {
        this.action = action;
        this.actionData = actionData;
    }
    
    public QuestAction<?, ?> getAction() {
        return action;
    }
    
    public QuestActionData getActionData() {
        return actionData;
    }
}