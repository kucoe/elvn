package net.kucoe.elvn.sync;

import net.kucoe.elvn.util.Console;

/**
 * Console implementation for {@link SyncStatusListener}
 * 
 * @author Vitaliy Basyuk
 */
public class ConsoleSyncStatus implements SyncStatusListener {
    
    private final Console console;
    
    /**
     * Constructs ConsoleSyncStatus.
     * 
     * @param console
     */
    public ConsoleSyncStatus(final Console console) {
        this.console = console;
    }
    
    @Override
    public void onStatusChange(final String status) {
        console.write(status);
    }
    
    @Override
    public String promptForPassword(final String prompt) {
        return console.readPassword(prompt);
    }
    
}
