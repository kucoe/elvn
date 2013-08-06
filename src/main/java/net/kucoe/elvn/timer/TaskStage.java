package net.kucoe.elvn.timer;

import java.util.HashMap;
import java.util.Map;

/**
 * Stage enumeration
 * 
 * @author Vitaliy Basyuk
 */
public enum TaskStage {
    
    /**
     * Elvn stage
     */
    Elvn('0'),
    
    /**
     * Working stage
     */
    Work('1'),
    
    /**
     * Working second stage
     */
    Work2('2'),
    
    /**
     * Working third stage
     */
    Work3('3'),
    
    /**
     * Break stage
     */
    Break('5');
    
    protected final char bit;
    protected static Map<Character, TaskStage> map = new HashMap<Character, TaskStage>();
    
    static {
        for (TaskStage value : values()) {
            map.put(value.bit, value);
        }
    }
    
    /**
     * Returns stage by it's bit
     * 
     * @param bit
     * @return {@link TaskStage}
     */
    public static TaskStage stage(char bit) {
        return map.get(bit);
    }
    
    private TaskStage(char bit) {
        this.bit = bit;
    }
    
    /**
     * Returns the bit char.
     * 
     * @return the bit char.
     */
    public char getBit() {
        return bit;
    }
    
}
