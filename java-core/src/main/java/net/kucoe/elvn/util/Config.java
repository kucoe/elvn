package net.kucoe.elvn.util;

import static net.kucoe.elvn.ListColor.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import net.kucoe.elvn.*;
import net.kucoe.elvn.List;
import net.kucoe.elvn.timer.OnTime;
import net.kucoe.elvn.timer.Timer;

/**
 * Configuration tool
 * 
 * @author Vitaliy Basyuk
 */
public class Config {
    
    private Map<ListColor, List> lists = new HashMap<ListColor, List>();
    private java.util.List<Note> notes = new ArrayList<Note>();
    private Date todayStart;
    private long lastModified;
    private StatusUpdateListener listener;
    
    /**
     * Overrides listener the listener.
     * 
     * @param listener the listener to set.
     */
    public void setStatusListener(final StatusUpdateListener listener) {
        this.listener = listener;
    }
    
    /**
     * Returns notes
     * 
     * @return {@link List}
     * @throws IOException
     * @throws JsonException
     */
    public java.util.List<Note> getNotes() throws IOException, JsonException {
        maybeInit();
        return notes;
    }
    
    /**
     * Returns list by color
     * 
     * @param color
     * @return {@link List}
     * @throws IOException
     * @throws JsonException
     */
    public List getList(final ListColor color) throws IOException, JsonException {
        maybeInit();
        return lists.get(color);
    }
    
    /**
     * Returns list by label
     * 
     * @param label
     * @return {@link List}
     * @throws IOException
     * @throws JsonException
     */
    public List getList(final String label) throws IOException, JsonException {
        maybeInit();
        for (List list : lists.values()) {
            if (list.getLabel().equals(label)) {
                return list;
            }
        }
        return null;
    }
    
    /**
     * Runs task
     * 
     * @param task
     * @param onTime
     * @throws Exception
     */
    public void runTask(final Task task, final OnTime onTime) throws Exception {
        if (task == null) {
            return;
        }
        maybeInit();
        saveTask(new Task(task.getId(), task.getList(), task.getText(), true, null));
        Timer.runElvn(task, new OnTime() {
            @Override
            public void onTime(final boolean completed) throws Exception {
                saveTask(new Task(task.getId(), task.getList(), task.getText(), true, new Date()));
                if (onTime != null) {
                    onTime.onTime(completed);
                }
            }
        });
    }
    
    /**
     * Deletes task.
     * 
     * @param task {@link Task}
     * @throws JsonException
     * @throws IOException
     */
    public void removeTask(final Task task) throws IOException, JsonException {
        maybeInit();
        remove(task);
        commit();
    }
    
    private void remove(final Task task) throws IOException, JsonException {
        List list = getList(ListColor.color(task.getList()));
        list.getTasks().remove(task);
        list = getList(Stroke);
        list.getTasks().remove(task);
    }
    
    /**
     * Saves or updates task.
     * 
     * @param task {@link Task}
     * @throws JsonException
     * @throws IOException
     */
    public void saveTask(final Task task) throws IOException, JsonException {
        maybeInit();
        List list = getList(ListColor.color(task.getList()));
        java.util.List<Task> tasks = list.getTasks();
        int idx = tasks.indexOf(task);
        if (idx > -1) {
            tasks.remove(idx);
            tasks.add(idx, task);
        } else {
            tasks.add(task);
        }
        commit();
        if (listener != null) {
            listener.onStatusChange(getStatus());
        }
    }
    
    /**
     * Deletes note.
     * 
     * @param note
     * @throws JsonException
     * @throws IOException
     */
    public void removeNote(final Note note) throws IOException, JsonException {
        maybeInit();
        notes.remove(note);
        commit();
    }
    
