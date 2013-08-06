package net.kucoe.elvn.lang;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import net.kucoe.elvn.sync.Sync;
import net.kucoe.elvn.sync.SyncStatusListener;

import org.junit.*;

@SuppressWarnings("javadoc")
public class SyncTest extends AbstractConfigTest {
    
    ConfigMock wronConfig;
    
    class ConfigSyncMock extends ConfigMock {
        @Override
        public String getSyncConfig() throws IOException {
            return "{\"email\":\"becevka@mail.ru\",\"password\":\"aaa\"}";
        }
    }
    
    class ConfigSyncMockWrong extends ConfigMock {
        @Override
        public String getSyncConfig() throws IOException {
            return "{\"email\":\"becevka@mail.ru\",\"password\":\"bbb\"}";
        }
    }
    
    @Before
    public void setUp() {
        super.setUp();
        config = new ConfigSyncMock();
        wronConfig = new ConfigSyncMockWrong();
        deleteDir();
    }
    
    @Override
    @After
    public void tearDown() {
        super.tearDown();
        deleteDir();
    }
    
    @Test
    public void testInit() throws Exception {
        Sync sync = config.getSync();
        if (sync != null) {
            sync.setStatusListener(new SyncStatusListener() {
                public void onStatusChange(final String status) {
                    System.out.println(status);
                }
            });
            sync.start();
        }
        Thread.sleep(10000);
        File dir = new File(getSyncDir());
        assertTrue(dir.exists());
        File listFile = new File(dir, "blue");
        assertTrue(listFile.exists());
        if (sync != null) {
            sync.stop();
        }
    }
    
    @Test
    public void testAuthError() throws Exception {
        Sync sync = wronConfig.getSync();
        if (sync != null) {
            sync.setStatusListener(new SyncStatusListener() {
                public void onStatusChange(final String status) {
                    System.out.println(status);
                }
            });
            sync.start();
        }
        Thread.sleep(10000);
        File dir = new File(getSyncDir());
        assertFalse(dir.exists());
        if (sync != null) {
            sync.stop();
        }
    }
    
    private void deleteDir() {
        File dir = new File(getSyncDir());
        del(dir);
    }
    
    protected String getSyncDir() {
        return config.getConfigPath().replace("test.json", "1362993619661");
    }
    
    private void del(final File file) {
        if (file.isDirectory()) {
            for (File c : file.listFiles()) {
                del(c);
            }
        }
        file.delete();
    }
    
}
