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
import net.kucoe.elvn.util.Config;
import net.kucoe.elvn.util.JsonException;

/**
 * Remote sync support
 * 
 * @author Vitaliy Basyuk
 */
public class Sync {
    
    private static final String DEFAULT_SERVER_PATH = "https://kucoe.net/elvn/";
    private static final long MONTH = 30 * 24 * 60 * 60 * 1000;
    
    private final String email;
    private final String password;
    private final String serverPath;
    private final int ignoreLimit;
    private final int interval;
    private final String basePath;
    private final Config config;
    private final SyncStatusListener statusListener;
    
    private String userId;
    private boolean stop;
    private boolean online;
    private boolean remoteContent;
    private Thread synchronizer;
    private final Map<String, SyncEvent> events = new HashMap<String, SyncEvent>();
    private long lastUpdate;
    
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
     * @param statusListener {@link SyncStatusListener}
     */
    public Sync(String email, String password, int ignoreLimit, int interval, String serverPath, String basePath,
            Config config, SyncStatusListener statusListener) {
        this.email = email;
        this.password = password;
        this.serverPath = serverPath == null ? DEFAULT_SERVER_PATH : serverPath;
        this.ignoreLimit = ignoreLimit;
        this.interval = interval;
        this.basePath = basePath;
        this.config = config;
        this.statusListener = statusListener;
    }
    
    /**
     * Changes service to online. Will not work if there is no connection.
     */
    public synchronized void online() {
        online = true;
    }
    
    /**
     * Changes service to go offline.
     **/
    public synchronized void offline() {
        online = false;
    }
    
    /**
     * Stop
     */
    public synchronized void stop() {
        stop = true;
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
                            Thread.sleep(millis);
                        } catch (Exception e) {
                            showSynchronizedFailedStatus();
                            e.printStackTrace();
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
    public synchronized void sync() throws IOException, JsonException {
        try {
            checkUser();
        } catch (Exception e) {
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
    }
    
    private void checkUser() throws Exception {
        userId = ask("user");
    }
    
    private void checkConfig() throws IOException, JsonException {
        File file = new File(basePath + userId + "/");
        if (!file.exists()) {
            file.mkdir();
            init(file);
        }
    }
    
    private void init(final File userDir) throws IOException, JsonException {
        ask("init", new InputListener() {
            public boolean onNextLine(String line) {
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
        this.lastUpdate = new Date().getTime();
        showInitializedStatus();
    }
    
    private void put() throws IOException, JsonException {
        List<SyncEvent> events = buildFromConfig();
        syncEvents(events);
    }
    
    private void syncEvents(List<SyncEvent> events) throws IOException {
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
        String path = userId == null ? basePath + "/events.log" : basePath + userId + "/events.log";
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
    
    private void saveEvents(String rawEvents) throws IOException {
        String path = userId == null ? basePath + "/events.log" : basePath + userId + "/events.log";
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
            if (!Done.equals(color)) {
                events.add(new SyncEvent(list, EventType.Create, null));
            }
            for (Task task : list.getTasks()) {
                events.add(new SyncEvent(task, EventType.Create, null));
            }
            events.add(new SyncEvent(new TimerInfo(null, null, 0), EventType.Create, null));
        }
        return null;
    }
    
    private List<SyncEvent> getServerUpdates() throws IOException, JsonException {
        long lastUpdate = getLastUpdate();
        final List<SyncEvent> events = new ArrayList<SyncEvent>();
        if ((new Date().getTime() - lastUpdate) > MONTH) {
            init(new File(basePath + userId + "/"));
            return events;
        }
        ask("get", new InputListener() {
            
            @Override
            public boolean onNextLine(String line) {
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
        String path = userId == null ? basePath + "/sync.log" : basePath + userId + "/sync.log";
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
        return 0;
    }
    
    private void saveLastUpdate() throws IOException {
        String path = userId == null ? basePath + "/sync.log" : basePath + userId + "/sync.log";
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
    
    private List<SyncEvent> calculateUpdates(Map<String, SyncEvent> clientMap, List<SyncEvent> serverList) {
        List<SyncEvent> updates = new ArrayList<SyncEvent>();
        for (SyncEvent event : serverList) {
            SyncEvent change = clientMap.get(event.getItemId());
            if (event.after(change)) {
                updates.add(event);
            }
        }
        return updates;
    }
    
    private void applyUpdates(List<SyncEvent> updates) throws IOException {
        File file = new File(basePath + userId + "/");
        for (SyncEvent event : updates) {
            String line = ItemParser.toRaw(event.getItem());
            writeFile(line, file);
        }
    }
    
    private List<SyncEvent> calculateChanges(Map<String, SyncEvent> clientMap, List<SyncEvent> serverList) {
        List<SyncEvent> changes = new ArrayList<SyncEvent>(clientMap.values());
        for (SyncEvent update : serverList) {
            String itemId = update.getItemId();
            SyncEvent change = clientMap.get(itemId);
            if (change != null && !change.after(update)) {
                changes.remove(change);
            }
        }
        return changes;
    }
    
    private String ask(String what, String... params) throws IOException {
        return ask(what, null, params);
    }
    
    private String ask(String what, InputListener listener, String... params) throws IOException {
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
        
        BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        InputListener l = listener;
        if (l == null) {
            l = getDefaultListener();
        }
        try {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (!listener.onNextLine(inputLine)) {
                    break;
                }
            }
        } finally {
            in.close();
        }
        return l.getInputString();
    }
    
    private String toRaw(List<SyncEvent> events) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (SyncEvent event : events) {
            if (i > 0) {
                sb.append('\n');
                sb.append(event.toString());
            }
        }
        return sb.toString();
    }
    
    protected void writeFile(String input, File userDir) throws IOException {
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
            
            public boolean onNextLine(String line) {
                sb.append(line);
                return true;
            }
            
            public String getInputString() {
                return sb.toString();
            }
        };
    }
    
    private String params(String... params) {
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
    
    private String buildParams(String... params) {
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
    
    private String encode(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return text;
        }
    }
    
    protected boolean isError(String input) {
        return "-1".equals(input);
    }
    
    private void showAuthFailedStatus() {
        if (statusListener != null) {
            statusListener.onStatusChange("Sync is offline: Authorization failed");
        }
    }
    
    private void showInitializedStatus() {
        if (statusListener != null) {
            statusListener.onStatusChange("Sync is online: Initialized sucessfully");
        }
    }
    
    private void showSynchronizedStatus() {
        if (statusListener != null) {
            statusListener.onStatusChange("Sync is online: Synchronized sucessfully");
        }
    }
    
    private void showSynchronizedFailedStatus() {
        if (statusListener != null) {
            statusListener.onStatusChange("Sync is online: Synchronization failed");
        }
    }
    
}
