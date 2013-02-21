package net.kucoe.elvn.controls;

import java.io.IOException;

import net.kucoe.elvn.Elvn;
import net.kucoe.elvn.util.Config;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;


/**
 * Configuration pop up
 * 
 * @author Vitaliy Basyuk
 */
public class ConfigDialog extends VBox {
    
    private final TextArea textArea;
    
    /**
     * Constructs ProxyDialog.
     * 
     * @param config
     * @param elvn
     * @param modalDimmer
     */
    public ConfigDialog(final Config config, final Elvn elvn, final StackPane modalDimmer) {
        setId("config-dialog");
        setSpacing(10);
        setMaxSize(430, USE_PREF_SIZE);
        // block mouse clicks
        setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent t) {
                t.consume();
            }
        });
        
        Text explanation =
                new Text("You can save your data with just simply copy and paste configurations from the box below "
                        + "or update your configuration with editing text in the box and saving it");
        explanation.setWrappingWidth(400);
        
        BorderPane explPane = new BorderPane();
        VBox.setMargin(explPane, new Insets(5, 5, 5, 5));
        explPane.setCenter(explanation);
        BorderPane.setMargin(explanation, new Insets(5, 5, 5, 5));
        
        // create title
        Label title = new Label("Elvn Configurations");
        title.setId("title");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
        getChildren().add(title);
        
        textArea = new TextArea();
        textArea.setPrefColumnCount(20);
        textArea.setTooltip(new Tooltip("Edit your configuartions here"));
        
        final Button okBtn = new Button("Save");
        okBtn.setId("save-button");
        okBtn.setDefaultButton(true);
        okBtn.setDisable(true);
        okBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent actionEvent) {
                save(config, elvn, modalDimmer);
            }
        });
        okBtn.setMinWidth(74);
        okBtn.setPrefWidth(74);
        HBox.setMargin(okBtn, new Insets(0, 8, 0, 0));
        
        ChangeListener<String> textListener = new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> ov, final String t, final String t1) {
                okBtn.setDisable(t == null || t.isEmpty() || t.equals(t1));
            }
        };
        textArea.textProperty().addListener(textListener);
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setId("cancel-button");
        cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent actionEvent) {
                elvn.hideModalMessage(modalDimmer);
            }
        });
        cancelBtn.setMinWidth(74);
        cancelBtn.setPrefWidth(74);
        
        HBox bottomBar = new HBox(0);
        bottomBar.setAlignment(Pos.BASELINE_RIGHT);
        bottomBar.getChildren().addAll(okBtn, cancelBtn);
        VBox.setMargin(bottomBar, new Insets(20, 5, 5, 5));
        
        getChildren().addAll(explPane, textArea, bottomBar);
        
        setOnKeyReleased(new EventHandler<KeyEvent>() {
            public void handle(final KeyEvent event) {
                if (event.isMetaDown() && event.getCode() == KeyCode.ENTER) {
                    save(config, elvn, modalDimmer);
                }
            }
        });
    }
    
    @Override
    public void requestFocus() {
        super.requestFocus();
        textArea.requestFocus();
    }
    
    /**
     * Updates configuration.
     * 
     * @param config
     */
    public void update(final String config) {
        textArea.setText(config);
    }
    
    private void save(final Config config, final Elvn elvn, final StackPane modalDimmer) {
        try {
            if (!modalDimmer.isVisible()) {
                return;
            }
            config.saveConfig(textArea.getText());
            elvn.hideModalMessage(modalDimmer);
        } catch (IOException e) {
            textArea.setText("Something goes wrong");
            e.printStackTrace();
        }
    }
}
