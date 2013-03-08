package net.kucoe.elvn.sync;

import java.util.*;

import net.kucoe.elvn.*;
import net.kucoe.elvn.List;
import net.kucoe.elvn.lang.result.ELResult;

/**
 * Describes item change for next synchronization
 * 
 * @author Vitaliy Basyuk
 */
public class SyncEvent {
    
    enum EventType {
        
        Create('0'),
        
        Modify('1'),
        
        List('2'),
        
        Timer('3');
        
        protected char bit;
        protected static Map<Character, EventType> map = new HashMap<Character, EventType>();
        
        static {
            for (EventType item : values()) {
                map.put(item.bit, item);
            }
        }
        
        private EventType(char bit) {
            this.bit = bit;
        }
    }
    
    private final Object item;
    private final EventType type;
    private final ELResult command;
    private final Date date;
    
    /**
     * Constructs SyncEvent.
     * 
     * @param item
     * @param type
     */
    public SyncEvent(Object item, EventType type) {
        this(item, type, null);
    }
    
    /**
     * Constructs SyncEvent.
     * 
     * @param item
     * @param type
     * @param command
     */
    public SyncEvent(Object item, EventType type, ELResult command) {
        this(item, type, command, new Date());
    }
    
    /**
     * Constructs SyncEvent.
     * 
     * @param item
     * @param type
     * @param command
     * @param date
     */
    public SyncEvent(Object item, EventType type, ELResult command, Date date) {
        this.item = item;
        this.type = type;
        this.command = command;
        this.date = date;
    }
    
    /**
     * Returns the item Object.
     * 
     * @return the item Object.
     */
    public Object getItem() {
        return item;
    }
    
    @Override
    public int hashCode() {
        return getItem().hashCode();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type.bit);
        sb.append(ItemParser.toRaw(item));
        return sb.toString();
    }
    
    /**
     * Build event from raw representation
     * 
     * @param raw
     * @return string
     */
    public static SyncEvent fromRaw(String raw) {
        if (raw == null) {
            return null;
        }
        char bit = raw.charAt(0);
        EventType type = EventType.map.get(bit);
        Object item = new ItemParser(raw.substring(1));
        return new SyncEvent(item, type, null);
    }
    
    /**
     * Returns particular item id
     * 
     * @return string
     */
    public String getItemId() {
        if (item instanceof Note) {
            return String.valueOf(((Note) item).getId());
        }
        if (item instanceof List) {
            return ((List) item).getColor();
        }
        if (item instanceof TimerInfo) {
            return ItemParser.TIMER_ID;
        }
        return null;
    }
    
    /**
     * Returns whether this event is after argument. After means different type or later date.
     * 
     * @param event
     * @return boolean
     */
    public boolean after(SyncEvent event) {
        if (event == null) {
            return true;
        }
        if (!type.equals(event.type)) {
            return true;
        }
        return date.after(event.date);
    }
}
