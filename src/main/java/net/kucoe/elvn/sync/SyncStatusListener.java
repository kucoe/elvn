package net.kucoe.elvn.sync;

/**
 * Listener for sync status changes
 * 
 * @author Vitaliy Basyuk
 */
public interface SyncStatusListener {
    
    /**
     * Called when status was changed
     * 
     * @param status
     */
    void onStatusChange(final String status);
    
    /**
     * Called when sync needs user password
     * 
     * @param prompt
     * @return received password
     */
    String promptForPassword(final String prompt);
    
}
