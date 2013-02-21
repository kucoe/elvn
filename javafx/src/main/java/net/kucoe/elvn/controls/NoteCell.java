package net.kucoe.elvn.controls;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import net.kucoe.elvn.CommandTool;
import net.kucoe.elvn.Note;

/**
 * {@link Note} cell.
 * 
 * @author Vitaliy Basyuk
 */
public class NoteCell extends ListCell<Note> {
    
    protected ToolBar toolBar;
    protected Label label;
    protected final CommandTool tool;
    private Button trashButton;
    
    /**
     * Constructs NoteCell.
     * 
     * @param tool
     */
    public NoteCell(final CommandTool tool) {
        this.tool = tool;
        setAlignment(Pos.CENTER_LEFT);
        setContentDisplay(ContentDisplay.LEFT);
        setGraphic(toolBar);
        
    }
    
    @Override
    public void updateItem(final Note item, final boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
            toolBar = new ToolBar();
            toolBar.setMinHeight(35);
            toolBar.setPrefHeight(35);
            toolBar.setMaxHeight(35);
            toolBar.setMaxWidth(Double.MAX_VALUE);
            
            label = new Label();
            label.managedProperty().bind(label.visibleProperty());
            EventHandler<MouseEvent> editHandler = new EventHandler<MouseEvent>() {
                @Override
                public void handle(final MouseEvent event) {
                    tool.showItem(getIndex());
                }
            };
            label.setOnMouseClicked(editHandler);
            
            toolBar.getItems().add(label);
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            toolBar.getItems().add(spacer);
            
            trashButton = new Button();
            trashButton.setId("trash-button");
            trashButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream(
                    "/net/kucoe/elvn/resources/icons/trash.png"))));
            trashButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(final ActionEvent event) {
                    tool.removeNote(getItem());
                }
            });
            trashButton.setMaxHeight(30);
            toolBar.getItems().add(trashButton);
            
            label.setText(item.getText());
            setGraphic(toolBar);
        } else {
            setGraphic(null);
            setText(null);
        }
    }
}
