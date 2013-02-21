package net.kucoe.elvn.controls;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import net.kucoe.elvn.*;

/**
 * Task cell that renders task.
 * 
 * @author Vitaliy Basyuk
 */
public class TaskCell extends ListCell<Task> {
    
    protected ToolBar toolBar;
    protected CheckBox checkBox;
    protected Button planButton;
    protected Button timerButton;
    protected Label label;
    protected ComboBox<ListColor> colorPicker;
    protected final CommandTool tool;
    
    /**
     * Constructs {@link TaskCell}.
     * 
     * @param tool
     */
    public TaskCell(final CommandTool tool) {
        this.tool = tool;
        setAlignment(Pos.CENTER_LEFT);
        setContentDisplay(ContentDisplay.LEFT);
        setGraphic(toolBar);
        
    }
    
    @Override
    public void updateItem(final Task item, final boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
            toolBar = new ToolBar();
            toolBar.setMinHeight(35);
            toolBar.setPrefHeight(35);
            toolBar.setMaxHeight(35);
            toolBar.setMaxWidth(Double.MAX_VALUE);
            
            checkBox = new CheckBox();
            checkBox.setSelected(item.getCompletedOn() != null);
            checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(final ObservableValue<? extends Boolean> value, final Boolean oldVal,
                    final Boolean newVal) {
                    Task task = getItem();
                    if (newVal) {
                        tool.completeTask(task);
                    } else {
                        tool.uncompleteTask(task);
                    }
                }
            });
            
            colorPicker = new ColorBox(null);
            ListColor[] values = ListColor.values();
            for (ListColor color : values) {
                if (!ListColor.White.equals(color) && !ListColor.Stroke.equals(color) && !ListColor.Teal.equals(color)
                        && tool.getList(color) != null) {
                    colorPicker.getItems().add(color);
                }
            }
            colorPicker.setValue(ListColor.color(item.getList()));
            colorPicker.valueProperty().addListener(new ChangeListener<ListColor>() {
                @Override
                public void changed(final ObservableValue<? extends ListColor> ov, final ListColor oldValue,
                    final ListColor newValue) {
                    Task item = getItem();
                    if (item != null && !item.getList().equals(newValue)) {
                        tool.updateTask(item, newValue.toString(), item.getText());
                    }
                }
            });
            
            planButton = new Button();
            planButton.setId("plan-button");
            if (item.isPlanned()) {
                planButton.getStyleClass().add("today");
            } else {
                planButton.getStyleClass().remove("today");
            }
            planButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream(
                    "/net/kucoe/elvn/resources/icons/plan.png"))));
            planButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(final ActionEvent event) {
                    Task task = getItem();
                    if (task.isPlanned()) {
                        tool.unplanTask(task);
                        planButton.getStyleClass().remove("today");
                    } else {
                        tool.planTask(task);
                        planButton.getStyleClass().add("today");
                    }
                }
            });
            planButton.setMaxHeight(30);
            
            label = new Label();
            label.setText(item.getText());
            EventHandler<MouseEvent> editHandler = new EventHandler<MouseEvent>() {
                @Override
                public void handle(final MouseEvent event) {
                    tool.showItem(getIndex());
                }
            };
            label.setOnMouseClicked(editHandler);
            
            toolBar.getItems().add(checkBox);
            toolBar.getItems().add(planButton);
            toolBar.getItems().add(colorPicker);
            toolBar.getItems().add(label);
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            toolBar.getItems().add(spacer);
            
            timerButton = new Button();
            timerButton.setId("time-button");
            timerButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream(
                    "/net/kucoe/elvn/resources/icons/timer.png"))));
            timerButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(final ActionEvent event) {
                    Task task = getItem();
                    tool.runTask(task);
                }
            });
            timerButton.setMaxHeight(30);
            toolBar.getItems().add(timerButton);
            
            setGraphic(toolBar);
        } else {
            setGraphic(null);
            setText(null);
        }
    }
}
