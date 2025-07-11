package com.stardevllc.starquests;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class QuestActionData {
    protected final String actionId;
    protected final Map<String, Object> data = new HashMap<>();
    
    public QuestActionData(String actionId) {
        this.actionId = actionId;
    }
    
    public void setData(String key, Object data) {
        this.data.put(key, data);
    }
    
    public <T> void modifyData(String key, Function<T, T> function, T defaultValue) {
        try {
            Object o = this.data.get(key);
            if (o == null) {
                o = defaultValue;
            }
            this.data.put(key, function.apply((T) o));
        } catch (Throwable t) {}
    }
    
    public Object get(String key) {
        return this.data.get(key);
    }
}