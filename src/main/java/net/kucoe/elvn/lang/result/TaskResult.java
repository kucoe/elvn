package net.kucoe.elvn.lang.result;

import static net.kucoe.elvn.ListColor.Blue;
import static net.kucoe.elvn.ListColor.Today;

import java.util.Date;

import net.kucoe.elvn.*;
import net.kucoe.elvn.lang.ELCommand;
import net.kucoe.elvn.util.Config;
import net.kucoe.elvn.util.Display;

/**
 * Task changes.
 * 
 * @author Vitaliy Basyuk
 */
public class TaskResult extends BaseResult {
    
    /**
     * Task list.
     */
    public final String list;
    
    /**
     * Task text.
     */
    public final String text;
    
    /**
     * Constructs TaskResult.
     */
    public TaskResult() {
        this(null, null);
    }
    
    /**
     * Constructs TaskResult.
     * 
     * @param list
     * @param text
     */
    public TaskResult(final String list, final String text) {
        this.list = list != null && list.isEmpty() ? null : list;
        this.text = text != null && text.isEmpty() ? null : text;
    }
    
    @Override
    public String execute(final Display display, final Config config) throws Exception {
        String currentList = display.getCurrentList();
        if (ELCommand.Notes.el().equals(currentList)) {
            String t = text;
            if (t == null) {
                t = "";
            }
            t = list == null ? t : list + ":" + t;
            config.saveNote(new Note(new Date().getTime(), t.trim()));
            return forward(new SwitchNotes(), display, config);
        }
        if (ELCommand.ListEdit.el().equals(currentList)) {
            config.saveList(list, text);
            return forward(new SwitchListEdit(), display, config);
        }
        updateTask(new Task(null, Blue.toString(), text, false, null), config, currentList);
        return forward(new SwitchListColor(currentList), display, config);
    }
    
    protected void updateTask(final Task task, final Config config, final String currentList) throws Exception {
        Long id = task.getId();
        if (id == null) {
            id = new Date().getTime();
        }
        String l = list;
        String t = text;
        if (l == null || ListColor.color(l) == null || isRestrictedList(l)) {
            if (l != null && ListColor.color(l) == null) {
                t = t == null ? l + ":" : l + ":" + t;
            }
            if (!isRestrictedList(currentList)) {
                l = currentList;
            } else {
                l = task.getList();
            }
        }
        if (t != null) {
            t = processText(task.getText(), t);
        } else {
            t = task.getText();
        }
        boolean planned = task.isPlanned() || Today.equals(ListColor.color(currentList));
        Date completedOn = task.getCompletedOn();
        
        Task update = new Task(id, l, t, planned, completedOn);
        config.saveTask(update);
    }
    
    protected String processText(final String oldText, String newText) {
        String t = newText;
        if (t.contains("%")) {
            String[] split = t.split("%");
            String replace = "";
            if (split.length > 1) {
                replace = split[1];
            }
            t = oldText.replace(split[0], replace);
        } else if (t.startsWith("+")) {
            t = oldText.concat(t.substring(1));
        } else if (t.startsWith("-")) {
            t = oldText.replace(t.substring(1), "");
        }
        return t.trim();
    }
    
    private boolean isRestrictedList(final String list) {
        ListColor color = ListColor.color(list);
        return ListColor.isSystemColor(color);
    }
}
