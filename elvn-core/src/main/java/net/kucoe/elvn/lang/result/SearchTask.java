package net.kucoe.elvn.lang.result;

import java.util.ArrayList;
import java.util.List;

import net.kucoe.elvn.*;
import net.kucoe.elvn.lang.ELCommand;
import net.kucoe.elvn.util.Config;
import net.kucoe.elvn.util.Display;

/**
 * Task changes over searched tasks.
 * 
 * @author Vitaliy Basyuk
 */
public class SearchTask extends TaskResult {
    
    /**
     * Search query.
     */
    public final String query;
    
    /**
     * Constructs SearchTask.
     */
    public SearchTask() {
        this(null, null, null);
    }
    
    /**
     * Constructs SearchTask.
     * 
     * @param list
     * @param text
     * @param query
     */
    public SearchTask(final String list, final String text, final String query) {
        super(list, patternText(text, query));
        this.query = query;
    }
    
    protected static String patternText(final String text, final String query) {
        String res = text;
        if (res != null && res.startsWith("%")) {
            res = query + res;
        }
        return res;
    }
    
    @Override
    public String execute(final Display display, final Config config) throws Exception {
        String currentList = display.getCurrentList();
        if (ELCommand.Notes.el().equals(currentList)) {
            List<Note> notes = config.findNotes(query);
            if (text != null) {
                for (Note note : notes) {
                    config.saveNote(new Note(note.getId(), processText(note.getText(), text)));
                }
            }
            notes = config.findNotes(query);
            display.showNotes(notes);
            return currentList;
        }
        List<Task> tasks = config.findTasks(query);
        if (text != null || list != null) {
            for (Task task : tasks) {
                updateTask(task, config, currentList);
            }
        }
        List<Task> result = new ArrayList<Task>();
        for (Task t : tasks) {
            result.add((Task) config.getById(t.getId()));
        }
        display.showTasks(result);
        return ListColor.White.toString();
    }
    
}