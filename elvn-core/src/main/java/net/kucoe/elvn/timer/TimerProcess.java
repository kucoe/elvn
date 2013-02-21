package net.kucoe.elvn.timer;

/**
 * Timer process interface
 * 
 * @author Vitaliy Basyuk
 */
public interface TimerProcess {
    
    /**
     * Initiate process
     * 
     * @param timeout
     * @param view
     * @param onTime
     */
    void init(int timeout, TimerView view, OnTime onTime);
    
    /**
     * Resume process
     */
    void play();
    
    /**
     * Suspend process
     */
    void stop();
    
    /**
     * Cancel process
     */
    void cancel();
    
    /**
     * Fire process
     * 
     * @param complete whether complete whole process or stage
     */
    void fire(boolean complete);
}
