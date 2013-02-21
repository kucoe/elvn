package net.kucoe.elvn.lang.result;

import net.kucoe.elvn.util.Config;
import net.kucoe.elvn.util.Display;

/**
 * Switch to status menu.
 * 
 * @author Vitaliy Basyuk
 */
public class SwitchStatus extends Switch {
    
    @Override
    public String execute(final Display display, final Config config) throws Exception {
        display.showStatus(config.getStatus());
        return null;
    }
}
