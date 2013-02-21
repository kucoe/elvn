package net.kucoe.elvn.util;

import java.io.IOException;

import net.kucoe.elvn.*;

/**
 * Display interface
 * 
 * @author Vitaliy Basyuk
 */
public interface Display {
    
    /**
     * Shows help message.
     * 
     * @param helpMessage
     */
    void showHelp(String helpMessage);
    
    /**
     * Shows status.
     * 
     * @param status
     */
    void showStatus(String status);
    
    /**
     * Shows config as a text.
     * 
     * @param config
     */
    void showConfig(String config);
    
    /**
     * Displays tasks lists.
     * 
     * @param config
     * @throws IOException
     * @throws JsonException
     */
    void showLists(Config config) throws IOException, JsonException;
    
    /**
     * Displays tasks list.
     * 
     * @param list {@link List}
     */
    void showList(List list);
    
    /**
     * Displays tasks list.
     * 
     * @param tasks {@link java.util.List}
     */
    void showTasks(java.util.List<Task> tasks);
    
    /**
     * Displays note list.
     * 
     * @param list {@link java.util.List}
     */
    void showNotes(java.util.List<Note> list);
    
    /**
     * Inlines note
     * 
     * @param note
     * @param position
     */
    void showNote(Note note, int position);
    
    /**
     * Inlines task
     * 
     * @param task
     * @param position
     */
    void showTask(Task task, int position);
    
    /**
     * Returns string
     * 
     * @return string
     */
    String getCurrentList();
    
    /**
     * Overrides current list.
     * 
     * @param current
     */
    void setCurrentList(String current);
}