    /**
     * Saves or updates note.
     * 
     * @param note
     * @throws JsonException
     * @throws IOException
     */
    public void saveNote(final Note note) throws IOException, JsonException {
        maybeInit();
        int idx = notes.indexOf(note);
        if (idx > -1) {
            notes.remove(idx);
            notes.add(idx, note);
        } else {
            notes.add(note);
        }
        commit();
    }
    
    /**
     * Saves or updates list.
     * 
     * @param color
     * @param label
     * @throws JsonException
     * @throws IOException
     */
    public void saveList(final String color, final String label) throws IOException, JsonException {
        maybeInit();
        ListColor listColor = ListColor.color(color);
        List list = getList(listColor);
        if (list != null) {
            if ((label == null || label.isEmpty() || List.NOT_ASSIGNED.equals(label))
                    && !(ListColor.Blue.equals(listColor) || ListColor.White.equals(listColor)
                            || ListColor.Teal.equals(listColor) || ListColor.Stroke.equals(listColor))) {
                lists.remove(listColor);
            } else {
                List newList = new List(label, listColor.toString());
                newList.getTasks().addAll(list.getTasks());
                lists.put(listColor, newList);
            }
        } else {
            List newList = new List(label, listColor.toString());
            lists.put(listColor, newList);
        }
        commit();
    }
    
    /**
     * Search for task or note by id.
     * 
     * @param id
     * @return found task or note
     * @throws JsonException
     * @throws IOException
     */
    public Note getById(final Long id) throws IOException, JsonException {
        if (id == null) {
            return null;
        }
        maybeInit();
        for (Note note : getNotes()) {
            if (note.getId().equals(id)) {
                return note;
            }
        }
        for (Task task : getList(ListColor.White).getTasks()) {
            if (task.getId().equals(id)) {
                return task;
            }
        }
        for (Task task : getList(ListColor.Stroke).getTasks()) {
            if (task.getId().equals(id)) {
                return task;
            }
        }
        return null;
    }
    
    /**
     * Search for notes containing text.
     * 
     * @param query
     * @return list of found notes
     * @throws JsonException
     * @throws IOException
     */
    public java.util.List<Note> findNotes(final String query) throws IOException, JsonException {
        maybeInit();
        java.util.List<Note> result = new ArrayList<Note>();
        if (query == null) {
            return result;
        }
        for (Note note : getNotes()) {
            filter(query, note, result);
        }
        return result;
    }
    
    /**
     * Search for tasks containing text.
     * 
     * @param query
     * @return list of found tasks
     * @throws JsonException
     * @throws IOException
     */
    public java.util.List<Task> findTasks(final String query) throws IOException, JsonException {
        maybeInit();
        java.util.List<Task> result = new ArrayList<Task>();
        if (query == null) {
            return result;
        }
        for (Task task : getList(ListColor.White).getTasks()) {
            filter(query, task, result);
        }
        for (Task task : getList(ListColor.Stroke).getTasks()) {
            filter(query, task, result);
        }
        return result;
    }
    
    private <T extends Note> void filter(final String query, final T note, final java.util.List<T> result) {
        String text = note.getText();
        if (text != null && text.toLowerCase().contains(query.toLowerCase())) {
            result.add(note);
        }
    }
    
    /**
     * Returns history file path
     * 
     * @return string
     * @throws IOException
     */
    public String getHistoryFile() throws IOException {
        InputStream resource = getStream(getHistoryPath());
        if (resource == null) {
            InputStream mainResource = getClass().getResourceAsStream("/net/kucoe/elvn/data/history.json");
            String st = read(mainResource);
            saveToFile(st, getHistoryPath());
        }
        return getHistoryPath();
    }
    
    /**
     * Reads history from file.
     * 
     * @return string
     * @throws IOException
     */
    public String getHistory() throws IOException {
        InputStream resource = getStream(getHistoryPath());
        if (resource == null) {
            InputStream mainResource = getClass().getResourceAsStream("/net/kucoe/elvn/data/history.json");
            String st = read(mainResource);
            saveHistory(st);
            return st;
        }
        return read(resource);
    }
    
