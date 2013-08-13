package net.kucoe.elvn.sync;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.*;

import net.kucoe.elvn.util.Config;

/**
 * Remote sync support
 * 
 * @author Vitaliy Basyuk
 */
public class Sync {
    
    private static final String DEFAULT_SERVER_PATH = "https://kucoe.net/elvn/";
    
    private final String email;
    private final boolean noKey;
    private final String serverPath;
    private final String basePath;
    private final Config config;
    
    private SyncStatusListener statusListener;
    private boolean auth;
    private int tries;
    
    private static MessageDigest digester;
    private static final char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
            'f' };
    
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
            digester = MessageDigest.getInstance("SHA-512");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Constructs Sync.
     * 
     * @param email
     * @param noKey
     * @param serverPath
     * @param basePath
     * @param config {@link Config}
     */
    public Sync(final String email, final boolean noKey, final String serverPath, final String basePath,
            final Config config) {
        this.email = email;
        this.noKey = noKey;
        this.serverPath = serverPath == null ? DEFAULT_SERVER_PATH : serverPath;
        this.basePath = basePath;
        this.config = config;
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
     * Reverts to the last version (before previous pull)
     * 
     * @throws IOException
     */
    public void restore() throws IOException {
        String path = basePath + "config.bak";
        File file = new File(path);
        if (file.exists()) {
            config.saveConfig(read(file));
        }
    }
    
    /**
     * Gets remote content
     * 
     * @throws IOException
     */
    public synchronized void pull() throws IOException {
        tries = 0;
        backup();
        auth();
        try {
            String remote = call("pull");
            if (remote.isEmpty()) {
                push();
            } else if ("false".equals(remote)) {
                showAuthFailedStatus();
                reauth();
            } else {
                config.saveConfig(remote);
                showSynchronizedStatus();
            }
        } catch (Exception e) {
            showSynchronizedFailedStatus(e.getMessage());
        }
    }
    
    /**
     * Pushes content to server.
     * 
     * @throws IOException
     */
    public synchronized void push() throws IOException {
        tries = 0;
        auth();
        try {
            String remote = call("push", "config", config.getConfig());
            if ("false".equals(remote)) {
                showAuthFailedStatus();
                reauth();
            } else {
                showSynchronizedStatus();
            }
        } catch (Exception e) {
            showSynchronizedFailedStatus(e.getMessage());
        }
    }
    
    private void auth() throws IOException {
        if (!auth) {
            auth = Boolean.parseBoolean(call("auth"));
            if (!auth) {
                showAuthFailedStatus();
                reauth();
            } else {
                tries = 0;
            }
        }
    }
    
    private void backup() throws IOException {
        String path = basePath + "config.bak";
        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        write(file, config.getConfig());
    }
    
    private boolean userExists() throws IOException {
        return Boolean.parseBoolean(call("auth", "email", email));
    }
    
    private String askForPassword() throws IOException {
        if (userExists()) {
            return statusListener.promptForPassword("Please enter your password:");
        }
        return statusListener.promptForPassword("Please choose password to register with:");
    }
    
    private void sendKey(final String password, final String key) throws IOException {
        String remote = call("auth", "email", email, "password", password, "key", key);
        if (remote.equals("false")) {
            showAuthFailedStatus();
            reauth();
        }
    }
    
    private void reauth() throws IOException {
        tries++;
        auth = false;
        String path = basePath + "sync.key";
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        if (tries <= 10) {
            auth();
        }
    }
    
    private String key() throws IOException {
        String path = basePath + "sync.key";
        File file = new File(path);
        if (!file.exists()) {
            String password = askForPassword();
            String key = newKey(password);
            file.createNewFile();
            write(file, key);
            key = getKey(key, getCreationTime(file));
            sendKey(password, key);
            return key;
        }
        String content = read(file);
        return getKey(content, getCreationTime(file));
    }
    
    private String getKey(final String key, final long time) throws UnsupportedEncodingException {
        String str = key.substring(0, 64) + time + key.substring(64);
        digester.reset();
        byte[] digest = digester.digest(str.getBytes("UTF-8"));
        return toHex(digest);
    }
    
    private String newKey(final String password) throws UnsupportedEncodingException {
        digester.reset();
        byte[] digest = digester.digest(password.getBytes("UTF-8"));
        return toHex(digest);
    }
    
    private long getCreationTime(final File file) throws IOException {
        BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        return attrs.creationTime().to(TimeUnit.MILLISECONDS);
    }
    
    private static String toHex(final byte[] bytes) {
        StringBuilder result = new StringBuilder(1024);
        for (byte b : bytes) {
            int lowBits = b & 0x0f;
            int highBits = (b >> 4) & 0x0f;
            result.append(hexChars[highBits]);
            result.append(hexChars[lowBits]);
        }
        return result.toString();
    }
    
    private String call(final String method, final String... params) throws IOException {
        URL url = new URL(serverPath + method);
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
        final StringBuilder sb = new StringBuilder();
        try {
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
        } finally {
            in.close();
        }
        return sb.toString();
    }
    
    private String params(final String... params) throws IOException {
        if (params == null) {
            return null;
        }
        final int size = params.length;
        if (size <= 0) {
            return authParams();
        }
        String built = buildParams(params);
        if (built.contains("key=") || built.contains("email=")) {
            return built;
        }
        return authParams() + '&' + built;
    }
    
    private String authParams() throws IOException {
        if (noKey) {
            return buildParams("email", email, "password", askForPassword());
        }
        return buildParams("email", email, "key", key());
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
            return text;
        }
    }
    
    private String read(final File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String str;
        StringBuilder sb = new StringBuilder();
        String newLine = "";
        while ((str = reader.readLine()) != null) {
            sb.append(newLine);
            sb.append(str);
            newLine = "\n";
        }
        reader.close();
        return sb.toString();
    }
    
    private void write(final File file, final String content) throws IOException {
        FileWriter writer = new FileWriter(file);
        writer.write(content);
        writer.flush();
        writer.close();
    }
    
    private void showAuthFailedStatus() {
        if (statusListener != null) {
            statusListener.onStatusChange("Authorization failed for " + email);
        }
    }
    
    private void showSynchronizedStatus() {
        if (statusListener != null) {
            statusListener.onStatusChange("Synchronized sucessfully as " + email);
        }
    }
    
    private void showSynchronizedFailedStatus(final String message) {
        if (statusListener != null) {
            statusListener.onStatusChange("Synchronization failed, cause:" + message);
        }
    }
    
    /**
     * Whether auth was successful.
     * 
     * @return boolean
     */
    public boolean isAuthSucceed() {
        return auth;
    }
    
}
