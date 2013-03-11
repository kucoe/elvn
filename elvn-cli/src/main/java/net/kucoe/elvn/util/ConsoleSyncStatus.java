package net.kucoe.elvn.util;

import net.kucoe.elvn.sync.SyncStatusListener;

/**
 * Console implementation for {@link SyncStatusListener}
 * 
 * @author Vitaliy Basyuk
 */
public class ConsoleSyncStatus implements SyncStatusListener {
    
    @Override
    public void onStatusChange(final String status) {
        System.out.println(status);
    }
    
}
