package net.kucoe.elvn.util;

/**
 * JSON processing exception
 * 
 * @author Vitaliy Basyuk
 */
public class JsonException extends Exception {
    
    private static final long serialVersionUID = -7869845111090622916L;
    
    /**
     * Constructs JsonException.
     * 
     * @param message
     */
    public JsonException(final String message) {
        super(message);
    }
    
}
