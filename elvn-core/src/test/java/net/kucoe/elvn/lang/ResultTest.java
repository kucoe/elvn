package net.kucoe.elvn.lang;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import net.kucoe.elvn.*;
import net.kucoe.elvn.lang.result.*;
import net.kucoe.elvn.timer.*;
import net.kucoe.elvn.util.*;

import org.junit.*;

@SuppressWarnings("javadoc")
public class ResultTest {
    
    class ConfigMock extends Config {
        public String getConfigPath() {
            checkElvnDir();
            return getUserDir() + "/.elvn/test.json";
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
        public void showHelp(String helpMessage) {
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
        
        private TaskStage stage;
        
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
    
    private ConfigMock config;
    private DisplayMock display;
    private TimerViewMock timerView;
    
    @Before
    public void setUp() {
        config = new ConfigMock();
        display = new DisplayMock();
        Timer.setProcess(getTimerProcess());
        timerView = new TimerViewMock();
        Timer.setTimerView(timerView);
        
    }
    
    @After
    public void tearDown() {
        delete();
    }
    
    @Test
    public void testTaskCreate() throws Exception {
        int size = config.getList(ListColor.All).getTasks().size();
        TaskResult command = new TaskResult("b", "a");
        assertEquals(ListColor.All.toString(), command.execute(display, config));
        assertEquals(size + 1, config.getList(ListColor.All).getTasks().size());
    }
    
    @Test
    public void testTask2Note() throws Exception {
        TaskCommand command = new TaskCommand(1, "@");
        assertEquals(ELCommand.Notes.el(), command.execute(display, config));
        assertEquals(2, config.getNotes().size());
    }
    
    @Test
    public void testNote2TaskCommand() throws Exception {
        TaskCommand command = new TaskCommand(1, "g");
        display.setCurrentList(ELCommand.Notes.el());
        assertEquals(ListColor.All.toString(), command.execute(display, config));
        assertEquals(0, config.getNotes().size());
        assertEquals(2, config.getList(ListColor.Green).getTasks().size());
    }
    
    @Test
    public void testNote2Task() throws Exception {
        LocateTask command = new LocateTask("g", null, 1);
        display.setCurrentList(ELCommand.Notes.el());
        assertEquals(ListColor.All.toString(), command.execute(display, config));
        assertEquals(0, config.getNotes().size());
        assertEquals(2, config.getList(ListColor.Green).getTasks().size());
    }
    
    @Test
    public void testGroupRunTask() throws Exception {
        GroupTaskCommand command = new GroupTaskCommand(">", 1, 2);
        assertEquals(ListColor.All.toString(), command.execute(display, config));
        assertEquals(2, config.getList(ListColor.Today).getTasks().size());
        assertTrue(Timer.isRunning());
    }
    
    @Test
    public void testGroupDeleteTask() throws Exception {
        GroupTaskCommand command = new GroupTaskCommand("x", 1, 2);
        assertEquals(ListColor.All.toString(), command.execute(display, config));
        assertEquals(0, config.getList(ListColor.All).getTasks().size());
    }
    
    @Test
    public void testBigGroupCompleteTask() throws Exception {
        for (int i = 0; i < 13; i++) {
            create();
        }
        assertEquals(15, config.getList(ListColor.All).getTasks().size());
        ELResult command = EL.process("#3-15v");
        assertEquals(ListColor.All.toString(), command.execute(display, config));
        assertEquals(2, config.getList(ListColor.All).getTasks().size());
        assertEquals(14, config.getList(ListColor.Done).getTasks().size());
    }
    
    @Test
    public void testBigGroupDeleteTask() throws Exception {
        for (int i = 0; i < 13; i++) {
            create();
        }
        assertEquals(15, config.getList(ListColor.All).getTasks().size());
        ELResult command = EL.process("#3-15x");
        assertEquals(ListColor.All.toString(), command.execute(display, config));
        assertEquals(2, config.getList(ListColor.All).getTasks().size());
    }
    
    @Test
    public void testAllDeleteTask() throws Exception {
        for (int i = 0; i < 13; i++) {
            create();
        }
        assertEquals(15, config.getList(ListColor.All).getTasks().size());
        ELResult command = EL.process("#*x");
        assertEquals(ListColor.All.toString(), command.execute(display, config));
        assertEquals(0, config.getList(ListColor.All).getTasks().size());
    }
    
    @Test
    public void testBigCompletedGroupDeleteTask() throws Exception {
        testBigGroupCompleteTask();
        display.setCurrentList(ListColor.Done.toString());
        assertEquals(14, config.getList(ListColor.Done).getTasks().size());
        ELResult command = EL.process("#2-14x");
        assertEquals(ListColor.Done.toString(), command.execute(display, config));
        assertEquals(1, config.getList(ListColor.Done).getTasks().size());
    }
    
    @Test
    public void testSearchTask() throws Exception {
        for (int i = 0; i < 13; i++) {
            create();
        }
        java.util.List<Task> tasks = config.getList(ListColor.All).getTasks();
        assertEquals(15, tasks.size());
        ELResult command = EL.process("?a");
        assertEquals(ListColor.All.toString(), command.execute(display, config));
        tasks = config.getList(ListColor.All).getTasks();
        assertEquals(15, tasks.size());
        for (Task t : tasks) {
            assertTrue(t.getText().contains("a"));
        }
    }
    
    @Test
    public void testSearchReplaceTask() throws Exception {
        for (int i = 0; i < 13; i++) {
            create();
        }
        java.util.List<Task> tasks = config.getList(ListColor.All).getTasks();
        assertEquals(15, tasks.size());
        ELResult command = EL.process("?a=a%b");
        assertEquals(ListColor.All.toString(), command.execute(display, config));
        tasks = config.getList(ListColor.All).getTasks();
        assertEquals(15, tasks.size());
        for (Task t : tasks) {
            assertTrue(t.getText().contains("b"));
        }
    }
    
    @Test
    public void testSearchReplaceEmptyTask() throws Exception {
        for (int i = 0; i < 13; i++) {
            create();
        }
        java.util.List<Task> tasks = config.getList(ListColor.All).getTasks();
        assertEquals(15, tasks.size());
        ELResult command = EL.process("?a=a%");
        assertEquals(ListColor.All.toString(), command.execute(display, config));
        tasks = config.getList(ListColor.All).getTasks();
        assertEquals(15, tasks.size());
        for (Task t : tasks) {
            assertFalse(t.getText().contains("a"));
        }
    }
    
    @Test
    public void testGroupDeleteNote() throws Exception {
        GroupTaskCommand command = new GroupTaskCommand("x", 1);
        display.setCurrentList(ELCommand.Notes.el());
        assertEquals(ELCommand.Notes.el(), command.execute(display, config));
        assertEquals(0, config.getNotes().size());
    }
    
    @Test
    public void testCreateTaskWrongList() throws Exception {
        ELResult result = EL.process("greena:text");
        display.setCurrentList(ListColor.Green.toString());
        assertEquals(ListColor.Green.toString(), result.execute(display, config));
        java.util.List<Task> tasks = config.getList(ListColor.Green).getTasks();
        int size = tasks.size();
        assertEquals(2, size);
        assertEquals("greena:text", tasks.get(1).getText());
    }
    
    @Test
    public void testCreateTaskWrongList2() throws Exception {
        ELResult result = EL.process("greena:");
        display.setCurrentList(ListColor.Green.toString());
        assertEquals(ListColor.Green.toString(), result.execute(display, config));
        java.util.List<Task> tasks = config.getList(ListColor.Green).getTasks();
        int size = tasks.size();
        assertEquals(2, size);
        assertEquals("greena:", tasks.get(1).getText());
    }
    
    @Test
    public void testTextTrim() throws Exception {
        ELResult result = EL.process(" aaaa ");
        display.setCurrentList(ELCommand.Notes.el());
        assertEquals(ELCommand.Notes.el(), result.execute(display, config));
        assertEquals("aaaa", config.getNotes().get(1).getText());
    }
    
    @Test
    public void testLocateTextTrim() throws Exception {
        ELResult result = EL.process("#1= aaaa ");
        display.setCurrentList(ELCommand.Notes.el());
        assertEquals(ELCommand.Notes.el(), result.execute(display, config));
        assertEquals("aaaa", config.getNotes().get(0).getText());
    }
    
    @Test
    public void testNoteCreate() throws Exception {
        ELResult result = EL.process("aaaa");
        display.setCurrentList(ELCommand.Notes.el());
        assertEquals(ELCommand.Notes.el(), result.execute(display, config));
        assertEquals("aaaa", config.getNotes().get(1).getText());
    }
    
    @Test
    public void testNoteWithColon() throws Exception {
        ELResult result = EL.process("kuku:aaaa");
        display.setCurrentList(ELCommand.Notes.el());
        assertEquals(ELCommand.Notes.el(), result.execute(display, config));
        assertEquals("kuku:aaaa", config.getNotes().get(1).getText());
    }
    
    @Test
    public void testNoteLocateReplace() throws Exception {
        ELResult result = EL.process("#1=note%task");
        display.setCurrentList(ELCommand.Notes.el());
        assertEquals(ELCommand.Notes.el(), result.execute(display, config));
        assertEquals("Test task", config.getNotes().get(0).getText());
    }
    
    @Test
    public void testNoteSearchReplace() throws Exception {
        ELResult result = EL.process("?note=note%task");
        display.setCurrentList(ELCommand.Notes.el());
        assertEquals(ELCommand.Notes.el(), result.execute(display, config));
        assertEquals("Test task", config.getNotes().get(0).getText());
    }
    
    @Test
    public void testTaskAppend() throws Exception {
        ELResult result = EL.process("#1=+popopo");
        display.setCurrentList(ListColor.Blue.toString());
        assertEquals(ListColor.Blue.toString(), result.execute(display, config));
        String text = config.getList(ListColor.Blue).getTasks().get(0).getText();
        assertEquals("Test taskpopopo", text);
    }
    
    @Test
    public void testTaskSubtract() throws Exception {
        ELResult result = EL.process("#1=-task");
        display.setCurrentList(ListColor.Blue.toString());
        assertEquals(ListColor.Blue.toString(), result.execute(display, config));
        String text = config.getList(ListColor.Blue).getTasks().get(0).getText();
        assertEquals("Test", text);
    }
    
    @Test
    public void testNoteAppend() throws Exception {
        ELResult result = EL.process("#1=+popopo");
        display.setCurrentList(ELCommand.Notes.el());
        assertEquals(ELCommand.Notes.el(), result.execute(display, config));
        String text = config.getNotes().get(0).getText();
        assertEquals("Test notepopopo", text);
    }
    
    @Test
    public void testNoteSubtract() throws Exception {
        ELResult result = EL.process("#1=-note");
        display.setCurrentList(ELCommand.Notes.el());
        assertEquals(ELCommand.Notes.el(), result.execute(display, config));
        String text = config.getNotes().get(0).getText();
        assertEquals("Test", text);
    }
    
    @Test
    public void testTimerRun() throws Exception {
        TaskCommand command = new TaskCommand(1, ">");
        command.execute(display, config);
        assertEquals(1, config.getList(ListColor.Today).getTasks().size());
        assertTrue(Timer.isRunning());
        assertEquals(TaskStage.Elvn, timerView.stage);
    }
    
    @Test
    public void testTimerCancel() throws Exception {
        testTimerRun();
        TimerCommand command = new TimerCommand("x");
        command.execute(display, config);
        assertFalse(Timer.isRunning());
        assertEquals(1, config.getList(ListColor.Done).getTasks().size());
    }
    
    @Test
    public void testTimerDone() throws Exception {
        testTimerRun();
        TimerCommand command = new TimerCommand("v");
        command.execute(display, config);
        assertFalse(Timer.isRunning());
        assertEquals(2, config.getList(ListColor.Done).getTasks().size());
    }
    
    @Test
    public void testTimerSkip() throws Exception {
        testTimerRun();
        TimerCommand command = new TimerCommand("^");
        command.execute(display, config);
        assertEquals(TaskStage.Work, timerView.stage);
    }
    
    @Test
    public void testStatus() throws Exception {
        testTimerDone();
        assertTrue(config.getStatus().contains("Done: 1"));
    }
    
    @Test
    public void testTimerHelp() throws Exception {
        TimerCommand command = new TimerCommand("s");
        command.execute(display, config);
        assertNotNull(display.helpMessage);
    }
    
    @Test
    public void testTaskHelp() throws Exception {
        TaskCommand command = new TaskCommand(1, "s");
        command.execute(display, config);
        assertNotNull(display.helpMessage);
    }
    
    @Test
    public void testTaskGroupHelp() throws Exception {
        TaskCommand command = new GroupTaskCommand("s", 1, 2);
        command.execute(display, config);
        assertNotNull(display.helpMessage);
    }
    
    private void create() throws Exception {
        TaskResult command = new TaskResult("b", "a");
        command.execute(display, config);
        Thread.sleep(50);
    }
    
    private void delete() {
        File file = new File(config.getConfigPath());
        if (file.exists()) {
            file.delete();
        }
    }
    
    private TimerProcess getTimerProcess() {
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
