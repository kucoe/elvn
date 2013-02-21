package net.kucoe.elvn.lang.result;

import net.kucoe.elvn.List;
import net.kucoe.elvn.util.Config;
import net.kucoe.elvn.util.Display;

/**
 * Switch to list by label.
 * 
 * @author Vitaliy Basyuk
 */
public class SwitchListLabel extends SwitchList {
    
    /**
     * Label
     */
    public final String label;
    
    /**
     * Constructs SwitchListLabel.
     * 
     * @param label
     */
    public SwitchListLabel(final String label) {
        this.label = label;
    }
    
    @Override
    public String execute(final Display display, final Config config) throws Exception {
        List list = config.getList(label);
        if (list != null) {
            display.showList(list);
            return list.getColor();
        }
        return null;
    }
    
}
