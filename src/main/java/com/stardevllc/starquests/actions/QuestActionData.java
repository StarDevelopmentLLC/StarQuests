package com.stardevllc.starquests.actions;

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
    
    public int getAsInt(String key) {
        if (!this.data.containsKey(key)) {
            return 0;
        }
        
        Object o = this.data.get(key);
        if (o instanceof Number number) {
            return number.intValue();
        } else if (o instanceof String str) {
            try {
                return Integer.parseInt(str);
            } catch (Throwable t) {
                return 0;
            }
        }
        
        return 0;
    }
    
    public double getAsDouble(String key) {
        if (!this.data.containsKey(key)) {
            return 0.0;
        }
        
        Object o = this.data.get(key);
        if (o instanceof Number number) {
            return number.doubleValue();
        } else if (o instanceof String str) {
            try {
                return Double.parseDouble(str);
            } catch (Throwable t) {
                return 0;
            }
        }
        
        return 0;
    }
    
    public String getAsString(String key) {
        if (!this.data.containsKey(key)) {
            return "";
        }
        
        Object o = this.data.get(key);
        if (o instanceof String str) {
            return str;
        }
        
        return o.toString();
    }
    
    public boolean getAsBoolean(String key) {
        if (!this.data.containsKey(key)) {
            return false;
        }
        
        Object o = this.data.get(key);
        if (o instanceof Boolean bool) {
            return bool;
        } else if (o instanceof String str) {
            try {
                return Boolean.parseBoolean(str);
            } catch (Exception e) {
                return false;
            }
        }
        
        return false;
    }
}