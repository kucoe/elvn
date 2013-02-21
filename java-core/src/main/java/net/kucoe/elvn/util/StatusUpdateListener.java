package net.kucoe.elvn.util;

/**
 * Listener for status changes
 * 
 * @author Vitaliy Basyuk
 */
public interface StatusUpdateListener {
    
    /**
     * Called when status was changed
     * 
     * @param status
     */
    void onStatusChange(final String status);
}
