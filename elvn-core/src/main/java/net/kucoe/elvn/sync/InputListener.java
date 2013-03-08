package net.kucoe.elvn.sync;

/**
 * Listener called on next input line.
 * 
 * @author Vitaliy Basyuk
 */
public interface InputListener {
    /**
     * Main method called.
     * 
     * @param line
     * @return true to continue line reading, false to break
     */
    boolean onNextLine(String line);
    
    /**
     * Return whole input string if any.
     * 
     * @return string
     */
    String getInputString();
}
