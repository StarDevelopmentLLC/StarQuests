package com.stardevllc.starquests.actions.function;

import com.stardevllc.starquests.actions.QuestAction;
import com.stardevllc.starquests.actions.QuestActionData;
import com.stardevllc.starquests.holder.QuestHolder;

@FunctionalInterface
public interface QuestActionAvailablePredicate<T, H extends QuestHolder<?>> {
    Availability test(QuestAction<T, H> action, H holder, QuestActionData actionData);
    
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