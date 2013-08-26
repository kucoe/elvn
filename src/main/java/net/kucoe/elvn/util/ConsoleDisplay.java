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
    private final Console console;
    
    /**
     * Constructs ConsoleDisplay.
     * 
     * @param console
     */
    public ConsoleDisplay(final Console console) {
        this.console = console;
    }
    
    @Override
    public void showHelp(final String helpMessage) {
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
            if (ListColor.isSystemColor(color)) {
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
            String format = formatTask(ListColor.All.toString(), task, i);
            console.write(format);
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
            console.write(format);
            i++;
        }
    }
    
    @Override
    public void showIdeas(final java.util.List<Idea> ideas) {
        showHeader("Ideas");
        int i = 1;
        Collections.sort(ideas);
        for (Idea idea : ideas) {
            console.write(formatIdea(idea, i));
            i++;
        }
    }
    
    @Override
    public void showIdea(final Idea idea, final int position) {
        showBodyText(ELCommand.Locate.el() + position + ELCommand.Assign.el() + formatIdea(idea, 0));
    }
    
    @Override
    public void showTask(final Task task, final int position) {
        showBodyText(ELCommand.Locate.el() + position + ELCommand.Assign.el()
                + formatTask(ListColor.All.toString(), task, 0));
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
        if (ListColor.isSystemColor(ListColor.color(currentList))) {
            sb.append(task.getList());
            sb.append(":");
        }
        sb.append(task.getText());
        if (task.getCompletedOn() == null && task.isPlanned() && !ListColor.Today.toString().equals(currentList)) {
            sb.append("-planned");
        }
        return sb.toString();
    }
    
    protected String formatIdea(final Idea idea, final int pos) {
        StringBuilder sb = new StringBuilder();
        if (pos > 0) {
            sb.append('\t');
            sb.append(pos);
            sb.append(".");
        }
        sb.append(idea.getText());
        return sb.toString();
    }
    
    protected void showHeader(final String header) {
        console.write("\t" + header);
    }
    
    protected void showBodyText(final String text) {
        console.write(text);
    }
    
}
