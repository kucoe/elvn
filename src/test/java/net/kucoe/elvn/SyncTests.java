package net.kucoe.elvn;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import net.kucoe.elvn.sync.Sync;
import net.kucoe.elvn.sync.SyncStatusListener;

import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class SyncTests extends AbstractConfigTest {
    
    ConfigMock noKeyConfig;
    
    class ConfigSyncMock extends ConfigMock {
        @Override
        public String getSyncConfig() throws IOException {
            return "email=becevka@mail.ru";
        }
    }
    
    class ConfigSyncNoKeyMock extends ConfigMock {
        @Override
        public String getSyncConfig() throws IOException {
            return "email=becevka@ya.ru\nnokey=true";
        }
    }
    
    class SyncStatusListenerMock implements SyncStatusListener {
        
        @Override
        public void onStatusChange(final String status) {
            System.out.println(status);
        }
        
        @Override
        public String promptForPassword(final String prompt) {
            return "aaa";
        }
        
    }
    
    class SyncStatusListenerMockFail implements SyncStatusListener {
        
        @Override
        public void onStatusChange(final String status) {
            System.out.println(status);
        }
        
        @Override
        public String promptForPassword(final String prompt) {
            return "bbb";
        }
        
    }
    
    @Before
    public void setUp() {
        super.setUp();
        config = new ConfigSyncMock();
        config.getConfigPath();
        noKeyConfig = new ConfigSyncNoKeyMock();
        noKeyConfig.getConfigPath();
    }
    
    @Test
    public void testAuth() throws Exception {
        Sync sync = config.getSync();
        assertNotNull(sync);
        sync.setStatusListener(new SyncStatusListenerMock());
        sync.pull();
        File dir = new File(config.getBasePath());
        assertTrue(dir.exists());
        File key = new File(dir, "sync.key");
        assertTrue(key.exists());
        assertTrue(sync.isAuthSucceed());
    }
    
    @Test
    public void testAuthFail() throws Exception {
        Sync sync = config.getSync();
        assertNotNull(sync);
        sync.setStatusListener(new SyncStatusListenerMockFail());
        sync.pull();
        File dir = new File(config.getBasePath());
        assertTrue(dir.exists());
        File key = new File(dir, "sync.key");
        assertFalse(key.exists());
        assertFalse(sync.isAuthSucceed());
    }
    
    @Test
    public void testAuthNoKey() throws Exception {
        Sync sync = noKeyConfig.getSync();
        assertNotNull(sync);
        sync.setStatusListener(new SyncStatusListenerMock());
        sync.pull();
        File dir = new File(noKeyConfig.getBasePath());
        assertTrue(dir.exists());
        File key = new File(dir, "sync.key");
        assertFalse(key.exists());
        assertTrue(sync.isAuthSucceed());
    }
    
}