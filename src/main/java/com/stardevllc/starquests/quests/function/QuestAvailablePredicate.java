package com.stardevllc.starquests.quests.function;

import com.stardevllc.starquests.holder.QuestHolder;
import com.stardevllc.starquests.quests.Quest;

@FunctionalInterface
public interface QuestAvailablePredicate<H extends QuestHolder<?>> {
    Availability test(Quest<H> quest, H holder);
    
    enum Availability {
        LOCKED, AVAILABLE(true), COMPLETE, WRONG_HOLDER_TYPE, ERROR; 
        
        private final boolean boolValue;
        
        Availability() {
            this(false);
        }
        
        Availability(boolean boolValue) {
            this.boolValue = boolValue;
        }
        
        public boolean asBoolean() {
            return boolValue;
        }
    }
}