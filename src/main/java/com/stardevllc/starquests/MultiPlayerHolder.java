package com.stardevllc.starquests;

import com.stardevllc.starquests.holder.QuestHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class MultiPlayerHolder extends QuestHolder<List<UUID>> {
    public MultiPlayerHolder(String key, List<UUID> value) {
        super(key, new ArrayList<>(value));
    }
    
    @Override
    public void sendMessage(String message) {
        String msg = color(message);
        for (UUID uuid : getValue()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendMessage(msg);
            }
        }
    }
}
