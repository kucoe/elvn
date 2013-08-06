package net.kucoe.elvn.lang.result;

import net.kucoe.elvn.util.Config;
import net.kucoe.elvn.util.Display;

/**
 * Marker for range collecting
 * 
 * @author Vitaliy Basyuk
 */
public class TaskRange extends BaseResult {
    
    /**
     * Range start
     */
    public final int start;
    
    /**
     * Range end
     */
    public final int end;
    
    /**
     * Constructs TaskRange.
     * 
     * @param start
     * @param end
     */
    public TaskRange(final int start, final int end) {
        this.start = start;
        this.end = end;
    }
    
    @Override
    public String execute(final Display display, final Config config) throws Exception {
        return null;
    }
    
}
