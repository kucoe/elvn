package net.kucoe.elvn.lang.result;

/**
 * Edit task result marker.
 * 
 * @author Vitaliy Basyuk
 */
public class EditTask extends TaskResult {
    
    /**
     * Base result holder.
     */
    public final TaskResult mainResult;
    
    /**
     * Constructs EditTask.
     * 
     * @param result
     */
    public EditTask(final TaskResult result) {
        mainResult = result;
    }
    
}
