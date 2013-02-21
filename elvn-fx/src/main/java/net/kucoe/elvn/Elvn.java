package net.kucoe.elvn;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javafx.animation.*;
import javafx.application.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import net.kucoe.elvn.controls.*;
import net.kucoe.elvn.controls.ColorBox.ColorLabelProvider;
import net.kucoe.elvn.lang.EL;
import net.kucoe.elvn.lang.ELCommand;
import net.kucoe.elvn.lang.result.ELResult;
import net.kucoe.elvn.timer.*;
import net.kucoe.elvn.util.*;

/**
 * Main application class
 */
public class Elvn extends Application implements Display {
    protected static final String APP_NAME = "Elvn";
    
    /**
     * Starts
     * 
     * @param args
     */
    public static void main(final String[] args) {
        Application.launch(args);
    }
    
    private Config config;
    private double mouseDragOffsetX = 0;
    private double mouseDragOffsetX2 = 0;
    private double mouseDragOffsetY = 0;
    private double mouseDragOffsetY2 = 0;
    private Pane mainArea;
    private Node currentListView;
    private String current;
    private ColorBox colorPicker;
    private TextField commandLine;
    private WindowResizeButton windowResizeButton;
    private Scene main;
    private EventHandler<KeyEvent> keyHandler;
    private String taskHint;
    private String noteHint;
    private StackPane modalDimmer;
    private ConfigDialog proxyDialog;
    private Label statusLabel;
    
    @Override
    public String getCurrentList() {
        return current;
    }
    
