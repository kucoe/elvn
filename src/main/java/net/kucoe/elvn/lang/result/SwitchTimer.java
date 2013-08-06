package net.kucoe.elvn.lang.result;

import net.kucoe.elvn.timer.Timer;
import net.kucoe.elvn.util.Config;
import net.kucoe.elvn.util.Display;

/**
 * Switch to timer menu.
 * 
 * @author Vitaliy Basyuk
 */
public class SwitchTimer extends Switch {
    
    @Override
    public String execute(final Display display, final Config config) throws Exception {
        Timer.show();
        return null;
    }
    
}
