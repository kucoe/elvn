package net.kucoe.elvn.lang.result;

import java.util.List;

import net.kucoe.elvn.Note;
import net.kucoe.elvn.lang.ELCommand;
import net.kucoe.elvn.util.Config;
import net.kucoe.elvn.util.Display;


/**
 * Switch notes result.
 * 
 * @author Vitaliy Basyuk
 */
public class SwitchNotes extends Switch {
    @Override
    public String execute(final Display display, final Config config) throws Exception {
        List<Note> notes = config.getNotes();
        display.showNotes(notes);
        return ELCommand.Notes.el();
    }
    
}
