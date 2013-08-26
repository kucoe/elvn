package net.kucoe.elvn.util;

import static net.kucoe.elvn.ListColor.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import net.kucoe.elvn.*;
import net.kucoe.elvn.List;
import net.kucoe.elvn.sync.Sync;
import net.kucoe.elvn.timer.OnTime;
import net.kucoe.elvn.timer.Timer;

/**
 * Configuration tool
 * 
 * @author Vitaliy Basyuk
 */
public class Config {
    
    private Map<ListColor, List> lists = new HashMap<ListColor, List>();
    private java.util.List<Idea> ideas = new ArrayList<Idea>();
    private Date todayStart;
    private long lastModified;
    private StatusUpdateListener listener;
    private Sync sync;
    
    /**
     * Overrides listener the listener.
     * 
     * @param listener the listener to set.
     */
    public void setStatusListener(final StatusUpdateListener listener) {
        this.listener = listener;
    }
    
    /**
     * Returns ideas
     * 
     * @return {@link List}
     * @throws IOException
     * @throws JsonException
     */
    public java.util.List<Idea> getIdeas() throws IOException, JsonException {
        maybeInit();
        return ideas;
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
        list = getList(Done);
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
     * Deletes idea.
     * 
     * @param idea
     * @throws JsonException
     * @throws IOException
     */
    public void removeIdea(final Idea idea) throws IOException, JsonException {
        maybeInit();
        ideas.remove(idea);
        commit();
    }
    
    /**
     * Saves or updates idea.
     * 
     * @param idea
     * @throws JsonException
     * @throws IOException
     */
    public void saveIdea(final Idea idea) throws IOException, JsonException {
        maybeInit();
        int idx = ideas.indexOf(idea);
        if (idx > -1) {
            ideas.remove(idx);
            ideas.add(idx, idea);
        } else {
            ideas.add(idea);
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
                    && !(ListColor.Blue.equals(listColor) || ListColor.isSystemColor(listColor))) {
                lists.remove(listColor);
            } else {
                List newList = new List(listColor, label);
                newList.getTasks().addAll(list.getTasks());
                lists.put(listColor, newList);
            }
        } else {
            List newList = new List(listColor, label);
            lists.put(listColor, newList);
        }
        commit();
    }
    
    /**
     * Search for task or idea by id.
     * 
     * @param id
     * @return found task or idea
     * @throws JsonException
     * @throws IOException
     */
    public Idea getById(final Long id) throws IOException, JsonException {
        if (id == null) {
            return null;
        }
        maybeInit();
        for (Idea idea : getIdeas()) {
            if (idea.getId().equals(id)) {
                return idea;
            }
        }
        for (Task task : getList(ListColor.All).getTasks()) {
            if (task.getId().equals(id)) {
                return task;
            }
        }
        for (Task task : getList(ListColor.Done).getTasks()) {
            if (task.getId().equals(id)) {
                return task;
            }
        }
        return null;
    }
    
