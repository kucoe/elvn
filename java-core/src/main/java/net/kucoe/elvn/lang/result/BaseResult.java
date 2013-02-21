package net.kucoe.elvn.lang.result;

import net.kucoe.elvn.util.Config;
import net.kucoe.elvn.util.Display;

/**
 * Abstract {@link ELResult} implementation
 * 
 * @author Vitaliy Basyuk
 */
public abstract class BaseResult implements ELResult {
    
    /**
     * Enable/disable forward
     */
    public boolean forwardEnabled = true;
    
    protected String forward(final ELResult result, final Display display, final Config config) throws Exception {
        if (!forwardEnabled) {
            return null;
        }
        return result.execute(display, config);
    }
}
