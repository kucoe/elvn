package net.kucoe.elvn.controls;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import net.kucoe.elvn.*;

/**
 * Shows list of editors.
 * 
 * @author Vitaliy Basyuk
 */
public class ListEditorView extends ListView<List> {
    
    /**
     * Constructs ListEditorView.
     * 
     * @param tool
     */
    public ListEditorView(final CommandTool tool) {
        for (ListColor color : ListColor.values()) {
            if (ListColor.isSystemColor(color)) {
                continue;
            }
            List list = tool.getList(color);
            if (list == null) {
                list = new List(List.NOT_ASSIGNED, color.toString());
            }
            getItems().add(list);
        }
        setCellFactory(new Callback<ListView<List>, ListCell<List>>() {
            @Override
            public ListCell<List> call(final ListView<List> list) {
                return new ListEditorCell(tool);
            }
        });
        setEditable(true);
    }
}
