package com.stardevllc.starquests;

import com.stardevllc.starquests.actions.QuestAction;
import com.stardevllc.starquests.actions.QuestActionData;
import com.stardevllc.starquests.line.QuestLine;
import com.stardevllc.starquests.quests.Quest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class QuestPlayer {
    private UUID uuid;
    
    private Map<String, QuestActionData> actionData = new HashMap<>();
    private Set<String> completedActions = new HashSet<>();
    private Set<String> completedQuests = new HashSet<>();
    private Set<String> completedQuestLines = new HashSet<>();
    
    public QuestPlayer(UUID uuid) {
        this.uuid = uuid;
    }
    
    public QuestPlayer(Player player) {
        this.uuid = player.getUniqueId();
    }
    
    public UUID getUuid() {
        return uuid;
    }
    
    public Optional<Player> getPlayer() {
        return Optional.ofNullable(Bukkit.getPlayer(uuid));
    }
    
    public Map<String, QuestActionData> getActionData() {
        return actionData;
    }
    
    public QuestActionData getData(QuestAction<?> action) {
        return this.actionData.computeIfAbsent(action.getId(), QuestActionData::new);
    }
    
    public Set<String> getCompletedActions() {
        return completedActions;
    }
    
    public boolean isActionComplete(QuestAction<?> questAction) {
        return this.completedActions.contains(questAction.getId());
    }
    
    public void completeAction(QuestAction<?> questAction) {
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
