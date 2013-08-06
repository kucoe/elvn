package net.kucoe.elvn.lang;

import net.kucoe.elvn.lang.result.ELResult;

/**
 * Elvn langualge processor.<br/>
 * 'stroke' is internal name for completed list<br/>
 * <br/>
 * /blue - switches to task list by color<br/>
 * /?work - switches to task list by label<br/>
 * /all - all tasks<br/>
 * /done - completed tasks<br/>
 * /& - switches to lists edit<br/>
 * /@ - switches to notes<br/>
 * /% - switches to sync menu (sync)<br/>
 * <br/>
 * vars - $task, $note, $list<br/>
 * <br/>
 * proto list {color:white, label:default, taskProto:$task}<br/>
 * proto note {text:}<br/>
 * proto task {list:white, text:, estimate:1,actuals:0,position:1,completedOn:}<br/>
 * <br/>
 * ? - get tasks by text contains<br/>
 * # - gets task by position in list<br/>
 * <br/>
 * examples : ?correct - returns all tasks in list that contains 'correct' in text<br/>
 * #2 - returns task that have 2 as position in list<br/>
 * <br/>
 * with returned task(s) can be done <br/>
 * <br/>
 * Command to save/edit task<br/>
 * <br/>
 * c(olor):text:estimate <br/>
 * text:estimate<br/>
 * text<br/>
 * <br/>
 * example<br/>
 * <br/>
 * #2=Need to update examples<br/>
 * ?correct=$$fixed$$ -that will change 'correct' to 'fixed'<br/>
 * <br/>
 * Move to another list<br/>
 * <br/>
 * #2=b:<br/>
 * #2=stroke: <br/>
 * <br/>
 * 
 * @author Vitaliy Basyuk
 */
public class EL {
    
    /**
     * Processes command.
     * 
     * @param command
     * @return {@link ELResult}
     */
    public static ELResult process(final String command) {
        ELCommand workier = ELCommand.Start;
        workier.init(command);
        while (workier != ELCommand.End) {
            workier = workier.next();
        }
        return workier.getResult();
    }
    
}
