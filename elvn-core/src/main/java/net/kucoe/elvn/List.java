package net.kucoe.elvn;

import java.util.ArrayList;

/**
 * List bean.
 * 
 * @author Vitaliy Basyuk
 */
public class List {
    
    /**
     * Not assigned list constant.
     */
    public static final String NOT_ASSIGNED = "Not assigned";
    
    private final String label;
    private final String color;
    private final java.util.List<Task> tasks = new ArrayList<Task>();
    
    /**
     * Constructs List.
     * 
     * @param label
     * @param color
     */
    public List(final String label, final String color) {
        this.color = color;
        this.label = label;
    }
    
    /**
     * Returns the label String.
     * 
     * @return the label String.
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * Returns the color String.
     * 
     * @return the color String.
     */
    public String getColor() {
        return color;
    }
    
    /**
     * Returns tasks.
     * 
     * @return list
     */
    public java.util.List<Task> getTasks() {
        return tasks;
    }
    
    @Override
    public int hashCode() {
        return getColor().hashCode();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append(":");
        sb.append(getLabel());
        sb.append("-");
        sb.append(getColor());
        return sb.toString();
    }
    
}
