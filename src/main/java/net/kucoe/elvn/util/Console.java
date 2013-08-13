package net.kucoe.elvn.util;

import java.io.IOException;

import jline.ConsoleReader;

/**
 * Console wrapper
 * 
 * @author Vitaliy Basyuk
 */
public class Console {
    
    private final ConsoleReader reader;
    private final String prompt;
    
    /**
     * Constructs Console.
     * 
     * @throws IOException
     */
    public Console() throws IOException {
        reader = new ConsoleReader();
        prompt = "elvn>";
    }
    
    /**
     * Returns the reader ConsoleReader.
     * 
     * @return the reader ConsoleReader.
     */
    public ConsoleReader getReader() {
        return reader;
    }
    
    /**
     * Reads input line
     * 
     * @return string
     */
    public String readLine() {
        try {
            return reader.readLine(prompt);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Reads input password
     * 
     * @param prompt
     * @return string
     */
    public String readPassword(final String prompt) {
        try {
            return reader.readLine(prompt, '*');
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Writes output
     * 
     * @param text
     */
    public void write(final String text) {
        System.out.println(text);
    }
}