    /**
     * Search for ideas containing text.
     * 
     * @param query
     * @return list of found ideas
     * @throws JsonException
     * @throws IOException
     */
    public java.util.List<Idea> findIdeas(final String query) throws IOException, JsonException {
        maybeInit();
        java.util.List<Idea> result = new ArrayList<Idea>();
        if (query == null) {
            return result;
        }
        for (Idea idea : getIdeas()) {
            filter(query, idea, result);
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
        for (Task task : getList(ListColor.All).getTasks()) {
            filter(query, task, result);
        }
        for (Task task : getList(ListColor.Done).getTasks()) {
            filter(query, task, result);
        }
        return result;
    }
    
    private <T extends Idea> void filter(final String query, final T idea, final java.util.List<T> result) {
        String text = idea.getText();
        if (text != null && text.toLowerCase().contains(query.toLowerCase())) {
            result.add(idea);
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
            InputStream mainResource = getClass().getResourceAsStream("/net/kucoe/elvn/data/history");
            String st = read(mainResource);
            saveToFile(st, getHistoryPath());
        }
        return getHistoryPath();
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
     * Reads configuration from file.
     * 
     * @return string
     * @throws IOException
     */
    public String getSyncConfig() throws IOException {
        InputStream resource = getStream(getSyncConfigPath());
        if (resource != null) {
            return read(resource);
        }
        return null;
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
        int planned = getList(Today).getTasks().size();
        int done = 0;
        java.util.List<Task> tasks = getList(Done).getTasks();
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
    
    /**
     * Returns {@link Sync} if not yet exists initialize it from configs
     * 
     * @return {@link Sync}
     * @throws IOException
     */
    public Sync getSync() throws IOException {
        if (sync == null) {
            String string = getSyncConfig();
            Properties prop = new Properties();
            if (string != null) {
                prop.load(new ByteArrayInputStream(string.getBytes("UTF-8")));
                String email = prop.getProperty("email");
                String server = prop.getProperty("server");
                boolean noKey = Boolean.parseBoolean(prop.getProperty("nokey"));
                sync = new Sync(email, noKey, server, getBasePath(), this);
            }
        }
        return sync;
    }
    
    private boolean isToday(final Date date) {
        initToday();
        return date.equals(todayStart);
    }
    
    @SuppressWarnings("unchecked")
    private synchronized void maybeInit() throws IOException, JsonException {
        if (lists.isEmpty() || fileUpdated()) {
            String string = getConfig();
            Jsonizer jsonizer = new Jsonizer();
            Map<String, Object> map = jsonizer.read(string);
            lists = new HashMap<ListColor, List>();
            List list = new List(All, "All");
            lists.put(All, list);
            java.util.List<Map<String, Object>> listsPart = (java.util.List<Map<String, Object>>) map.get("lists");
            for (Map<String, Object> listPart : listsPart) {
                String label = (String) listPart.get("label");
                String color = (String) listPart.get("color");
                ListColor listColor = ListColor.color(color);
                if (listColor != null) {
                    list = new List(listColor, label);
                    lists.put(listColor, list);
                }
            }
            list = new List(Today, "Today");
            lists.put(Today, list);
            list = new List(Done, "Completed");
            lists.put(Done, list);
            java.util.List<Map<String, Object>> tasksPart = (java.util.List<Map<String, Object>>) map.get("tasks");
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
                    lists.get(All).getTasks().add(task);
                    if (p) {
                        lists.get(Today).getTasks().add(task);
                    }
                } else {
                    lists.get(Done).getTasks().add(task);
                }
            }
            ideas = new ArrayList<Idea>();
            java.util.List<Map<String, Object>> ideasPart = (java.util.List<Map<String, Object>>) map.get("ideas");
            for (Map<String, Object> ideaPart : ideasPart) {
                String text = (String) ideaPart.get("text");
                Long id = (Long) ideaPart.get("id");
                long i = id == null ? 1 : id.longValue();
                Idea idea = new Idea(i, text);
                ideas.add(idea);
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
            if (All.toString().equals(color) || Today.toString().equals(color)) {
                continue;
            }
            if (!Done.toString().equals(color)) {
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
        java.util.List<Map<String, Object>> ideasPart = new LinkedList<Map<String, Object>>();
        for (Idea idea : ideas) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("id", idea.getId());
            map.put("text", idea.getText());
            ideasPart.add(map);
        }
        Map<String, Object> main = new LinkedHashMap<String, Object>();
        main.put("lists", listsPart);
        main.put("tasks", tasksPart);
        main.put("ideas", ideasPart);
        String json = new Jsonizer().write(main);
        json = json.replace("},", "},\n");
        json = json.replace("],", "],\n");
        saveConfig(json);
        if (sync != null) {
            sync.push();
        }
        lists = new HashMap<ListColor, List>();
        ideas = new ArrayList<Idea>();
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
        return getBasePath() + "config.json";
    }
    
    protected String getSyncConfigPath() {
        checkElvnDir();
        return getBasePath() + "sync.ini";
    }
    
    protected String getHistoryPath() {
        checkElvnDir();
        return getBasePath() + "history";
    }
    
    protected void checkElvnDir() {
        File file = new File(getBasePath());
        if (!file.exists()) {
            file.mkdir();
        }
    }
    
    protected String getBasePath() {
        return getUserDir() + "/.elvn/";
    }
    
    protected String getUserDir() {
        return System.getProperty("user.home");
    }
    
}
