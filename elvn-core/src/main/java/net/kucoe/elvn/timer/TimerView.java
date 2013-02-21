package net.kucoe.elvn.timer;

import net.kucoe.elvn.Task;

/**
 * Timer view interface.
 * 
 * @author Vitaliy Basyuk
 */
public interface TimerView {
    
    /**
     * View behavior on timer show.
     * 
     * @param task
     * @param stage
     * @param seconds
     */
    void show(Task task, TaskStage stage, final int seconds);
    
    /**
     * Shows small view.
     */
    void showSmall();
    
    /**
     * View behavior on timer hide.
     */
    void hide();
    
    /**
     * Updates time.
     * 
     * @param seconds
     */
    void update(int seconds);
    
    /**
     * Plays on time sound.
     * 
     * @param stage
     */
    void playOnTime();
    
    /**
     * Plays on time sound.
     * 
     * @param stage
     */
    void playOnStart();
    
    /**
     * Toggle time periodical show.
     */
    void silent();
    
}
