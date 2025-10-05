package com.stardevllc.starquests.line.function;

import com.stardevllc.starquests.line.QuestLine;
import org.bukkit.entity.Player;

@FunctionalInterface
public interface QuestLineConsumer {
    void apply(QuestLine questLine, Player player);    
}