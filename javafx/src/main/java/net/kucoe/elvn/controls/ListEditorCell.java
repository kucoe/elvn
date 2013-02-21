package net.kucoe.elvn.controls;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import net.kucoe.elvn.*;

/**
 * {@link ListEditorView} cell.
 * 
 * @author Vitaliy Basyuk
 */
public class ListEditorCell extends ListCell<List> {
    
    protected ToolBar toolBar;
    protected Label label;
    protected CommandTool tool;
    protected Rectangle rect;
    
    /**
     * Constructs ListEditorCell.
     * 
     * @param tool
     */
    public ListEditorCell(final CommandTool tool) {
        this.tool = tool;
        setAlignment(Pos.CENTER_LEFT);
        setContentDisplay(ContentDisplay.LEFT);
        setGraphic(toolBar);
        
    }
    
    @Override
    public void updateItem(final List item, final boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
            toolBar = new ToolBar();
            toolBar.setMinHeight(35);
            toolBar.setPrefHeight(35);
            toolBar.setMaxHeight(35);
            toolBar.setMaxWidth(Double.MAX_VALUE);
            
            rect = new Rectangle(20, 20);
            rect.setArcHeight(10);
            rect.setArcWidth(10);
            label = new Label();
            label.managedProperty().bind(label.visibleProperty());
            EventHandler<MouseEvent> editHandler = new EventHandler<MouseEvent>() {
                @Override
                public void handle(final MouseEvent event) {
                    tool.showItem(getIndex());
                }
            };
            label.setOnMouseClicked(editHandler);
            toolBar.getItems().add(rect);
            toolBar.getItems().add(label);
            
            rect.setFill(Color.web(ListColor.color(item.getColor()).getHex()));
            label.setText(item.getLabel());
            setGraphic(toolBar);
        } else {
            setGraphic(null);
            setText(null);
        }
    }
    
}
