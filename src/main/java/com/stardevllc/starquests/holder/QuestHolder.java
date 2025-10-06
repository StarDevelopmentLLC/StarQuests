package com.stardevllc.starquests.holder;

import com.stardevllc.starquests.actions.QuestAction;
import com.stardevllc.starquests.actions.QuestActionData;
import com.stardevllc.starquests.line.QuestLine;
import com.stardevllc.starquests.quests.Quest;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Function;

public class QuestHolder<T> {
    
    private static Function<String, String> colorFunction;
    public static void setColorFunction(Function<String, String> function) {
        if (colorFunction == null) {
            colorFunction = function;
        }
    }
    
    protected static String color(String text) {
        if (colorFunction != null) {
            return colorFunction.apply(text);
        }
        
        return text;
    }
    
    protected String key;
    protected Map<String, QuestActionData> actionData = new HashMap<>();
    protected Set<String> completedActions = new HashSet<>();
    protected Set<String> completedQuests = new HashSet<>();
    protected Set<String> completedQuestLines = new HashSet<>();
    
    protected T value;
    
    public QuestHolder(String key, T value) {
        this.key = key;
        this.value = value;
    }
    
    public void sendMessage(String message) {
        
    }
    
    public Optional<Player> getPlayer() {
        return Optional.empty();
    }
    
    public String getKey() {
        return key;
    }
    
    public T getValue() {
        return value;
    }
    
    public Map<String, QuestActionData> getActionData() {
        return actionData;
    }
    
    public QuestActionData getData(QuestAction<?, ?> action) {
        return this.actionData.computeIfAbsent(action.getId(), QuestActionData::new);
    }
    
    public Set<String> getCompletedActions() {
        return completedActions;
    }
    
    public boolean isActionComplete(QuestAction<?, ?> questAction) {
        return this.completedActions.contains(questAction.getId());
    }
    
    public void completeAction(QuestAction<?, ?> questAction) {
        this.completedActions.add(questAction.getId());
    }
    
    public Set<String> getCompletedQuests() {
        return completedQuests;
    }
    
    public boolean isQuestComplete(Quest quest) {
        return this.completedQuests.contains(quest.getId());
    }
    
    public void completeQuest(Quest quest) {
        this.completedQuests.add(quest.getId());
    }
    
    public Set<String> getCompletedQuestLines() {
        return completedQuestLines;
    }
    
    public boolean isQuestLineComplete(QuestLine questLine) {
        return this.completedQuestLines.contains(questLine.getId());
    }
    
    public void completeQuestLine(QuestLine questLine) {
        this.completedQuestLines.add(questLine.getId());
    }
}