    /**
     * Reads configuration from file.
     * 
     * @return string
     * @throws IOException
     */
    public String getConfig() throws IOException {
        InputStream resource = getStream(getConfigPath());
        if (resource == null) {
            InputStream mainResource = getClass().getResourceAsStream("/net/kucoe/elvn/data/config.json");
            String st = read(mainResource);
            saveConfig(st);
            return st;
        }
        return read(resource);
    }
    
    /**
     * Save configuration to file.
     * 
     * @param string
     * @throws IOException
     */
    public void saveConfig(final String string) throws IOException {
        saveToFile(string, getConfigPath());
    }
    
    /**
     * Save history to file.
     * 
     * @param string
     * @throws IOException
     */
    public void saveHistory(final String string) throws IOException {
        saveToFile(string, getHistoryPath());
    }
    
    /**
     * Saves and reloads config
     * 
     * @throws JsonException
     * @throws IOException
     */
    public void reload() throws IOException, JsonException {
        maybeInit();
        commit();
    }
    
    /**
     * Returns status planned/completed
     * 
     * @return status
     * @throws JsonException
     * @throws IOException
     */
    public String getStatus() throws IOException, JsonException {
        maybeInit();
        String today = new SimpleDateFormat("E, dd MMMM yyyy", Locale.US).format(new Date());
        int planned = getList(Teal).getTasks().size();
        int done = 0;
        java.util.List<Task> tasks = getList(Stroke).getTasks();
        for (Task task : tasks) {
            if (isToday(task.getCompletedOn())) {
                done++;
                if (task.isPlanned()) {
                    planned++;
                }
            }
        }
        return today + " Planned: " + planned + "; Done: " + done;
    }
    
    private boolean isToday(final Date date) {
        initToday();
        return date.equals(todayStart);
    }
    
    private synchronized void maybeInit() throws IOException, JsonException {
        if (lists.isEmpty() || fileUpdated()) {
            lists = new HashMap<ListColor, List>();
            List list = new List("All", White.toString());
            lists.put(White, list);
            java.util.List<Map<String, Object>> listsPart = getConfigPart("lists");
            for (Map<String, Object> listPart : listsPart) {
                String label = (String) listPart.get("label");
                String color = (String) listPart.get("color");
                ListColor listColor = ListColor.color(color);
                if (listColor != null) {
                    list = new List(label, color);
                    lists.put(listColor, list);
                }
            }
            list = new List("Today", Teal.toString());
            lists.put(Teal, list);
            list = new List("Completed", Stroke.toString());
            lists.put(Stroke, list);
            java.util.List<Map<String, Object>> tasksPart = getConfigPart("tasks");
            for (Map<String, Object> taskPart : tasksPart) {
                String listName = (String) taskPart.get("list");
                String text = (String) taskPart.get("text");
                Long id = (Long) taskPart.get("id");
                Boolean planned = (Boolean) taskPart.get("planned");
                Date completedOn = (Date) taskPart.get("completedOn");
                long i = id == null ? new Date().getTime() : id.longValue();
                boolean p = planned == null ? false : planned;
                Task task = new Task(i, listName, text, p, completedOn);
                if (completedOn == null) {
                    List list4Task = lists.get(ListColor.color(listName));
                    if (list4Task != null) {
                        list4Task.getTasks().add(task);
                    }
                    lists.get(White).getTasks().add(task);
                    if (p) {
                        lists.get(Teal).getTasks().add(task);
                    }
                } else {
                    lists.get(Stroke).getTasks().add(task);
                }
            }
            notes = new ArrayList<Note>();
            java.util.List<Map<String, Object>> notesPart = getConfigPart("notes");
            for (Map<String, Object> notePart : notesPart) {
                String text = (String) notePart.get("text");
                Long id = (Long) notePart.get("id");
                long i = id == null ? 1 : id.longValue();
                Note note = new Note(i, text);
                notes.add(note);
            }
        }
    }
    
