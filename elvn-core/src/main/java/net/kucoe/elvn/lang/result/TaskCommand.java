package net.kucoe.elvn.lang.result;

import static net.kucoe.elvn.lang.result.TaskCommand.Command.*;

import java.util.*;

import net.kucoe.elvn.*;
import net.kucoe.elvn.lang.ELCommand;
import net.kucoe.elvn.util.Config;
import net.kucoe.elvn.util.Display;

/**
 * Execute task command
 * 
 * @author Vitaliy Basyuk
 */
public class TaskCommand extends LocateTask {
    
    /**
     * Timer commands
     * 
     * @author Vitaliy Basyuk
     */
    public enum Command {
        
        /**
         * Run command
         */
        Run(">"),
        
        /**
         * Plan command
         */
        Plan("+"),
        
        /**
         * Unplan command
         */
        Unplan("-"),
        
        /**
         * Done command
         */
        Done("v"),
        
        /**
         * Undone command
         */
        Undone("^"),
        
        /**
         * Delete command
         */
        Del("x"),
        
        /**
         * Undone command
         */
        Note("@");
        
        private String alias;
        
        private static Map<String, Command> commands = new HashMap<String, TaskCommand.Command>();
        
        static {
            for (Command command : values()) {
                commands.put(command.alias, command);
            }
        }
        
        private Command(final String alias) {
            this.alias = alias;
        }
        
        /**
         * Returns command alias
         * 
         * @return string
         */
        public String alias() {
            return alias;
        }
        
        /**
         * Returns command by alias
         * 
         * @param alias
         * @return {@link Command}
         */
        public static Command command(String alias) {
            return commands.get(alias);
        }
        
        /**
         * Returns command name
         * 
         * @return string
         */
        public String toString() {
            return name().toLowerCase();
        }
    }
    
    /**
     * Command
     */
    public final String command;
    
    /**
     * Constructs TaskCommand.
     * 
     * @param position
     * @param command
     */
    public TaskCommand(final int position, final String command) {
        super(null, null, position);
        this.command = command;
    }
    
    @Override
    public String execute(final Display display, final Config config) throws Exception {
        String currentList = display.getCurrentList();
        if (ELCommand.Notes.el().equals(currentList)) {
            Note note = getNote(config);
            if (note != null) {
                if (Del.alias().equals(command)) {
                    config.removeNote(note);
                } else {
                    config.removeNote(note);
                    config.saveTask(new Task(note.getId(), command, note.getText(), false, null));
                    return forward(new SwitchListColor(ListColor.All.toString()), display, config);
                }
            }
            return forward(new SwitchNotes(), display, config);
        }
        Command c = Command.command(command);
        if (c == null) {
            String helpMessage = getHelpMessage(command);
            display.showHelp(helpMessage);
        } else {
            final Task task = getTask(currentList, config);
            boolean reshow = true;
            if (task != null) {
                if (Run.equals(c)) {
                    config.runTask(task, null);
                    reshow = false;
                } else if (Del.equals(c)) {
                    config.removeTask(task);
                } else if (Plan.equals(c)) {
                    config.saveTask(new Task(task.getId(), task.getList(), task.getText(), true, null));
                } else if (Unplan.equals(c)) {
                    config.saveTask(new Task(task.getId(), task.getList(), task.getText(), false, null));
                } else if (Done.equals(c)) {
                    config.saveTask(new Task(task.getId(), task.getList(), task.getText(), task.isPlanned(), new Date()));
                } else if (Undone.equals(c)) {
                    config.removeTask(task);
                    config.saveTask(new Task(task.getId(), task.getList(), task.getText(), task.isPlanned(), null));
                } else if (Note.equals(c)) {
                    config.removeTask(task);
                    config.saveNote(new Note(task.getId(), task.getText()));
                    return forward(new SwitchNotes(), display, config);
                }
            }
            if (reshow) {
                return forward(new SwitchListColor(currentList), display, config);
            }
        }
        return null;
    }
    
    protected static String getHelpMessage(String command) {
        String helpMessage =
                "\tWrong task command: " + command + "\n" + "\tAvailable commands:\n" + "\t> run task\n"
                        + "\t@ convert task to note\n" + "\t+ make task planned\n" + "\t- make task not planned\n"
                        + "\tv make task completed\n" + "\t^ make task not completed\n" + "\tx delete task";
        return helpMessage;
    }
    
}
