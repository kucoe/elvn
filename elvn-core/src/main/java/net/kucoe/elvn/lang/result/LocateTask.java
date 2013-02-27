package net.kucoe.elvn.lang.result;

import java.io.IOException;

import net.kucoe.elvn.*;
import net.kucoe.elvn.lang.ELCommand;
import net.kucoe.elvn.util.*;

/**
 * Task changes over located task by position.
 * 
 * @author Vitaliy Basyuk
 */
public class LocateTask extends TaskResult {
    
    /**
     * Explicit note/task item
     */
    public Note item;
    
    /**
     * Position.
     */
    public final int position;
    
    /**
     * Constructs LocateTask.
     */
    public LocateTask() {
        this(null, null, 0);
    }
    
    /**
     * Constructs LocateTask.
     * 
     * @param list
     * @param text
     * @param position
     */
    public LocateTask(final String list, final String text, final int position) {
        super(list, text);
        this.position = position;
    }
    
    @Override
    public String execute(final Display display, final Config config) throws Exception {
        String currentList = display.getCurrentList();
        boolean reshow = true;
        if (ELCommand.Notes.el().equals(currentList)) {
            Note note = getNote(config);
            if (note != null) {
                if (text == null && list == null) {
                    reshow = false;
                    display.showNote(note, position);
                } else if (text != null) {
                    String t = text;
                    if (t == null) {
                        t = "";
                    }
                    t = list == null ? t : list + ":" + t;
                    config.saveNote(new Note(note.getId(), processText(note.getText(), t)));
                } else if (list != null) {
                    ListColor color = ListColor.color(list);
                    if (color != null) {
                        config.removeNote(note);
                        config.saveTask(new Task(note.getId(), list, note.getText(), false, null));
                        return forward(new SwitchListColor(color.toString()), display, config);
                    }
                }
            }
            return reshow ? forward(new SwitchNotes(), display, config) : currentList;
        }
        Task task = getTask(currentList, config);
        if (task != null) {
            if (list == null && text == null) {
                reshow = false;
                display.showTask(task, position);
            } else {
                updateTask(task, config, currentList);
            }
        }
        return reshow ? forward(new SwitchListColor(currentList), display, config) : currentList;
    }
    
    protected Note getNote(final Config config) throws IOException, JsonException {
        if (item != null) {
            return item;
        }
        java.util.List<Note> notes = config.getNotes();
        if (notes.size() >= position) {
            return notes.get(position - 1);
        }
        return null;
    }
    
    protected Task getTask(final String currentList, final Config config) throws IOException, JsonException {
        if (ELCommand.Notes.el().equals(currentList)) {
            return null;
        }
        if (item instanceof Task) {
            return (Task) item;
        }
        List list = config.getList(ListColor.color(currentList));
        if (list != null) {
            java.util.List<Task> tasks = list.getTasks();
            if (tasks.size() >= position) {
                return tasks.get(position - 1);
            }
        }
        return null;
    }
    
}
