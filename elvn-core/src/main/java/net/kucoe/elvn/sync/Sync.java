package net.kucoe.elvn.sync;

import static net.kucoe.elvn.ListColor.*;

import java.io.*;
import java.net.*;
import java.security.KeyStore;
import java.util.*;
import java.util.List;

import javax.net.ssl.*;

import net.kucoe.elvn.*;
import net.kucoe.elvn.sync.SyncEvent.EventType;
import net.kucoe.elvn.util.*;

/**
 * Remote sync support
 * 
 * @author Vitaliy Basyuk
 */
public class Sync {
    
    private static final String DEFAULT_SERVER_PATH = "https://kucoe.net/elvn/";
    private static final long MONTH = 30 * 24 * 60 * 60 * 1000l;
    
    private final String email;
    private final String password;
    private final String serverPath;
    private final int ignoreLimit;
    private final int interval;
    private final String basePath;
    private final Config config;
    
    private SyncStatusListener statusListener;
    private String userId;
    private boolean stop;
    private boolean online;
    private boolean remoteContent;
    private Thread synchronizer;
    private final Map<String, SyncEvent> events = new HashMap<String, SyncEvent>();
    private long lastUpdate;
    private boolean processing;
    private boolean success;
    
    static {
        try {
            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream keystoreStream = Sync.class.getResourceAsStream("/net/kucoe/elvn/sync/sync.ts");
            keystore.load(keystoreStream, "becevka".toCharArray());
            trustManagerFactory.init(keystore);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustManagers, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Constructs Sync.
     * 
     * @param email
     * @param password
     * @param ignoreLimit
     * @param interval
     * @param serverPath
     * @param basePath
     * @param config {@link Config}
     */
    public Sync(final String email, final String password, final int ignoreLimit, final int interval,
            final String serverPath, final String basePath, final Config config) {
        this.email = email;
        this.password = password;
        this.serverPath = serverPath == null ? DEFAULT_SERVER_PATH : serverPath;
        this.ignoreLimit = ignoreLimit;
        this.interval = interval;
        this.basePath = basePath;
        this.config = config;
        online = true;
    }
    
    /**
     * Overrides statusListener the statusListener.
     * 
     * @param statusListener the statusListener to set.
     */
    public void setStatusListener(final SyncStatusListener statusListener) {
        this.statusListener = statusListener;
    }
    
    /**
     * Changes service to online. Will not work if there is no connection.
     */
    public void online() {
        online = true;
    }
    
    /**
     * Changes service to go offline.
     **/
    public void offline() {
        online = false;
    }
    
    /**
     * Stop
     */
    public synchronized void stop() {
        stop = true;
        try {
            List<SyncEvent> changes = calculateChanges(getClientEvents(), new ArrayList<SyncEvent>());
            if (changes != null && !changes.isEmpty()) {
                saveEvents(toRaw(changes));
            }
            saveLastUpdate();
        } catch (IOException e) {
            // ignore
        }
        synchronized (synchronizer) {
            synchronizer.notifyAll();
        }
        synchronizer = null;
    }
    
    /**
     * Starts
     */
    public synchronized void start() {
        if (synchronizer == null || stop) {
            final int millis = interval * 60 * 1000;
            synchronizer = new Thread() {
                public void run() {
                    while (!stop) {
                        try {
                            sync();
                            synchronized (synchronizer) {
                                synchronizer.wait(millis);
                            }
                        } catch (Exception e) {
                            showSynchronizedFailedStatus(e.getMessage());
                            e.printStackTrace();
                            processing = false;
                        }
                    }
                }
            };
            stop = false;
            synchronizer.start();
        }
    }
    
    /**
     * Synchronizes elvn items. This will not start service, if not started.
     * 
     * @throws JsonException
     * @throws IOException
     */
    public void sync() throws IOException, JsonException {
        if (processing) {
            return;
        }
        processing = true;
        try {
            checkUser();
        } catch (Exception e) {
            e.printStackTrace();
            if (userId == null) {
                return;
            }
            // maybe connection lost, continue
        }
        if (isError(userId)) {
            userId = null;
            showAuthFailedStatus();
        } else {
            checkConfig();
            Map<String, SyncEvent> map = getClientEvents();
            List<SyncEvent> list = new ArrayList<SyncEvent>();
            try {
                list = getServerUpdates();
            } catch (Exception e) {
                // maybe connection lost, continue
            }
            List<SyncEvent> updates = calculateUpdates(map, list);
            if (!updates.isEmpty()) {
                applyUpdates(updates);
            }
            List<SyncEvent> changes = calculateChanges(map, list);
            syncEvents(changes);
            showSynchronizedStatus();
        }
        processing = false;
    }
    
    private void checkUser() throws Exception {
        userId = ask("user");
    }
    
    private void checkConfig() throws IOException, JsonException {
        File file = new File(getUserPath());
        if (!file.exists()) {
            file.mkdir();
            init(file);
        }
    }
    
    private void init(final File userDir) throws IOException, JsonException {
        ask("init", new InputListener() {
            public boolean onNextLine(final String line) {
                if (isError(line)) {
                    return false;
                }
                if (!remoteContent) {
                    remoteContent = true;
                }
                try {
                    writeFile(line, userDir);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            
            public String getInputString() {
                return "";
            }
        });
        if (!remoteContent) {
            put();
            init(userDir);
        }
        lastUpdate = new Date().getTime();
        showInitializedStatus();
    }
    
    private void put() throws IOException, JsonException {
        List<SyncEvent> events = buildFromConfig();
        syncEvents(events);
    }
    
    private void syncEvents(final List<SyncEvent> events) throws IOException {
        if (events.isEmpty()) {
            return;
        }
        String raw = toRaw(events);
        try {
            ask("put", "events", raw);
        } catch (Exception e) {
            saveEvents(raw);
            saveLastUpdate();
        }
    }
    
    private Map<String, SyncEvent> getClientEvents() throws IOException {
        if (events.isEmpty()) {
            restoreSavedEvents();
        }
        return events;
    }
    
    private void restoreSavedEvents() throws IOException {
        String path = userId == null ? basePath + "events.log" : getUserPath() + "events.log";
        File file = new File(path);
        if (file.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    SyncEvent event = SyncEvent.fromRaw(line);
                    if (event != null && event.getItemId() != null) {
                        events.put(event.getItemId(), event);
                    }
                }
            } finally {
                reader.close();
                file.delete();
            }
        }
    }
    
    private void saveEvents(final String rawEvents) throws IOException {
        if (rawEvents == null || rawEvents.isEmpty()) {
            return;
        }
        String path = userId == null ? basePath + "events.log" : getUserPath() + "events.log";
        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
        try {
            writer.write(rawEvents);
        } finally {
            writer.close();
        }
    }
    
    private List<SyncEvent> buildFromConfig() throws IOException, JsonException {
        List<SyncEvent> events = new ArrayList<SyncEvent>();
        List<Note> notes = config.getNotes();
        for (Note note : notes) {
            events.add(new SyncEvent(note, EventType.Create, null));
        }
        for (ListColor color : ListColor.values()) {
            if (All.equals(color) || Today.equals(color)) {
                continue;
            }
            net.kucoe.elvn.List list = config.getList(color);
            if (list == null) {
                continue;
            }
            if (!Done.equals(color)) {
                events.add(new SyncEvent(list, EventType.Create, null));
            }
            for (Task task : list.getTasks()) {
                events.add(new SyncEvent(task, EventType.Create, null));
            }
        }
        events.add(new SyncEvent(new TimerInfo(null, null, 0), EventType.Create, null));
        return events;
    }
    
    private List<SyncEvent> getServerUpdates() throws IOException, JsonException {
        long lastUpdate = getLastUpdate();
        final List<SyncEvent> events = new ArrayList<SyncEvent>();
        if ((new Date().getTime() - lastUpdate) > MONTH) {
            init(new File(getUserPath()));
            return events;
        }
        ask("get", new InputListener() {
            
            @Override
            public boolean onNextLine(final String line) {
                if (isError(line)) {
                    return false;
                }
                events.add(SyncEvent.fromRaw(line));
                return true;
            }
            
            @Override
            public String getInputString() {
                return "";
            }
        }, "after", String.valueOf(lastUpdate));
        this.lastUpdate = new Date().getTime();
        return events;
    }
    
    private long getLastUpdate() throws IOException {
        if (lastUpdate > 0) {
            return lastUpdate;
        }
        String path = userId == null ? basePath + "sync.log" : getUserPath() + "sync.log";
        File file = new File(path);
        if (file.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            try {
                String line = reader.readLine();
                if (line != null) {
                    return Long.valueOf(line);
                }
            } finally {
                reader.close();
            }
        }
        return new Date().getTime();
    }
    
    private void saveLastUpdate() throws IOException {
        if (lastUpdate == 0) {
            return;
        }
        String path = userId == null ? basePath + "sync.log" : getUserPath() + "sync.log";
        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        try {
            writer.write(String.valueOf(lastUpdate));
        } finally {
            writer.close();
        }
    }
    
    private List<SyncEvent> calculateUpdates(final Map<String, SyncEvent> clientMap, final List<SyncEvent> serverList) {
        List<SyncEvent> updates = new ArrayList<SyncEvent>();
        for (SyncEvent event : serverList) {
            SyncEvent change = clientMap.get(event.getItemId());
            if (event.after(change)) {
                updates.add(event);
            }
        }
        return updates;
    }
    
    private void applyUpdates(final List<SyncEvent> updates) throws IOException, JsonException {
        File userDir = new File(getUserPath());
        for (SyncEvent event : updates) {
            String fileName = event.getItemId();
            if (fileName != null) {
                File file = new File(userDir, fileName);
                String body = null;
                if (!file.exists()) {
                    file.createNewFile();
                } else {
                    body = getFileBody(file);
                }
                body = event.apply(body);
                if (body != null) {
                    FileWriter writer = new FileWriter(file);
                    writer.write(body);
                    writer.flush();
                    writer.close();
                } else {
                    file.delete();
                }
            }
        }
        updateConfig();
    }
    
    protected String getFileBody(final File file) throws IOException {
        String result = null;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        try {
            result = reader.readLine();
        } finally {
            reader.close();
        }
        return result;
    }
    
    private void updateConfig() throws IOException, JsonException {
        List<Map<String, Object>> listsPart = new LinkedList<Map<String, Object>>();
        List<Map<String, Object>> tasksPart = new LinkedList<Map<String, Object>>();
        List<Map<String, Object>> notesPart = new LinkedList<Map<String, Object>>();
        File[] files = new File(getUserPath()).listFiles();
        for (File file : files) {
            String line = getFileBody(file);
            ItemParser parser = new ItemParser(line);
            Object item = parser.getItem();
            if (item instanceof net.kucoe.elvn.List) {
                Map<String, Object> map = new LinkedHashMap<String, Object>();
                map.put("label", ((net.kucoe.elvn.List) item).getLabel());
                map.put("color", ((net.kucoe.elvn.List) item).getColor());
                listsPart.add(map);
            } else if (item instanceof Task) {
                Map<String, Object> map = new LinkedHashMap<String, Object>();
                map.put("id", ((Task) item).getId());
                map.put("list", ((Task) item).getList());
                map.put("text", ((Task) item).getText());
                map.put("planned", ((Task) item).isPlanned());
                map.put("completedOn", ((Task) item).getCompletedOn());
                tasksPart.add(map);
            } else if (item instanceof Note) {
                Map<String, Object> map = new LinkedHashMap<String, Object>();
                map.put("id", ((Note) item).getId());
                map.put("text", ((Note) item).getText());
                notesPart.add(map);
            }
        }
        Map<String, Object> main = new LinkedHashMap<String, Object>();
        main.put("lists", listsPart);
        main.put("tasks", tasksPart);
        main.put("notes", notesPart);
        String json = new Jsonizer().write(main);
        json = json.replace("},", "},\n");
        json = json.replace("],", "],\n");
        config.saveConfig(json);
    }
    
    protected String getUserPath() {
        return basePath + userId + "/";
    }
    
    private List<SyncEvent> calculateChanges(final Map<String, SyncEvent> clientMap, final List<SyncEvent> serverList) {
        List<SyncEvent> changes = new ArrayList<SyncEvent>(clientMap.values());
        for (SyncEvent update : serverList) {
            String itemId = update.getItemId();
            SyncEvent change = clientMap.get(itemId);
            if (change != null && !change.after(update)) {
                changes.remove(change);
            }
        }
        for (SyncEvent event : clientMap.values()) {
            if (event.stale(ignoreLimit)) {
                changes.remove(event);
            }
        }
        return changes;
    }
    
    private String ask(final String what, final String... params) throws IOException {
        return ask(what, null, params);
    }
    
    private String ask(final String what, final InputListener listener, final String... params) throws IOException {
        if (!online) {
            throw new IOException("Offline");
        }
        URL url = new URL(serverPath + what);
        String body = params(params);
        
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setUseCaches(false);
        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        urlConnection.setRequestProperty("Content-Length", "" + body.length());
        urlConnection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows 98; DigExt)");
        
        DataOutputStream outStream = new DataOutputStream(urlConnection.getOutputStream());
        outStream.writeBytes(body);
        outStream.flush();
        outStream.close();
        
        int code = urlConnection.getResponseCode();
        if (code != 200) {
            throw new IOException(urlConnection.getResponseMessage());
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        InputListener l = listener;
        if (l == null) {
            l = getDefaultListener();
        }
        try {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (!l.onNextLine(inputLine)) {
                    break;
                }
            }
        } finally {
            in.close();
        }
        return l.getInputString();
    }
    
    private String toRaw(final List<SyncEvent> events) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (SyncEvent event : events) {
            if (i > 0) {
                sb.append('\n');
            }
            sb.append(event.toString());
            i++;
        }
        return sb.toString();
    }
    
