package net.kucoe.elvn.lang.result;

import static net.kucoe.elvn.lang.result.TaskCommand.Command.Run;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.kucoe.elvn.ListColor;
import net.kucoe.elvn.Task;
import net.kucoe.elvn.lang.ELCommand;
import net.kucoe.elvn.util.*;

/**
 * Group task operator.
 * 
 * @author Vitaliy Basyuk
 */
public class GroupTaskCommand extends TaskCommand {
    
    /**
     * Positions
     */
    public final int[] positions;
    
    /**
     * Constructs GroupTaskCommand.
     * 
     * @param command
     * @param positions
     */
    public GroupTaskCommand(final String command, final int... positions) {
        super(0, command);
        this.positions = positions;
    }
    
    @Override
    public String execute(final Display display, final Config config) throws Exception {
        String currentList = display.getCurrentList();
        Command c = Command.command(command);
        boolean ideas = ELCommand.Ideas.el().equals(currentList);
        if (!ideas && c == null) {
            display.showHelp(getHelpMessage(command));
        } else {
            List<TaskCommand> commands = new ArrayList<TaskCommand>();
            Task first = null;
            String cmd = command;
            if (Run.alias().equals(cmd)) {
                cmd = Command.Plan.alias();
            }
            int[] pos = checkPositions(config, currentList, ideas);
            for (int position : pos) {
                TaskCommand command = createTaskCommand(config, currentList, ideas, cmd, position);
                commands.add(command);
                if (first == null && !ideas) {
                    first = (Task) command.item;
                }
            }
            for (TaskCommand command : commands) {
                command.execute(display, config);
            }
            if (ideas) {
                return forward(new SwitchIdeas(), display, config);
            }
            if (Run.alias().equals(command)) {
                config.runTask(first, null);
            }
            return forward(new SwitchListColor(currentList), display, config);
        }
        return null;
    }
    
    protected int[] checkPositions(final Config config, String currentList, boolean ideas) throws IOException,
            JsonException {
        int[] pos = positions;
        if (pos == null) {
            int size = 0;
            if (ideas) {
                size = config.getIdeas().size();
            } else {
                net.kucoe.elvn.List list = config.getList(ListColor.color(currentList));
                if (list != null) {
                    size = list.getTasks().size();
                }
            }
            pos = new int[size];
            for (int i = 0; i < size; i++) {
                pos[i] = i + 1;
            }
        }
        return pos;
    }
    
    protected TaskCommand createTaskCommand(final Config config, String currentList, boolean ideas, String cmd, int i)
            throws IOException, JsonException {
        TaskCommand command = new TaskCommand(i, cmd);
        if (ideas) {
            command.item = command.getIdea(config);
        } else {
            Task task = command.getTask(currentList, config);
            command.item = task;
        }
        command.forwardEnabled = false;
        return command;
    }
}
