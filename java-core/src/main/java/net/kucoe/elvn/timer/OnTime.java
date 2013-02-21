package net.kucoe.elvn.timer;

/**
 * What should be done on time.
 * 
 * @author Vitaliy Basyuk
 */
public interface OnTime {
    
    /**
     * Executes on time.
     * 
     * @param completed true if complete button was invoked
     * @throws Exception
     */
    void onTime(boolean completed) throws Exception;
    
}
