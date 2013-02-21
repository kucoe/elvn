package net.kucoe.elvn.lang.result;

import net.kucoe.elvn.lang.ELCommand;
import net.kucoe.elvn.util.Config;
import net.kucoe.elvn.util.Display;

/**
 * Switch to sync menu.
 * 
 * @author Vitaliy Basyuk
 */
public class SwitchSync extends Switch {
    
    @Override
    public String execute(final Display display, final Config config) throws Exception {
        config.reload();
        display.showConfig(config.getConfig());
        return ELCommand.Sync.el();
    }
}
