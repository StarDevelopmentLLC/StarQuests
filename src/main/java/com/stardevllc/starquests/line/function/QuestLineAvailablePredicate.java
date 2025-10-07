package com.stardevllc.starquests.line.function;

import com.stardevllc.starquests.holder.QuestHolder;
import com.stardevllc.starquests.line.QuestLine;

@FunctionalInterface
public interface QuestLineAvailablePredicate<H extends QuestHolder<?>> {
    Availability test(QuestLine<H> questLine, H holder);
    
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