    protected void writeFile(final String input, final File userDir) throws IOException {
        ItemParser parser = new ItemParser(input);
        String fileName = parser.getFileName();
        if (fileName != null) {
            File file = new File(userDir, fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            String body = parser.getFileBody();
            if (body != null) {
                FileWriter writer = new FileWriter(file);
                writer.write(body);
                writer.flush();
                writer.close();
            }
        }
    }
    
    private InputListener getDefaultListener() {
        return new InputListener() {
            private final StringBuilder sb = new StringBuilder();
            
            public boolean onNextLine(final String line) {
                sb.append(line);
                return true;
            }
            
            public String getInputString() {
                return sb.toString();
            }
        };
    }
    
    private String params(final String... params) {
        if (params == null) {
            return null;
        }
        final int size = params.length;
        if (size <= 0) {
            return authParams();
        }
        return authParams() + '&' + buildParams(params);
    }
    
    private String authParams() {
        return buildParams("email", email, "password", password);
    }
    
    private String buildParams(final String... params) {
        final int size = params.length;
        if (size % 2 > 0) {
            throw new IllegalArgumentException("Should be paired");
        }
        if (size <= 0) {
            return "";
        }
        final StringBuilder sb = new StringBuilder(size * 16);
        for (int i = 0; i < size;) {
            if (i > 0) {
                sb.append('&');
            }
            String p = params[i];
            if (p != null) {
                sb.append(encode(p));
            } else {
                break;
            }
            i++;
            p = params[i];
            if (i < size && p != null) {
                sb.append('=');
                sb.append(encode(p));
            } else {
                break;
            }
            i++;
        }
        return sb.toString();
    }
    
    private String encode(final String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return text;
        }
    }
    
    protected boolean isError(final String input) {
        return "-1".equals(input);
    }
    
    private void showAuthFailedStatus() {
        if (statusListener != null) {
            statusListener.onStatusChange("Sync is offline: Authorization failed for " + email);
        }
    }
    
    private void showInitializedStatus() {
        if (statusListener != null) {
            statusListener.onStatusChange("Sync is online: Initialized sucessfully into " + userId);
        }
    }
    
    private void showSynchronizedStatus() {
        if (statusListener != null && !success) {
            statusListener.onStatusChange("Sync is online: Synchronized sucessfully as " + email);
        }
        success = true;
    }
    
    private void showSynchronizedFailedStatus(final String message) {
        success = false;
        if (statusListener != null) {
            statusListener.onStatusChange("Sync is online: Synchronization failed, cause:" + message);
        }
    }
    
}
