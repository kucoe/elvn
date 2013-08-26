package net.kucoe.elvn;

import static org.junit.Assert.*;
import net.kucoe.elvn.lang.EL;
import net.kucoe.elvn.lang.ELCommand;
import net.kucoe.elvn.lang.result.*;
import net.kucoe.elvn.timer.TaskStage;
import net.kucoe.elvn.timer.Timer;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class ResultTest extends AbstractConfigTest {
    
    @Test
    public void testTaskCreate() throws Exception {
        int size = config.getList(ListColor.All).getTasks().size();
        TaskResult command = new TaskResult("b", "a");
        assertEquals(ListColor.All.toString(), command.execute(display, config));
        assertEquals(size + 1, config.getList(ListColor.All).getTasks().size());
    }
    
    @Test
    public void testTask2Idea() throws Exception {
        TaskCommand command = new TaskCommand(1, "@");
        assertEquals(ELCommand.Ideas.el(), command.execute(display, config));
        assertEquals(2, config.getIdeas().size());
    }
    
    @Test
    public void testIdea2TaskCommand() throws Exception {
        TaskCommand command = new TaskCommand(1, "g");
        display.setCurrentList(ELCommand.Ideas.el());
        assertEquals(ListColor.All.toString(), command.execute(display, config));
        assertEquals(0, config.getIdeas().size());
        assertEquals(2, config.getList(ListColor.Green).getTasks().size());
    }
    
    @Test
    public void testIdea2Task() throws Exception {
        LocateTask command = new LocateTask("g", null, 1);
        display.setCurrentList(ELCommand.Ideas.el());
        assertEquals(ListColor.Green.toString(), command.execute(display, config));
        assertEquals(0, config.getIdeas().size());
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
    public void testGroupDeleteIdea() throws Exception {
        GroupTaskCommand command = new GroupTaskCommand("x", 1);
        display.setCurrentList(ELCommand.Ideas.el());
        assertEquals(ELCommand.Ideas.el(), command.execute(display, config));
        assertEquals(0, config.getIdeas().size());
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
        display.setCurrentList(ELCommand.Ideas.el());
        assertEquals(ELCommand.Ideas.el(), result.execute(display, config));
        assertEquals("aaaa", config.getIdeas().get(1).getText());
    }
    
    @Test
    public void testLocateTextTrim() throws Exception {
        ELResult result = EL.process("#1= aaaa ");
        display.setCurrentList(ELCommand.Ideas.el());
        assertEquals(ELCommand.Ideas.el(), result.execute(display, config));
        assertEquals("aaaa", config.getIdeas().get(0).getText());
    }
    
    @Test
    public void testIdeaCreate() throws Exception {
        ELResult result = EL.process("aaaa");
        display.setCurrentList(ELCommand.Ideas.el());
        assertEquals(ELCommand.Ideas.el(), result.execute(display, config));
        assertEquals("aaaa", config.getIdeas().get(1).getText());
    }
    
    @Test
    public void testIdeaWithColon() throws Exception {
        ELResult result = EL.process("kuku:aaaa");
        display.setCurrentList(ELCommand.Ideas.el());
        assertEquals(ELCommand.Ideas.el(), result.execute(display, config));
        assertEquals("kuku:aaaa", config.getIdeas().get(1).getText());
    }
    
    @Test
    public void testIdeaLocateReplace() throws Exception {
        ELResult result = EL.process("#1=idea%task");
        display.setCurrentList(ELCommand.Ideas.el());
        assertEquals(ELCommand.Ideas.el(), result.execute(display, config));
        assertEquals("Test task", config.getIdeas().get(0).getText());
    }
    
    @Test
    public void testIdeaSearchReplace() throws Exception {
        ELResult result = EL.process("?idea=idea%task");
        display.setCurrentList(ELCommand.Ideas.el());
        assertEquals(ELCommand.Ideas.el(), result.execute(display, config));
        assertEquals("Test task", config.getIdeas().get(0).getText());
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
    public void testIdeaAppend() throws Exception {
        ELResult result = EL.process("#1=+popopo");
        display.setCurrentList(ELCommand.Ideas.el());
        assertEquals(ELCommand.Ideas.el(), result.execute(display, config));
        String text = config.getIdeas().get(0).getText();
        assertEquals("Test ideapopopo", text);
    }
    
    @Test
    public void testIdeaSubtract() throws Exception {
        ELResult result = EL.process("#1=-idea");
        display.setCurrentList(ELCommand.Ideas.el());
        assertEquals(ELCommand.Ideas.el(), result.execute(display, config));
        String text = config.getIdeas().get(0).getText();
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
    public void testSyncHelp() throws Exception {
        SyncCommand command = new SyncCommand("s");
        command.execute(display, config);
        assertNotNull(display.helpMessage);
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
    
}
