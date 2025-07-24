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
        
        for (Quest quest : plugin.getQuests().values()) {
            if (plugin.isQuestComplete(player.getUniqueId(), quest)) {
                plugin.getColors().coloredLegacy(player, "&a" + quest.getName());
            } else if (plugin.isQuestAvailble(player.getUniqueId(), quest)) {
                plugin.getColors().coloredLegacy(player, "&e" + quest.getName());
                
                for (QuestAction<?> action : quest.getActions().values()) {
                    if (plugin.isActionComplete(player.getUniqueId(), action)) {
                        plugin.getColors().coloredLegacy(player, "    &a" + action.getName());
                    }else if (plugin.isActionAvailable(player.getUniqueId(), action)) {
                        plugin.getColors().coloredLegacy(player, "    &e" + action.getName());
                    } else {
                        plugin.getColors().coloredLegacy(player, "    &c" + action.getName());
                    }
                }
                
            } else {
                plugin.getColors().coloredLegacy(player, "&c" + quest.getName());
            }
        }
        
        return true;
    }
}
