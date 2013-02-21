package net.kucoe.elvn.controls;

import java.util.List;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import net.kucoe.elvn.CommandTool;
import net.kucoe.elvn.Note;

/**
 * Show notes
 * 
 * @author Vitaliy Basyuk
 */
public class NoteView extends ListView<Note> {
    
    /**
     * Constructs NoteView.
     * 
     * @param tool
     * @param notes
     */
    public NoteView(final CommandTool tool, final List<Note> notes) {
        for (Note note : notes) {
            getItems().add(note);
        }
        setCellFactory(new Callback<ListView<Note>, ListCell<Note>>() {
            @Override
            public ListCell<Note> call(final ListView<Note> list) {
                return new NoteCell(tool);
            }
        });
    }
}
