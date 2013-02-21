package net.kucoe.elvn.controls;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import net.kucoe.elvn.CommandTool;
import net.kucoe.elvn.Task;

/**
 * Task cell that renders completed task.
 * 
 * @author Vitaliy Basyuk
 */
public class CompletedTaskCell extends TaskCell {
    
    private Button trashButton;
    
    /**
     * Constructs CompletedTaskCell.
     * 
     * @param tool
     */
    public CompletedTaskCell(final CommandTool tool) {
        super(tool);
    }
    
    @Override
    public void updateItem(final Task item, final boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
            label.getStyleClass().add("stroked");
            toolBar.getItems().remove(colorPicker);
            toolBar.getItems().remove(planButton);
            toolBar.getItems().remove(timerButton);
            trashButton = new Button();
            trashButton.setId("trash-button");
            trashButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream(
                    "/net/kucoe/elvn/resources/icons/trash.png"))));
            trashButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(final ActionEvent event) {
                    tool.removeTask(getItem());
                }
            });
            trashButton.setMaxHeight(30);
            toolBar.getItems().add(trashButton);
        }
    }
}
