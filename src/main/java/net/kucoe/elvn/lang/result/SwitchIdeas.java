package net.kucoe.elvn.lang.result;

import java.util.List;

import net.kucoe.elvn.Idea;
import net.kucoe.elvn.lang.ELCommand;
import net.kucoe.elvn.util.Config;
import net.kucoe.elvn.util.Display;


/**
 * Switch ideas result.
 * 
 * @author Vitaliy Basyuk
 */
public class SwitchIdeas extends Switch {
    @Override
    public String execute(final Display display, final Config config) throws Exception {
        List<Idea> ideas = config.getIdeas();
        display.showIdeas(ideas);
        return ELCommand.Ideas.el();
    }
    
}
