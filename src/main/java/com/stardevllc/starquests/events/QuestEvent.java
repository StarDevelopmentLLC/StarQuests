package com.stardevllc.starquests.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class QuestEvent extends Event {
    private static HandlerList handlers = new HandlerList();
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}