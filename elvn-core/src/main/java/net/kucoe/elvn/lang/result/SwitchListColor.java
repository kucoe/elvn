package net.kucoe.elvn.lang.result;

import net.kucoe.elvn.List;
import net.kucoe.elvn.ListColor;
import net.kucoe.elvn.util.Config;
import net.kucoe.elvn.util.Display;

/**
 * Switch to list by color
 * 
 * @author Vitaliy Basyuk
 */
public class SwitchListColor extends SwitchList {
    
    /**
     * Color
     */
    public final String color;
    
    /**
     * Constructs SwitchListColor.
     * 
     * @param color
     */
    public SwitchListColor(final String color) {
        this.color = color;
    }
    
    @Override
    public String execute(final Display display, final Config config) throws Exception {
        List list = config.getList(ListColor.color(color));
        if (list != null) {
            display.showList(list);
            return list.getColor();
        }
        return null;
    }
    
}
