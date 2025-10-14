package com.stardevllc.starquests.hooks;

import com.stardevllc.starevents.EventListener;
import com.stardevllc.starquests.events.ActionCompleteEvent;
import com.stardevllc.starquests.events.ActionUpdateEvent;
import org.bukkit.event.EventHandler;

public class StarEventsHook extends EventListener {
    @EventHandler
    public void onActionComplete(ActionCompleteEvent e) {
        EVENT_BUS.post(e);
    }
    
    @EventHandler
    public void onActionUpdate(ActionUpdateEvent e) {
        EVENT_BUS.post(e);
    }
}