package net.kucoe.elvn.lang;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import net.kucoe.elvn.ListColor;

import org.junit.*;

@SuppressWarnings("javadoc")
public class SyncTest extends AbstractConfigTest {
    
    class ConfigSyncMock extends ConfigMock {
        @Override
        public String getSyncConfig() throws IOException {
            return "{\"email\":\"becevka@mail.ru\",\"password\":\"64555\"}";
        }
    }
    
    @Before
    public void setUp() {
        super.setUp();
        config = new ConfigSyncMock();
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
        config.getList(ListColor.Today);
        Thread.sleep(10000);
        File dir = new File(getSyncDir());
        assertTrue(dir.exists());
        File listFile = new File(dir, "blue");
        assertTrue(listFile.exists());
    }
    
    private void deleteDir() {
        File dir = new File(getSyncDir());
        del(dir);
    }
    
    protected String getSyncDir() {
        return config.getConfigPath().replace("test.json", "1362771947351");
    }
    
    private void del(File file) {
        if (file.isDirectory()) {
            for (File c : file.listFiles())
                del(c);
        }
        file.delete();
    }
    
}
