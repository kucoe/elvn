package net.kucoe.elvn.lang;

import static org.junit.Assert.*;
import net.kucoe.elvn.lang.result.*;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class ELTest {
    
    @Test
    public void testSwitchListColor() {
        ELResult process = EL.process("/all");
        assertTrue("Result type is wrong", process instanceof SwitchListColor);
        assertEquals("all", ((SwitchListColor) process).color);
    }
    
    @Test
    public void testSwitchListLabel() {
        ELResult process = EL.process("/alla");
        assertTrue("Result type is wrong", process instanceof SwitchListLabel);
        assertEquals("alla", ((SwitchListLabel) process).label);
    }
    
    @Test
    public void testSwitchListEditor() {
        ELResult process = EL.process("&");
        assertTrue("Result type is wrong", process instanceof SwitchListEdit);
    }
    
    @Test
    public void testSwitchNotes() {
        ELResult process = EL.process("@");
        assertTrue("Result type is wrong", process instanceof SwitchNotes);
    }
    
    @Test
    public void testSwitchSync() {
        ELResult process = EL.process("%");
        assertTrue("Result type is wrong", process instanceof SwitchSync);
    }
    
    @Test
    public void testSwitchTimer() {
        ELResult process = EL.process("$");
        assertTrue("Result type is wrong", process instanceof SwitchTimer);
    }
    
    @Test
    public void testSwitchStatus() {
        ELResult process = EL.process("!");
        assertTrue("Result type is wrong", process instanceof SwitchStatus);
    }
    
    @Test
    public void testSwitchWrongNote() {
        ELResult process = EL.process("@s");
        assertTrue("Result type is wrong", process instanceof TaskResult);
        assertEquals("@s", ((TaskResult) process).text);
    }
    
    @Test
    public void testSwitchWrongSync() {
        ELResult process = EL.process("%s");
        assertTrue("Result type is wrong", process instanceof TaskResult);
        assertEquals("%s", ((TaskResult) process).text);
    }
    
    @Test
    public void testSwitchWrongListEditor() {
        ELResult process = EL.process("&s");
        assertTrue("Result type is wrong", process instanceof TaskResult);
    }
    
    @Test
    public void testSwitchWrongTimer() {
        ELResult process = EL.process("$sa");
        assertTrue("Result type is wrong", process instanceof TaskResult);
        assertEquals("$sa", ((TaskResult) process).text);
    }
    
    @Test
    public void testSwitchWrongStatus() {
        ELResult process = EL.process("!s");
        assertTrue("Result type is wrong", process instanceof TaskResult);
        assertEquals("!s", ((TaskResult) process).text);
    }
    
    @Test
    public void testSwitchTimerCommand() {
        ELResult process = EL.process("$:");
        assertTrue("Result type is wrong", process instanceof TimerCommand);
        assertEquals(":", ((TimerCommand) process).command);
    }
    
    @Test
    public void testSearchTask() {
        ELResult process = EL.process("?alla");
        assertTrue("Result type is wrong", process instanceof SearchTask);
        assertEquals("alla", ((SearchTask) process).query);
    }
    
    @Test
    public void testLocateTask() {
        ELResult process = EL.process("#2");
        assertTrue("Result type is wrong", process instanceof LocateTask);
        assertEquals(2, ((LocateTask) process).position);
        assertNull(((LocateTask) process).text);
    }
    
    @Test
    public void testWrongLocateTask() {
        ELResult process = EL.process("#a");
        assertTrue("Result type is wrong", process instanceof TaskResult);
        assertEquals("#a", ((TaskResult) process).text);
    }
    
    @Test
    public void testCreateTask() {
        ELResult process = EL.process("green:text");
        assertTrue("Result type is wrong", process instanceof TaskResult);
        assertEquals("green", ((TaskResult) process).list);
        assertEquals("text", ((TaskResult) process).text);
    }
    
    @Test
    public void testCreateTaskSimple() {
        ELResult process = EL.process("text");
        assertTrue("Result type is wrong", process instanceof TaskResult);
        assertNull(((TaskResult) process).list);
        assertEquals("text", ((TaskResult) process).text);
    }
    
    @Test
    public void testEditTaskSearch() {
        ELResult process = EL.process("?correct=correct%fixed");
        assertTrue("Result type is wrong", process instanceof SearchTask);
        assertNull(((SearchTask) process).list);
        assertEquals("correct%fixed", ((SearchTask) process).text);
        assertEquals("correct", ((SearchTask) process).query);
    }
    
    @Test
    public void testEditTaskLocate() {
        ELResult process = EL.process("#2=Need to update examples");
        assertTrue("Result type is wrong", process instanceof LocateTask);
        assertEquals(2, ((LocateTask) process).position);
        assertNull(((TaskResult) process).list);
        assertEquals("Need to update examples", ((TaskResult) process).text);
    }
    
    @Test
    public void testMoveTask() {
        ELResult process = EL.process("#2=blue:");
        assertTrue("Result type is wrong", process instanceof LocateTask);
        assertEquals(2, ((LocateTask) process).position);
        assertEquals("blue", ((TaskResult) process).list);
        assertEquals(null, ((TaskResult) process).text);
    }
    
    @Test
    public void testCommandTaskLocate() {
        ELResult process = EL.process("#2>");
        assertTrue("Result type is wrong", process instanceof TaskCommand);
        assertEquals(2, ((LocateTask) process).position);
        assertNull(((TaskResult) process).text);
        assertNull(((TaskResult) process).list);
        assertEquals(">", ((TaskCommand) process).command);
    }
    
    @Test
    public void testCommandTaskLocateRange() {
        ELResult process = EL.process("#4-2>");
        assertTrue("Result type is wrong", process instanceof GroupTaskCommand);
        assertEquals(3, ((GroupTaskCommand) process).positions.length);
        assertEquals(">", ((TaskCommand) process).command);
    }
    
    @Test
    public void testCommandTaskLocateBigRange() {
        ELResult process = EL.process("#2-15>");
        assertTrue("Result type is wrong", process instanceof GroupTaskCommand);
        assertEquals(14, ((GroupTaskCommand) process).positions.length);
        assertEquals(">", ((TaskCommand) process).command);
    }
    
    @Test
    public void testCommandTaskLocateGroup() {
        ELResult process = EL.process("#2,4,5>");
        assertTrue("Result type is wrong", process instanceof GroupTaskCommand);
        assertEquals(3, ((GroupTaskCommand) process).positions.length);
        assertEquals(">", ((TaskCommand) process).command);
    }
    
    @Test
    public void testCommandTaskLocateAll() {
        ELResult process = EL.process("#*>");
        assertTrue("Result type is wrong", process instanceof GroupTaskCommand);
        assertEquals(">", ((TaskCommand) process).command);
        assertNull(((GroupTaskCommand) process).positions);
    }
    
    @Test
    public void testCommandTaskUnplan() {
        ELResult process = EL.process("#4-");
        assertTrue("Result type is wrong", process instanceof TaskCommand);
        assertEquals("-", ((TaskCommand) process).command);
    }
}
