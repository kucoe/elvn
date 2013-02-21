package net.kucoe.elvn.util;

import java.io.IOException;
import java.util.Collections;

import net.kucoe.elvn.*;
import net.kucoe.elvn.lang.ELCommand;

/**
 * {@link Display} implementation
 * 
 * @author Vitaliy Basyuk
 */
public class ConsoleDisplay implements Display {
    
    private String currentList;
    
    @Override
    public void showHelp(String helpMessage) {
        showBodyText(helpMessage);
    }
    
    @Override
    public void showStatus(final String status) {
        showBodyText(status);
    }
    
    @Override
    public void showConfig(final String config) {
        showHeader("Config");
        showBodyText(config);
    }
    
    @Override
    public void showLists(final Config config) throws IOException, JsonException {
        showHeader("Lists");
        for (ListColor color : ListColor.values()) {
            if (ListColor.White.equals(color) || ListColor.Teal.equals(color) || ListColor.Stroke.equals(color)) {
                continue;
            }
            List list = config.getList(color);
            if (list == null) {
                showBodyText(color + ":" + List.NOT_ASSIGNED);
            } else {
                showBodyText(color + ":" + list.getLabel());
            }
        }
    }
    
    @Override
    public void showTasks(final java.util.List<Task> tasks) {
        int i = 1;
        Collections.sort(tasks);
        for (Task task : tasks) {
            String format = formatTask(ListColor.White.toString(), task, i);
            System.out.println(format);
            i++;
        }
    }
    
    @Override
    public void showList(final List list) {
        showHeader(list.getLabel());
        int i = 1;
        java.util.List<Task> tasks = list.getTasks();
        Collections.sort(tasks);
        for (Task task : tasks) {
            String format = formatTask(list.getColor(), task, i);
            System.out.println(format);
            i++;
        }
    }
    
    @Override
    public void showNotes(final java.util.List<Note> notes) {
        showHeader("Notes");
        int i = 1;
        Collections.sort(notes);
        for (Note note : notes) {
            System.out.println(formatNote(note, i));
            i++;
        }
    }
    
    @Override
    public void showNote(final Note note, final int position) {
        showBodyText(ELCommand.Locate.el() + position + ELCommand.Assign.el() + formatNote(note, 0));
    }
    
    @Override
    public void showTask(final Task task, final int position) {
        showBodyText(ELCommand.Locate.el() + position + ELCommand.Assign.el()
                + formatTask(ListColor.White.toString(), task, 0));
    }
    
    @Override
    public void setCurrentList(final String current) {
        currentList = current;
    }
    
    @Override
    public String getCurrentList() {
        return currentList;
    }
    
    protected String formatTask(final String currentList, final Task task, final int pos) {
        StringBuilder sb = new StringBuilder();
        if (pos > 0) {
            sb.append('\t');
            sb.append(pos);
            sb.append(".");
        }
        if (task.getCompletedOn() == null && ListColor.White.toString().equals(currentList)) {
            sb.append(task.getList());
            sb.append(":");
        }
        sb.append(task.getText());
        if (task.getCompletedOn() == null && task.isPlanned() && !ListColor.Teal.toString().equals(currentList)) {
            sb.append("-planned");
        }
        return sb.toString();
    }
    
    protected String formatNote(final Note note, final int pos) {
        StringBuilder sb = new StringBuilder();
        if (pos > 0) {
            sb.append('\t');
            sb.append(pos);
            sb.append(".");
        }
        sb.append(note.getText());
        return sb.toString();
    }
    
    protected void showHeader(final String header) {
        System.out.println("\t" + header);
    }
    
    protected void showBodyText(final String text) {
        System.out.println(text);
    }
    
}
