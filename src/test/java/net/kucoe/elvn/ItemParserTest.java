package net.kucoe.elvn;

import static org.junit.Assert.*;

import java.util.Date;

import net.kucoe.elvn.sync.ItemParser;
import net.kucoe.elvn.timer.TaskStage;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class ItemParserTest {
    
    @Test
    public void testNote() {
        ItemParser parser = new ItemParser("01361710253287hello all note");
        assertTrue("Result type is wrong", parser.getItem() instanceof Note);
        Note item = (Note) parser.getItem();
        assertEquals(new Long(1361710253287l), item.getId());
        assertEquals("hello all note", item.getText());
    }
    
    @Test
    public void testNoteUnicode() {
        ItemParser parser = new ItemParser("01361710253287Привіт з тестового повідомлення");
        assertTrue("Result type is wrong", parser.getItem() instanceof Note);
        Note item = (Note) parser.getItem();
        assertEquals(new Long(1361710253287l), item.getId());
        assertEquals("Привіт з тестового повідомлення", item.getText());
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void testTask() {
        ItemParser parser = new ItemParser("11361710253287g+1361712733634hello all task");
        assertTrue("Result type is wrong", parser.getItem() instanceof Task);
        Task item = (Task) parser.getItem();
        assertEquals(new Long(1361710253287l), item.getId());
        assertEquals("green", item.getList());
        assertTrue(item.isPlanned());
        assertNotNull(item.getCompletedOn());
        assertEquals(32, item.getCompletedOn().getMinutes());
        assertEquals("hello all task", item.getText());
    }
    
    @Test
    public void testTaskNotCompleted() {
        ItemParser parser = new ItemParser("11361710253287g-0hello all task");
        assertTrue("Result type is wrong", parser.getItem() instanceof Task);
        Task item = (Task) parser.getItem();
        assertEquals(new Long(1361710253287l), item.getId());
        assertEquals("green", item.getList());
        assertFalse(item.isPlanned());
        assertNull(item.getCompletedOn());
        assertEquals("hello all task", item.getText());
    }
    
    @Test
    public void testList() {
        ItemParser parser = new ItemParser("2gmy list");
        assertTrue("Result type is wrong", parser.getItem() instanceof List);
        List item = (List) parser.getItem();
        assertEquals("green", item.getColor());
        assertEquals("my list", item.getLabel());
    }
    
    @Test
    public void testTimer() {
        ItemParser parser = new ItemParser("32061361710253287");
        assertTrue("Result type is wrong", parser.getItem() instanceof TimerInfo);
        TimerInfo item = (TimerInfo) parser.getItem();
        assertEquals(TaskStage.Work2, item.getStage());
        assertEquals(6, item.getMinutes());
        assertEquals(new Long(1361710253287l), item.getRunId());
    }
    
    @Test
    public void testEmptyTimer() {
        ItemParser parser = new ItemParser("3-");
        assertTrue("Result type is wrong", parser.getItem() instanceof TimerInfo);
        TimerInfo item = (TimerInfo) parser.getItem();
        assertNull(item.getStage());
        assertEquals(0, item.getMinutes());
        assertNull(item.getRunId());
    }
    
    @Test
    public void testRawNote() {
        Note note = new Note(1111l, "aaaa");
        assertEquals("01111aaaa", ItemParser.toRaw(note));
    }
    
    @Test
    public void testRawTask() {
        Note note = new Task(1111l, "green", "aaaa", false, new Date(0));
        assertEquals("11111g-0aaaa", ItemParser.toRaw(note));
    }
    
    @Test
    public void testRawList() {
        List list = new List(ListColor.Green, "aaaa");
        assertEquals("2gaaaa", ItemParser.toRaw(list));
    }
    
    @Test
    public void testRawTimer() {
        TimerInfo timerInfo = new TimerInfo(1111l, TaskStage.Break, 6);
        assertEquals("35061111", ItemParser.toRaw(timerInfo));
    }
    
    @Test
    public void testRawEmptyTimer() {
        TimerInfo timerInfo = new TimerInfo(null, null, 0);
        assertEquals("3-", ItemParser.toRaw(timerInfo));
    }
}
