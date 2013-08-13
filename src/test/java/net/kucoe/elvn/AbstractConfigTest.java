package net.kucoe.elvn;

import java.io.File;
import java.io.IOException;

import net.kucoe.elvn.timer.*;
import net.kucoe.elvn.util.*;

import org.junit.After;
import org.junit.Before;

@SuppressWarnings("javadoc")
public abstract class AbstractConfigTest {
    
    class ConfigMock extends Config {
        
        protected String getBasePath() {
            return getUserDir() + "/.elvn-test/";
        }
        
        @Override
        protected String getConfigPath() {
            checkElvnDir();
            return getBasePath() + "test.json";
        }
        
        @Override
        public String getSyncConfig() throws IOException {
            return null;
        }
    }
    
    class DisplayMock implements Display {
        
        private String currentList = ListColor.All.toString();
        
        protected String helpMessage;
        
        @Override
        public void showTasks(final java.util.List<Task> tasks) {
            // empty
        }
        
        @Override
        public void showTask(final Task task, final int position) {
            // empty
        }
        
        @Override
        public void showHelp(final String helpMessage) {
            if (this.helpMessage != null) {
                this.helpMessage = null;
            } else {
                this.helpMessage = helpMessage;
            }
        }
        
        @Override
        public void showStatus(final String status) {
            // empty
        }
        
        @Override
        public void showNotes(final java.util.List<Note> list) {
            // empty
        }
        
        @Override
        public void showNote(final Note note, final int position) {
            // empty
        }
        
        @Override
        public void showLists(final Config config) throws IOException, JsonException {
            // empty
        }
        
        @Override
        public void showList(final List list) {
            // empty
        }
        
        @Override
        public void showConfig(final String config) {
            // empty
        }
        
        @Override
        public void setCurrentList(final String current) {
            currentList = current;
        }
        
        @Override
        public String getCurrentList() {
            return currentList;
        }
    }
    
    class TimerViewMock implements TimerView {
        
        protected TaskStage stage;
        
        @Override
        public void update(final int seconds) {
            // empty
        }
        
        @Override
        public void silent() {
            // empty
        }
        
        @Override
        public void showSmall() {
            // empty
        }
        
        @Override
        public void show(final Task task, final TaskStage stage, final int seconds) {
            if (stage != null) {
                this.stage = stage;
            }
        }
        
        @Override
        public void playOnTime() {
            // empty
        }
        
        @Override
        public void playOnStart() {
            // empty
        }
        
        @Override
        public void hide() {
            // empty
        }
    }
    
    protected ConfigMock config;
    protected DisplayMock display;
    protected TimerViewMock timerView;
    
    @Before
    public void setUp() {
        config = new ConfigMock();
        File dir = new File(config.getBasePath());
        del(dir);
        display = new DisplayMock();
        Timer.setProcess(getTimerProcess());
        timerView = new TimerViewMock();
        Timer.setTimerView(timerView);
        
    }
    
    @After
    public void tearDown() {
        File dir = new File(config.getBasePath());
        del(dir);
    }
    
    private void del(final File file) {
        if (file.isDirectory()) {
            for (File c : file.listFiles()) {
                del(c);
            }
        }
        file.delete();
    }
    
    protected TimerProcess getTimerProcess() {
        return new TimerProcess() {
            
            private OnTime onTime;
            
            @Override
            public void stop() {
                // empty
            }
            
            @Override
            public void play() {
                // empty
            }
            
            @Override
            public void init(final int timeout, final TimerView view, final OnTime onTime) {
                this.onTime = onTime;
            }
            
            @Override
            public void fire(final boolean complete) {
                try {
                    onTime.onTime(complete);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            @Override
            public void cancel() {
                // empty
            }
        };
    }
}
