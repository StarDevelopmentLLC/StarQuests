package com.stardevllc.starquests.holder;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class QuestPlayer extends QuestHolder<UUID> {
    public QuestPlayer(UUID uuid) {
        super(uuid.toString(), uuid);
    }
    
    public QuestPlayer(Player player) {
        this(player.getUniqueId());
    }
    
    @Override
    public void sendMessage(String message) {
        getPlayer().ifPresent(player -> player.sendMessage(color(message)));
    }
    
    public UUID getUuid() {
        return getValue();
    }
    
    @Override
    public Optional<Player> getPlayer() {
        return Optional.ofNullable(Bukkit.getPlayer(getUuid()));
    }
}
