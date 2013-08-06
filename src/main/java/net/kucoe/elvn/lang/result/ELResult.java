package net.kucoe.elvn.lang.result;

import net.kucoe.elvn.util.Config;
import net.kucoe.elvn.util.Display;

/**
 * Result description.
 * 
 * @author Vitaliy Basyuk
 */
public interface ELResult {
    
    /**
     * System execute method.
     * 
     * @param display
     * @param config
     * @return list change
     * @throws Exception
     */
    String execute(Display display, Config config) throws Exception;
}
