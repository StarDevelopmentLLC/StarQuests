package com.stardevllc.starquests.quests.function;

import com.stardevllc.starquests.quests.Quest;
import org.bukkit.entity.Player;

@FunctionalInterface
public interface QuestConsumer {
    void apply(Quest quest, Player player);    
}