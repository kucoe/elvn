package net.kucoe.elvn.util;

import java.io.IOException;
import java.util.*;

/**
 * History manager
 * 
 * @author Vitaliy Basyuk
 */
public class History {
    
    private LinkedList<String> queue = new LinkedList<String>();
    private int pos = -1;
    private final Config config;
    
    /**
     * Constructs History.
     * 
     * @param config
     */
    public History(final Config config) {
        this.config = config;
    }
    
    /**
     * Add history record.
     * 
     * @param string
     */
    public synchronized void introduce(final String string) {
        queue.add(0, string);
        pos = -1;
        if (queue.size() > 100) {
            queue = new LinkedList<String>(queue.subList(0, 100));
        }
        try {
            persist();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JsonException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Returns next command
     * 
     * @return string
     */
    public String getNext() {
        if (pos - 1 < 0) {
            return "";
        }
        pos--;
        return queue.get(pos);
    }
    
    /**
     * Returns previous command
     * 
     * @return string
     */
    public String getPrevious() {
        if (queue.size() > pos + 1) {
            pos++;
        }
        return queue.get(pos);
    }
    
    /**
     * Prepare history.
     * 
     * @throws IOException
     * @throws JsonException
     */
    public void prepare() throws IOException, JsonException {
        String json = config.getHistory();
        Map<String, Object> map = new Jsonizer().read(json);
        @SuppressWarnings("unchecked")
        java.util.List<String> historyPart = (java.util.List<String>) map.get("history");
        queue = new LinkedList<String>(historyPart);
    }
    
    /**
     * Save history to file.
     * 
     * @throws IOException
     * @throws JsonException
     */
    public void persist() throws IOException, JsonException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("history", queue);
        String json = new Jsonizer().write(map);
        config.saveHistory(json);
    }
}
