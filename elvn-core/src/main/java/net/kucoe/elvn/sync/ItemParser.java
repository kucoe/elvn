package net.kucoe.elvn.sync;

import java.util.*;

import net.kucoe.elvn.*;
import net.kucoe.elvn.List;
import net.kucoe.elvn.timer.TaskStage;

/**
 * Parse item line to item;
 * 
 * @author Vitaliy Basyuk
 */
public class ItemParser {
    
    private static final int ID_SIZE = 13;
    
    /**
     * Timer file name id
     */
    public static final String TIMER_ID = "timer";
    
    enum Item {
        Note('0'),
        
        Task('1'),
        
        List('2'),
        
        Timer('3');
        
        protected final char bit;
        protected static Map<Character, Item> map = new HashMap<Character, Item>();
        
        static {
            for (Item item : values()) {
                map.put(item.bit, item);
            }
        }
        
        private Item(char bit) {
            this.bit = bit;
        }
    }
    
    private final char[] line;
    private final Object item;
    
    /**
     * Constructs ItemParser.
     * 
     * @param line
     */
    public ItemParser(String line) {
        this.line = line.toCharArray();
        this.item = parse();
    }
    
    /**
     * Returns the item Object.
     * 
     * @return the item Object.
     */
    public Object getItem() {
        return item;
    }
    
    /**
     * Returns raw representation of item
     * 
     * @param item
     * @return string
     */
    public static String toRaw(Object item) {
        StringBuilder sb = new StringBuilder();
        if (item instanceof Task) {
            sb.append(Item.Task.bit);
            sb.append(((Note) item).getId());
            sb.append(((Task) item).getList().charAt(0));
            sb.append(((Task) item).isPlanned() ? '+' : '-');
            Date completeOn = ((Task) item).getCompletedOn();
            if (completeOn == null) {
                sb.append('0');
            } else {
                sb.append(completeOn.getTime());
            }
            sb.append(((Note) item).getText());
        } else if (item instanceof Note) {
            sb.append(Item.Note.bit);
            sb.append(((Note) item).getId());
            sb.append(((Note) item).getText());
        } else if (item instanceof List) {
            sb.append(Item.List.bit);
            sb.append(((List) item).getColor().charAt(0));
            sb.append(((List) item).getLabel());
        } else if (item instanceof TimerInfo) {
            sb.append(Item.Timer.bit);
            TaskStage stage = ((TimerInfo) item).getStage();
            if (stage == null) {
                sb.append('-');
            } else {
                sb.append(stage.getBit());
                int minutes = ((TimerInfo) item).getMinutes();
                if (minutes < 10) {
                    sb.append('0');
                }
                sb.append(minutes);
                sb.append(((TimerInfo) item).getRunId());
            }
        }
        return sb.toString();
    }
    
    private Object parse() {
        Item item = Item.map.get(line[0]);
        String id, text;
        int read = 1;
        int length = line.length;
        switch (item) {
            case Note:
                id = new String(line, read, ID_SIZE);
                read += ID_SIZE;
                text = new String(line, read, length - read);
                return new Note(toLong(id), text);
            case Task:
                id = new String(line, read, ID_SIZE);
                read += ID_SIZE;
                String list = new String(line, read, 1);
                read++;
                char planned = line[read];
                read++;
                boolean isCompleted = '0' != line[read];
                Date completedOn = null;
                if (isCompleted) {
                    completedOn = new Date(toLong(new String(line, read, ID_SIZE)));
                    read += ID_SIZE;
                } else {
                    read++;
                }
                text = new String(line, read, length - read);
                return new Task(toLong(id), list, text, '+' == planned, completedOn);
            case List:
                id = new String(line, read, 1);
                read++;
                text = new String(line, read, length - read);
                ListColor color = ListColor.color(id);
                if (color != null && !ListColor.isSystemColor(color)) {
                    return new List(color, text);
                }
                return null;
            case Timer:
                TaskStage taskStage = TaskStage.stage(line[read]);
                if (taskStage == null) {
                    return new TimerInfo(null, taskStage, 0);
                }
                read++;
                String time = new String(line, read, 2);
                read += 2;
                id = new String(line, read, ID_SIZE);
                return new TimerInfo(toLong(id), taskStage, (int) toLong(time));
        }
        return null;
    }
    
    /**
     * Returns file name according to parsed item
     * 
     * @return string
     */
    public String getFileName() {
        if (item instanceof Note) {
            return String.valueOf(((Note) item).getId());
        }
        if (item instanceof List) {
            return ((List) item).getColor();
        }
        if (item instanceof TimerInfo) {
            return TIMER_ID;
        }
        return null;
    }
    
    /**
     * Returns file body according to parsed item
     * 
     * @return string
     */
    public String getFileBody() {
        if (item instanceof Note) {
            int offset = ID_SIZE + 1;
            return new String(line, offset, line.length - ID_SIZE + 1);
        }
        if (item instanceof List) {
            int offset = 2;
            return new String(line, offset, line.length - offset);
        }
        if (item instanceof TimerInfo) {
            int offset = 1;
            return new String(line, offset, line.length - offset);
        }
        return null;
    }
    
    private long toLong(String id) {
        long l = 0;
        try {
            l = Long.valueOf(id);
        } catch (NumberFormatException e) {
            // ignore
        }
        return l;
    }
}
