package net.kucoe.elvn.lang.result;

import net.kucoe.elvn.lang.ELCommand;
import net.kucoe.elvn.util.Config;
import net.kucoe.elvn.util.Display;

/**
 * Switch to list edit menu.
 * 
 * @author Vitaliy Basyuk
 */
public class SwitchListEdit extends Switch {
    @Override
    public String execute(final Display display, final Config config) throws Exception {
        display.showLists(config);
        return ELCommand.ListEdit.el();
    }
    
}
