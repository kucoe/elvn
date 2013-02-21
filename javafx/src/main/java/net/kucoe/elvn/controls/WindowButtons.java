package net.kucoe.elvn.controls;

import net.kucoe.elvn.timer.Timer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;


/**
 * Horizontal box with 3 small buttons for window close, minimize and maximize.
 */
public class WindowButtons extends HBox {
    private Rectangle2D backupWindowBounds = null;
    private boolean maximized = false;
    
    /**
     * Constructs WindowButtons.
     * 
     * @param stage
     * @param onMinimize
     */
    public WindowButtons(final Stage stage, final EventHandler<ActionEvent> onMinimize) {
        super(4);
        // create buttons
        Button closeBtn = new Button();
        closeBtn.setId("window-close");
        closeBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent actionEvent) {
                Timer.shutdown();
                SysTray.showInTray(false);
                Platform.exit();
            }
        });
        Button minBtn = new Button();
        minBtn.setId("window-min");
        minBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent actionEvent) {
                if (onMinimize != null) {
                    onMinimize.handle(actionEvent);
                    if (!actionEvent.isConsumed()) {
                        stage.setIconified(true);
                    }
                } else {
                    stage.setIconified(true);
                }
                
            }
        });
        Button maxBtn = new Button();
        maxBtn.setId("window-max");
        maxBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent actionEvent) {
                toogleMaximized(stage);
            }
        });
        getChildren().addAll(maxBtn, minBtn, closeBtn);
    }
    
    /**
     * Change maximized.
     * 
     * @param stage
     */
    public void toogleMaximized(final Stage stage) {
        final Screen screen = Screen.getScreensForRectangle(stage.getX(), stage.getY(), 1, 1).get(0);
        if (maximized) {
            maximized = false;
            if (backupWindowBounds != null) {
                stage.setX(backupWindowBounds.getMinX());
                stage.setY(backupWindowBounds.getMinY());
                stage.setWidth(backupWindowBounds.getWidth());
                stage.setHeight(backupWindowBounds.getHeight());
            }
        } else {
            maximized = true;
            backupWindowBounds = new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
            stage.setX(screen.getVisualBounds().getMinX());
            stage.setY(screen.getVisualBounds().getMinY());
            stage.setWidth(screen.getVisualBounds().getWidth());
            stage.setHeight(screen.getVisualBounds().getHeight());
        }
    }
    
    /**
     * Returns whether stage is maximized.
     * 
     * @return boolean
     */
    public boolean isMaximized() {
        return maximized;
    }
}
