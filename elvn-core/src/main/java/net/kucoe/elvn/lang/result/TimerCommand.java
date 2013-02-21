package net.kucoe.elvn.lang.result;

import static net.kucoe.elvn.lang.result.TimerCommand.Command.*;
import static net.kucoe.elvn.timer.Timer.*;

import java.util.*;

import net.kucoe.elvn.ListColor;
import net.kucoe.elvn.Task;
import net.kucoe.elvn.timer.OnTime;
import net.kucoe.elvn.util.Config;
import net.kucoe.elvn.util.Display;

/**
 * Execute timer command
 * 
 * @author Vitaliy Basyuk
 */
public class TimerCommand extends SwitchTimer {
    
    /**
     * Timer commands
     * 
     * @author Vitaliy Basyuk
     */
    public enum Command {
        
        /**
         * Play command
         */
        Play(">"),
        
        /**
         * Pause command
         */
        Pause(":"),
        
        /**
         * Stop command
         */
        Stop("x"),
        
        /**
         * Skip command
         */
        Skip("^"),
        
        /**
         * Done command
         */
        Done("v");
        
        private String alias;
        
        private static Map<String, Command> commands = new HashMap<String, TimerCommand.Command>();
        
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
     * Constructs TimerCommand.
     * 
     * @param command
     */
    public TimerCommand(final String command) {
        this.command = command;
    }
    
    @Override
    public String execute(final Display display, final Config config) throws Exception {
        boolean reshow = true;
        Command c = Command.command(command);
        if (c == null) {
            String helpMessage = getHelpMessage(command);
            display.showHelp(helpMessage);
        } else {
            if (Pause.equals(c)) {
                pause();
            } else if (Play.equals(c)) {
                if (isRunning()) {
                    resume();
                } else {
                    List<Task> tasks = config.getList(ListColor.Teal).getTasks();
                    if (!tasks.isEmpty()) {
                        final Iterator<Task> it = tasks.iterator();
                        Task task = it.next();
                        OnTime onTime = new OnTime() {
                            public void onTime(final boolean completed) throws Exception {
                                if (it.hasNext()) {
                                    config.runTask(it.next(), this);
                                }
                            }
                        };
                        config.runTask(task, onTime);
                    }
                    reshow = false;
                }
            } else if (Stop.equals(c)) {
                cancel();
            } else if (Skip.equals(c)) {
                fire(false);
            } else if (Done.equals(c)) {
                fire(true);
            }
            if (reshow) {
                show();
            }
        }
        return null;
    }
    
    protected static String getHelpMessage(String command) {
        String helpMessage =
                "\tWrong timer command: " + command + "\n" + "\tAvailable commands:\n" + "\t> run/resume task\n"
                        + "\t: pause task\n" + "\t^ skip current stage\n" + "\tx cancel task\n" + "\tv complete task";
        return helpMessage;
    }
}
