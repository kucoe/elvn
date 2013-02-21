package net.kucoe.elvn.lang.result;

import net.kucoe.elvn.util.Config;
import net.kucoe.elvn.util.Display;

/**
 * Marker for group collecting
 * 
 * @author Vitaliy Basyuk
 */
public class TaskGroup extends BaseResult {
    
    /**
     * Positions
     */
    public final int[] positions;
    
    /**
     * Constructs TaskGroup.
     * 
     * @param positions
     */
    public TaskGroup(final int[] positions) {
        this.positions = positions;
    }
    
    @Override
    public String execute(final Display display, final Config config) throws Exception {
        return null;
    }
    
}