    @Override
    public void showHelp(final String helpMessage) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                commandLine.setText(helpMessage);
                commandLine.positionCaret(helpMessage.length());
            }
        });
    }
    
    @Override
    public void setCurrentList(final String current) {
        this.current = current;
    }
    
    @Override
    public void showConfig(final String config) {
        proxyDialog.update(config);
        showConfigDialog(modalDimmer, proxyDialog);
    }
    
    @Override
    public void showStatus(final String status) {
        if (statusLabel != null) {
            statusLabel.setText(status);
        }
    }
    
    @Override
    public void showList(final List list) {
        if (list == null) {
            return;
        }
        current = list.getColor();
        CommandTool tool = new CommandTool(this, config);
        ListView<Task> view = tool.createView(list, null);
        mainArea.getChildren().setAll(view);
        currentListView = view;
        colorPicker.setValue(ListColor.color(list.getColor()));
        colorPicker.setVisible(true);
        commandLine.setPromptText(taskHint);
        commandLine.requestFocus();
    }
    
    @Override
    public void showLists(final Config config) {
        current = ELCommand.ListEdit.el();
        CommandTool tool = new CommandTool(this, config);
        ListView<List> view = new ListEditorView(tool);
        mainArea.getChildren().setAll(view);
        currentListView = view;
        colorPicker.setVisible(false);
        commandLine.setPromptText(taskHint);
        commandLine.requestFocus();
    }
    
    @Override
    public void showNote(final Note note, final int position) {
        if (note != null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    String s = ELCommand.Locate.el() + position + ELCommand.Assign.el() + note.getText();
                    commandLine.setText(s);
                    commandLine.positionCaret(s.length());
                }
            });
        }
    }
    
    @Override
    public void showNotes(final java.util.List<Note> notes) {
        current = ELCommand.Notes.el();
        CommandTool tool = new CommandTool(this, config);
        ListView<Note> view = new NoteView(tool, notes);
        mainArea.getChildren().setAll(view);
        currentListView = view;
        colorPicker.setVisible(false);
        commandLine.setPromptText(noteHint);
        commandLine.requestFocus();
    }
    
    @Override
    public void showTask(final Task task, final int position) {
        if (task != null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    String s =
                            ELCommand.Locate.el() + position + ELCommand.Assign.el() + task.getList() + ":"
                                    + task.getText();
                    commandLine.setText(s);
                    commandLine.positionCaret(s.length());
                }
            });
        }
    }
    
    @Override
    public void showTasks(final java.util.List<Task> tasks) {
        current = ListColor.White.toString();
        CommandTool tool = new CommandTool(this, config);
        ListView<Task> view = tool.createView(null, tasks);
        mainArea.getChildren().setAll(view);
        currentListView = view;
        colorPicker.setValue(ListColor.White);
        colorPicker.setVisible(true);
        commandLine.setPromptText(taskHint);
        commandLine.requestFocus();
    }
    
    @Override
    public void start(final Stage stage) throws Exception {
        stage.setTitle(APP_NAME);
        StackPane layerPane = createLayerPane(stage);
        config = new Config();
        final CommandTool tool = new CommandTool(this, config);
        BorderPane root = (BorderPane) layerPane.getChildren().get(0);
        Scene scene = createScene(layerPane);
        modalDimmer = createModalDimmer();
        layerPane.getChildren().add(modalDimmer);
        proxyDialog = new ConfigDialog(config, this, modalDimmer);
        ToolBar toolBar = createTopToolbar(stage);
        root.setTop(toolBar);
        
        SplitPane splitPane = createSplitPane();
        BorderPane centerSplitPane = new BorderPane();
        ToolBar tasksToolBar = createMidToolbar(tool, config);
        Button timerButton = createTimerButton(tool);
        Button listButton = createListButton();
        Button notesButton = createNotesButton();
        Button settingsButton = createSettingsButton();
        tasksToolBar.getItems().add(timerButton);
        tasksToolBar.getItems().add(listButton);
        tasksToolBar.getItems().add(notesButton);
        tasksToolBar.getItems().add(settingsButton);
        scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            public void handle(final KeyEvent event) {
                if (event.isMetaDown()) {
                    if (event.getCode() == KeyCode.T) {
                        tool.runTask(null);
                    } else if (event.getCode() == KeyCode.L) {
                        showLists(config);
                    } else if (event.getCode() == KeyCode.N) {
                        showNotes(getNotes());
                    } else if (event.getCode() == KeyCode.S) {
                        showConfig(getConfig());
                    }
                }
            }
        });
        
        mainArea = createMainArea(stage);
        ToolBar tasksBottomBar = createTasksBootomToolbar(config);
        centerSplitPane.setTop(tasksToolBar);
        centerSplitPane.setCenter(mainArea);
        centerSplitPane.setBottom(tasksBottomBar);
        
        splitPane.getItems().addAll(centerSplitPane);
        splitPane.setDividerPosition(0, 1);
        splitPane.setDividerPosition(2, 1);
        root.setCenter(splitPane);
        windowResizeButton.setManaged(false);
        root.getChildren().add(windowResizeButton);
        // show stage
        stage.setScene(scene);
        stage.show();
        
        showList(config.getList(ListColor.Teal));
    }
    
    private Scene createScene(final StackPane layerPane) {
        boolean is3dSupported = Platform.isSupported(ConditionalFeature.SCENE3D);
        Scene scene = new Scene(layerPane, 1020, 700, is3dSupported);
        if (is3dSupported) {
            // RT-13234
            scene.setCamera(new PerspectiveCamera());
        }
        URL resource = getClass().getResource("/net/kucoe/elvn/resources/elvn.css");
        scene.getStylesheets().add(resource.toExternalForm());
        return scene;
    }
    
    private Scene createFloatingScene(final Stage stage, final VBox view) {
        boolean is3dSupported = Platform.isSupported(ConditionalFeature.SCENE3D);
        StackPane layerPane = new StackPane();
        BorderPane root = new BorderPane();
        root.getStyleClass().add("application");
        root.setId("root");
        layerPane.setDepthTest(DepthTest.DISABLE);
        layerPane.getChildren().add(root);
        Scene scene = new Scene(layerPane, 200, 150, is3dSupported);
        if (is3dSupported) {
            // RT-13234
            scene.setCamera(new PerspectiveCamera());
        }
        URL resource = getClass().getResource("/net/kucoe/elvn/resources/elvn.css");
        scene.getStylesheets().add(resource.toExternalForm());
        
        ToolBar toolBar = new ToolBar();
        toolBar.setId("top-toolbar");
        toolBar.setMinHeight(30);
        toolBar.setPrefHeight(30);
        toolBar.setMaxHeight(30);
        toolBar.setMaxWidth(Double.MAX_VALUE);
        ImageView logo =
                new ImageView(new Image(getClass().getResourceAsStream("/net/kucoe/elvn/resources/images/logo.png")));
        logo.setCursor(Cursor.MOVE);
        HBox.setMargin(logo, new Insets(0, 0, 0, 5));
        toolBar.getItems().add(logo);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        toolBar.getItems().add(spacer);
        root.setTop(toolBar);
        root.setCenter(view);
        addDND(stage, toolBar, null);
        return scene;
    }
    
    private StackPane createLayerPane(final Stage stage) {
        StackPane layerPane = new StackPane();
        BorderPane root = new BorderPane();
        stage.initStyle(StageStyle.UNDECORATED);
        windowResizeButton = new WindowResizeButton(stage, 650, 400);
        root = new BorderPane() {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                windowResizeButton.autosize();
                windowResizeButton.setLayoutX(getWidth() - windowResizeButton.getLayoutBounds().getWidth());
                windowResizeButton.setLayoutY(getHeight() - windowResizeButton.getLayoutBounds().getHeight());
            }
        };
        root.getStyleClass().add("application");
        root.setId("root");
        layerPane.setDepthTest(DepthTest.DISABLE);
        layerPane.getChildren().add(root);
        return layerPane;
    }
    
    private StackPane createModalDimmer() {
        final StackPane modalDimmer = new StackPane();
        modalDimmer.setId("modal-dimmer");
        modalDimmer.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent t) {
                t.consume();
                hideModalMessage(modalDimmer);
            }
        });
        modalDimmer.setOnKeyReleased(new EventHandler<KeyEvent>() {
            public void handle(final KeyEvent event) {
                if (event.getCode() == KeyCode.ESCAPE) {
                    hideModalMessage(modalDimmer);
                }
            }
        });
        modalDimmer.setVisible(false);
        return modalDimmer;
    }
    
    /**
     * Show the given node as a floating dialog over the whole application, with the rest of the
     * application dimmed out and blocked from mouse events.
     * 
     * @param modalDimmer
     * @param message
     */
    public void showModalMessage(final StackPane modalDimmer, final Node message) {
        modalDimmer.getChildren().add(message);
        modalDimmer.setOpacity(0);
        modalDimmer.setVisible(true);
        modalDimmer.setCache(true);
        TimelineBuilder.create().keyFrames(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent t) {
                modalDimmer.setCache(false);
                message.requestFocus();
            }
        }, new KeyValue(modalDimmer.opacityProperty(), 1, Interpolator.EASE_BOTH))).build().play();
    }
    
    /**
     * Hide any modal message that is shown
     * 
     * @param modalDimmer
     */
    public void hideModalMessage(final StackPane modalDimmer) {
        modalDimmer.setCache(true);
        TimelineBuilder.create().keyFrames(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent t) {
                modalDimmer.setCache(false);
                modalDimmer.setVisible(false);
                modalDimmer.getChildren().clear();
                commandLine.requestFocus();
            }
        }, new KeyValue(modalDimmer.opacityProperty(), 0, Interpolator.EASE_BOTH))).build().play();
    }
    
    /**
     * Show the dialog for setting proxy to the user
     * 
     * @param modalDimmer
     * @param configDialog
     */
    public void showConfigDialog(final StackPane modalDimmer, final Node configDialog) {
        showModalMessage(modalDimmer, configDialog);
    }
    
    private SplitPane createSplitPane() {
        SplitPane splitPane = new SplitPane();
        splitPane.setId("main-splitpane");
        splitPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        GridPane.setConstraints(splitPane, 0, 0);
        return splitPane;
    }
    
    private Button createTimerButton(final CommandTool tool) {
        EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent event) {
                tool.runTask(null);
            }
        };
        return createButton("timer", handler);
    }
    
    private Button createListButton() {
        EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent event) {
                showLists(config);
            }
        };
        return createButton("list", handler);
    }
    
    private Button createNotesButton() {
        EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent event) {
                showNotes(getNotes());
            }
        };
        return createButton("notes", handler);
    }
    
    private Button createSettingsButton() {
        EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent event) {
                showConfig(getConfig());
            }
        };
        return createButton("sync", handler);
    }
    
    private Button createButton(final String name, final EventHandler<ActionEvent> handler) {
        Button button = new Button();
        button.setId(name + "-button");
        button.setGraphic(new ImageView(new Image(getClass().getResourceAsStream(
                "/net/kucoe/elvn/resources/icons/" + name + ".png"))));
        button.setOnAction(handler);
        return button;
    }
    
    private ToolBar createTopToolbar(final Stage stage) {
        ToolBar toolBar = new ToolBar();
        toolBar.setId("top-toolbar");
        toolBar.setMinHeight(35);
        toolBar.setPrefHeight(35);
        toolBar.setMaxHeight(35);
        toolBar.setMaxWidth(Double.MAX_VALUE);
        ImageView logo =
                new ImageView(new Image(getClass().getResourceAsStream("/net/kucoe/elvn/resources/images/logo.png")));
        logo.setCursor(Cursor.MOVE);
        HBox.setMargin(logo, new Insets(0, 0, 0, 5));
        toolBar.getItems().add(logo);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        toolBar.getItems().add(spacer);
        WindowButtons windowButtons = addWindowButtons(stage, toolBar);
        toolBar.getItems().add(windowButtons);
        GridPane.setConstraints(toolBar, 0, 0);
        return toolBar;
    }
    
    private ToolBar createMidToolbar(final CommandTool tool, final Config config) throws IOException, JsonException {
        ToolBar toolBar = new ToolBar();
        toolBar.setId("mid-toolbar");
        toolBar.setMinHeight(35);
        toolBar.setPrefHeight(35);
        toolBar.setMaxHeight(35);
        toolBar.setMaxWidth(Double.MAX_VALUE);
        statusLabel = new Label();
        statusLabel.setText(config.getStatus());
        statusLabel.getStyleClass().add("inset-label");
        config.setStatusListener(new StatusUpdateListener() {
            public void onStatusChange(String status) {
                showStatus(status);
            }
        });
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        colorPicker = createColorBox(tool);
        toolBar.getItems().add(colorPicker);
        toolBar.getItems().add(spacer);
        toolBar.getItems().add(statusLabel);
        spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        toolBar.getItems().add(spacer);
        return toolBar;
    }
    
    private ColorBox createColorBox(final CommandTool tool) {
        ColorLabelProvider colorLabelProvider = new ColorLabelProvider() {
            @Override
            public String getLabel(final ListColor color) {
                List list = getList(color);
                return list == null ? null : list.getLabel();
            }
        };
        ColorBox colorPicker = new ColorBox(colorLabelProvider);
        colorPicker.setItems(tool.colors());
        colorPicker.valueProperty().addListener(new ChangeListener<ListColor>() {
            @Override
            public void changed(final ObservableValue<? extends ListColor> ov, final ListColor oldValue,
                final ListColor newValue) {
                showList(getList(newValue));
            }
        });
        return colorPicker;
    }
    
    private ToolBar createTasksBootomToolbar(final Config config) {
        ToolBar toolBar = new ToolBar();
        toolBar.setId("mid-toolbar");
        toolBar.setMinHeight(70);
        toolBar.setPrefHeight(70);
        toolBar.setMaxHeight(70);
        toolBar.setMaxWidth(Double.MAX_VALUE);
        final History history = new History(config);
        try {
            history.prepare();
        } catch (IOException | JsonException e) {
            e.printStackTrace();
        }
        commandLine = new TextField();
        commandLine.getStyleClass().add("big-font");
        taskHint = "Add task for ex.(red:what to be done:2)";
        noteHint = "Add note";
        commandLine.setPromptText(taskHint);
        final KeyCombination comboEnter = new KeyCodeCombination(KeyCode.ENTER);
        final KeyCombination comboUp = new KeyCodeCombination(KeyCode.UP);
        final KeyCombination comboDown = new KeyCodeCombination(KeyCode.DOWN);
        keyHandler = new EventHandler<KeyEvent>() {
            public void handle(final KeyEvent t) {
                if (comboEnter.match(t)) {
                    t.consume();
                    String text = commandLine.getText();
                    history.introduce(text);
                    ELResult result = EL.process(text);
                    try {
                        current = result.execute(Elvn.this, config);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    commandLine.setText("");
                }
                if (comboUp.match(t)) {
                    String text = history.getPrevious();
                    commandLine.setText(text);
                }
                if (comboDown.match(t)) {
                    String text = history.getNext();
                    commandLine.setText(text);
                }
                commandLine.positionCaret(commandLine.getText().length());
            }
        };
        commandLine.setOnKeyReleased(keyHandler);
        HBox.setHgrow(commandLine, Priority.ALWAYS);
        toolBar.getItems().add(commandLine);
        return toolBar;
    }
    
    private WindowButtons addWindowButtons(final Stage stage, final ToolBar toolBar) {
        final WindowButtons windowButtons = new WindowButtons(stage, new EventHandler<ActionEvent>() {
            public void handle(final ActionEvent event) {
                if (Timer.isRunning()) {
                    Timer.showFloating();
                    event.consume();
                }
            }
        });
        // add window header double clicking
        toolBar.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent event) {
                if (event.getClickCount() == 2) {
                    windowButtons.toogleMaximized(stage);
                }
            }
        });
        addDND(stage, toolBar, windowButtons);
        return windowButtons;
    }
    
    protected void addDND(final Stage stage, final ToolBar toolBar, final WindowButtons windowButtons) {
        // add window dragging
        toolBar.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent event) {
                if (windowButtons == null) {
                    mouseDragOffsetX2 = event.getSceneX();
                    mouseDragOffsetY2 = event.getSceneY();
                } else {
                    mouseDragOffsetX = event.getSceneX();
                    mouseDragOffsetY = event.getSceneY();
                }
            }
        });
        toolBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent event) {
                if (windowButtons == null) {
                    stage.setX(event.getScreenX() - mouseDragOffsetX2);
                    stage.setY(event.getScreenY() - mouseDragOffsetY2);
                } else if (!windowButtons.isMaximized()) {
                    stage.setX(event.getScreenX() - mouseDragOffsetX);
                    stage.setY(event.getScreenY() - mouseDragOffsetY);
                }
            }
        });
    }
    
    private Pane createMainArea(final Stage stage) {
        final Pane mainArea = new Pane() {
            @Override
            protected void layoutChildren() {
                for (Node child : getChildren()) {
                    child.resizeRelocate(0, 0, getWidth(), getHeight());
                }
            }
        };
        mainArea.setId("main-area");
        Timer.setTimerView(new TimerViewBase() {
            protected void renderTo(final VBox view) {
                if (main != null) {
                    stage.setScene(main);
                    stage.setY(30);
                    stage.setX(100);
                    stage.setTitle(APP_NAME);
                    main = null;
                }
                mainArea.getChildren().setAll(view);
            }
            
            protected void back() {
                if (main != null) {
                    stage.setScene(main);
                    stage.setY(30);
                    stage.setX(100);
                    stage.setTitle(APP_NAME);
                    main = null;
                }
                mainArea.getChildren().setAll(currentListView);
            }
            
            @Override
            protected void toFloatingWindow(final VBox view) {
                main = stage.getScene();
                stage.setScene(createFloatingScene(stage, view));
                stage.setY(30);
                stage.setX(400);
            }
            
            @Override
            protected void update(final String text) {
                if (main != null) {
                    stage.setTitle(text);
                }
            }
        });
        Timer.setProcess(new TimelineProcess());
        return mainArea;
    }
    
    private String getConfig() {
        try {
            return config.getConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private java.util.List<Note> getNotes() {
        try {
            return config.getNotes();
        } catch (IOException | JsonException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    
    private List getList(final ListColor color) {
        try {
            return config.getList(color);
        } catch (IOException | JsonException e) {
            e.printStackTrace();
        }
        return null;
    }
}
