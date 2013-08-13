package net.kucoe.elvn.lang.result;

import static net.kucoe.elvn.lang.result.SyncCommand.Command.*;

import java.util.HashMap;
import java.util.Map;

import net.kucoe.elvn.sync.Sync;
import net.kucoe.elvn.util.Config;
import net.kucoe.elvn.util.Display;

/**
 * Execute sync commands
 * 
 * @author Vitaliy Basyuk
 */
public class SyncCommand extends SwitchSync {
    
    /**
     * Sync commands
     * 
     * @author Vitaliy Basyuk
     */
    public enum Command {
        
        /**
         * Push command
         */
        Push(">"),
        
        /**
         * Pull command
         */
        Pull("<"),
        
        /**
         * Revert command
         */
        Revert("-");
        
        private String alias;
        
        private static Map<String, Command> commands = new HashMap<String, Command>();
        
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
        public static Command command(final String alias) {
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
     * Constructs SyncCommand.
     * 
     * @param command
     */
    public SyncCommand(final String command) {
        this.command = command;
    }
    
    @Override
    public String execute(final Display display, final Config config) throws Exception {
        Command c = Command.command(command);
        Sync sync = config.getSync();
        if (c == null) {
            String helpMessage = getHelpMessage(command);
            display.showHelp(helpMessage);
        } else {
            if (sync != null) {
                if (Pull.equals(c)) {
                    sync.pull();
                } else if (Push.equals(c)) {
                    sync.push();
                } else if (Revert.equals(c)) {
                    sync.restore();
                }
            } else {
                display.showHelp("Sync is not initialized");
            }
        }
        return super.execute(display, config);
    }
    
    protected static String getHelpMessage(final String command) {
        String helpMessage =
                "\tWrong sync command: " + command + "\n" + "\tAvailable commands:\n" + "\t> push\n" + "\t< pull\n"
                        + "\t- revert";
        return helpMessage;
    }
}