    private boolean fileUpdated() {
        File file = new File(getConfigPath());
        long lm = file.lastModified();
        if (file.exists() && lm > lastModified) {
            lastModified = lm;
            return true;
        }
        return false;
    }
    
    @SuppressWarnings("deprecation")
    private synchronized void initToday() {
        if (todayStart == null) {
            Date today = new Date();
            todayStart = new Date(0);
            todayStart.setYear(today.getYear());
            todayStart.setMonth(today.getMonth());
            todayStart.setDate(today.getDate());
            todayStart.setHours(0);
            todayStart.setMinutes(0);
            todayStart.setSeconds(0);
        }
    }
    
    private synchronized void commit() throws IOException, JsonException {
        java.util.List<Map<String, Object>> listsPart = new LinkedList<Map<String, Object>>();
        java.util.List<Map<String, Object>> tasksPart = new LinkedList<Map<String, Object>>();
        for (List list : lists.values()) {
            String color = list.getColor();
            if (White.toString().equals(color) || Teal.toString().equals(color)) {
                continue;
            }
            if (!Stroke.toString().equals(color)) {
                Map<String, Object> map = new LinkedHashMap<String, Object>();
                map.put("label", list.getLabel());
                map.put("color", list.getColor());
                listsPart.add(map);
            }
            for (Task task : list.getTasks()) {
                Map<String, Object> map = new LinkedHashMap<String, Object>();
                map.put("id", task.getId());
                map.put("list", task.getList());
                map.put("text", task.getText());
                map.put("planned", task.isPlanned());
                map.put("completedOn", task.getCompletedOn());
                tasksPart.add(map);
            }
        }
        java.util.List<Map<String, Object>> notesPart = new LinkedList<Map<String, Object>>();
        for (Note note : notes) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("id", note.getId());
            map.put("text", note.getText());
            notesPart.add(map);
        }
        Map<String, Object> main = new LinkedHashMap<String, Object>();
        main.put("lists", listsPart);
        main.put("tasks", tasksPart);
        main.put("notes", notesPart);
        String json = new Jsonizer().write(main);
        json = json.replace("},", "},\n");
        json = json.replace("],", "],\n");
        saveConfig(json);
        lists = new HashMap<ListColor, List>();
        notes = new ArrayList<Note>();
    }
    
    @SuppressWarnings("unchecked")
    protected java.util.List<Map<String, Object>> getConfigPart(final String partName) throws IOException,
            JsonException {
        String string = getConfig();
        Jsonizer jsonizer = new Jsonizer();
        Map<String, Object> map = jsonizer.read(string);
        return (java.util.List<Map<String, Object>>) map.get(partName);
    }
    
    protected InputStream getStream(final String path) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            return new FileInputStream(file);
        }
        return null;
    }
    
    protected String read(final InputStream resource) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(resource));
        String str;
        StringBuilder sb = new StringBuilder();
        while ((str = br.readLine()) != null) {
            sb.append(str);
            sb.append("\n");
        }
        resource.close();
        return sb.toString();
    }
    
    protected synchronized void saveToFile(final String string, final String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
        BufferedWriter out = new BufferedWriter(fileWriter);
        out.write(string);
        out.flush();
        out.close();
        lastModified = file.lastModified();
    }
    
    protected String getConfigPath() {
        checkElvnDir();
        return getUserDir() + "/.elvn/config.json";
    }
    
    protected String getHistoryPath() {
        checkElvnDir();
        return getUserDir() + "/.elvn/history.json";
    }
    
    protected void checkElvnDir() {
        File file = new File(getUserDir() + "/.elvn/");
        if (!file.exists()) {
            file.mkdir();
        }
    }
    
    protected String getUserDir() {
        return System.getProperty("user.home");
    }
    
}
