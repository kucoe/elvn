package net.kucoe.elvn;

import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import net.kucoe.elvn.controls.CompletedTaskCell;
import net.kucoe.elvn.controls.TaskCell;
import net.kucoe.elvn.lang.result.*;
import net.kucoe.elvn.util.*;

/**
 * Command helping tool
 * 
 * @author Vitaliy Basyuk
 */
public class CommandTool {
    
    private final ObservableList<ListColor> colors = FXCollections.observableArrayList();
    
    private final Display display;
    private final Config config;
    
    /**
     * Constructs CommandTool.
     * 
     * @param display
     * @param config
     */
    public CommandTool(final Display display, final Config config) {
        this.display = display;
        this.config = config;
        colors.add(ListColor.Teal);
        colors.add(ListColor.White);
        for (ListColor color : ListColor.values()) {
            if (ListColor.White.equals(color) || ListColor.Teal.equals(color) || ListColor.Stroke.equals(color)) {
                continue;
            }
            List list = getList(color);
            if (list != null) {
                colors.add(color);
            }
        }
        colors.add(ListColor.Stroke);
    }
    
    /**
     * Returns the colors ColorsBinding.
     * 
     * @return the colors ColorsBinding.
     */
    public ObservableList<ListColor> colors() {
        return colors;
    }
    
    /**
     * Runs task timer
     * 
     * @param task {@link Task}
     */
    public void runTask(final Task task) {
        if (task == null) {
            TimerCommand command = new TimerCommand(TimerCommand.Command.Play.alias());
            doExec(command);
        } else {
            TaskCommand command = new TaskCommand(0, TaskCommand.Command.Run.alias());
            command.item = task;
            doExec(command);
        }
    }
    
    /**
     * Saves list info
     * 
     * @param color
     * @param text
     */
    public void saveList(final ListColor color, final String text) {
        if (text == null || text.isEmpty() || List.NOT_ASSIGNED.equals(text)) {
            colors.remove(color);
        } else {
            if (!colors.contains(color)) {
                colors.add(colors.size() - 2, color);
            }
        }
        TaskResult command = new TaskResult(color.toString(), text);
        doExec(command);
    }
    
    /**
     * Returns list view
     * 
     * @param list
     * @param tasks
     * @return {@link ListView}
     */
    public ListView<Task> createView(final List list, final java.util.List<Task> tasks) {
        final ListColor color = list == null ? ListColor.White : ListColor.color(list.getColor());
        final boolean completeList = ListColor.Stroke.equals(color) ? true : false;
        final ListView<Task> listView = new ListView<>();
        java.util.List<Task> t = tasks;
        if (list != null) {
            t = list.getTasks();
        }
        for (Task task : t) {
            listView.getItems().add(task);
        }
        
        listView.setCellFactory(new Callback<ListView<Task>, ListCell<Task>>() {
            @Override
            public ListCell<Task> call(final ListView<Task> list) {
                if (completeList) {
                    return new CompletedTaskCell(CommandTool.this);
                }
                return new TaskCell(CommandTool.this);
            }
        });
        listView.setEditable(false);
        return listView;
    }
    
    /**
     * Returns list by color
     * 
     * @param color
     * @return {@link List}
     */
    public List getList(final ListColor color) {
        try {
            return config.getList(color);
        } catch (IOException | JsonException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Updates task
     * 
     * @param task
     * @param list
     * @param text
     */
    public void updateTask(final Task task, final String list, final String text) {
        LocateTask command = new LocateTask(list, text, 0);
        command.item = task;
        doExec(command);
    }
    
    /**
     * Updates note
     * 
     * @param note
     * @param text
     */
    public void updateNote(final Note note, final String text) {
        LocateTask command = new LocateTask(null, text, 0);
        command.item = note;
        doExec(command);
    }
    
    /**
     * Remove note command
     * 
     * @param note
     */
    public void removeNote(final Note note) {
        TaskCommand command = new TaskCommand(0, TaskCommand.Command.Del.alias());
        command.item = note;
        doExec(command);
    }
    
    /**
     * Remove task command
     * 
     * @param task
     */
    public void removeTask(final Task task) {
        TaskCommand command = new TaskCommand(0, TaskCommand.Command.Del.alias());
        command.item = task;
        doExec(command);
    }
    
    /**
     * Show item command
     * 
     * @param index
     */
    public void showItem(final int index) {
        LocateTask command = new LocateTask(null, null, index + 1);
        doExec(command);
    }
    
    /**
     * Plans task command
     * 
     * @param task
     */
    public void planTask(final Task task) {
        TaskCommand command = new TaskCommand(0, TaskCommand.Command.Plan.alias());
        command.item = task;
        doExec(command);
    }
    
    /**
     * Unplans task command
     * 
     * @param task
     */
    public void unplanTask(final Task task) {
        TaskCommand command = new TaskCommand(0, TaskCommand.Command.Unplan.alias());
        command.item = task;
        doExec(command);
    }
    
    /**
     * Completes task command
     * 
     * @param task
     */
    public void completeTask(final Task task) {
        TaskCommand command = new TaskCommand(0, TaskCommand.Command.Done.alias());
        command.item = task;
        doExec(command);
    }
    
    /**
     * Uncompletes task command
     * 
     * @param task
     */
    public void uncompleteTask(final Task task) {
        TaskCommand command = new TaskCommand(0, TaskCommand.Command.Undone.alias());
        command.item = task;
        doExec(command);
    }
    
    private void doExec(final ELResult command) {
        try {
            command.execute(display, config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
