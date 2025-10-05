package com.stardevllc.starquests.cmds;

import com.stardevllc.starlib.dependency.Inject;
import com.stardevllc.starquests.StarQuests;
import com.stardevllc.starquests.actions.QuestAction;
import com.stardevllc.starquests.quests.Quest;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class QuestCmd implements CommandExecutor {
    
    @Inject
    private StarQuests plugin;
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getColors().coloredLegacy(sender, "&cOnly players can use that command");
            return true;
        }
        
        boolean hasQuest = false;
        for (Quest quest : plugin.getQuestRegistry()) {
            if (plugin.isQuestAvailble(player.getUniqueId(), quest)) {
                hasQuest = true;
                plugin.getColors().coloredLegacy(player, "&e" + quest.getName());
                
                for (QuestAction<?> action : quest.getActions().values()) {
                    if (plugin.isActionAvailable(player.getUniqueId(), action)) {
                        plugin.getColors().coloredLegacy(player, "    &e" + action.getName());
                    }
                }
            }
        }
        
        if (!hasQuest) {
            plugin.getColors().coloredLegacy(player, "&cNo quests available.");
        }
        
        return true;
    }
}
