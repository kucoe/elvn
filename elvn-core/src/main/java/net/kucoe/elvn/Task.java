package net.kucoe.elvn;

import java.util.Date;

/**
 * Main task bean
 * 
 * @author Vitaliy Basyuk
 */
public class Task extends Note {
    
    private final Date completedOn;
    private final ListColor list;
    private final Boolean planned;
    
    /**
     * Constructs Task.
     * 
     * @param id
     * @param list
     * @param text
     * @param planned
     * @param completedOn
     */
    public Task(final Long id, final String list, final String text, final boolean planned, final Date completedOn) {
        super(id, text);
        this.completedOn = completedOn;
        this.list = list == null ? ListColor.Blue : ListColor.color(list);
        this.planned = planned;
    }
    
    /**
     * Returns the list String.
     * 
     * @return the list String.
     */
    public String getList() {
        return list.toString();
    }
    
    /**
     * Returns the planned Boolean.
     * 
     * @return the planned Boolean.
     */
    public boolean isPlanned() {
        return planned;
    }
    
    /**
     * Returns the completedOn Date.
     * 
     * @return the completedOn Date.
     */
    public Date getCompletedOn() {
        return completedOn;
    }
    
    @Override
    public int compareTo(final Note o) {
        if (!(o instanceof Task)) {
            throw new IllegalArgumentException("Not a task");
        }
        boolean thisVal = isPlanned();
        boolean anotherVal = ((Task) o).isPlanned();
        return (thisVal == anotherVal) ? super.compareTo(o) : (isPlanned() ? -1 : 1);
    }
    
}
