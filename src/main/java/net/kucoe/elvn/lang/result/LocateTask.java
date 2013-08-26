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
     * Explicit idea/task item
     */
    public Idea item;
    
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
        if (ELCommand.Ideas.el().equals(currentList)) {
            Idea idea = getIdea(config);
            if (idea != null) {
                if (text == null && list == null) {
                    reshow = false;
                    display.showIdea(idea, position);
                } else if (text != null) {
                    String t = text;
                    if (t == null) {
                        t = "";
                    }
                    t = list == null ? t : list + ":" + t;
                    config.saveIdea(new Idea(idea.getId(), processText(idea.getText(), t)));
                } else if (list != null) {
                    ListColor color = ListColor.color(list);
                    if (color != null) {
                        config.removeIdea(idea);
                        config.saveTask(new Task(idea.getId(), list, idea.getText(), false, null));
                        return forward(new SwitchListColor(color.toString()), display, config);
                    }
                }
            }
            return reshow ? forward(new SwitchIdeas(), display, config) : currentList;
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
    
    protected Idea getIdea(final Config config) throws IOException, JsonException {
        if (item != null) {
            return item;
        }
        java.util.List<Idea> ideas = config.getIdeas();
        if (ideas.size() >= position) {
            return ideas.get(position - 1);
        }
        return null;
    }
    
    protected Task getTask(final String currentList, final Config config) throws IOException, JsonException {
        if (ELCommand.Ideas.el().equals(currentList)) {
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
