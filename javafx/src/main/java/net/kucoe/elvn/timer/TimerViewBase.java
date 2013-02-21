package net.kucoe.elvn.timer;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import net.kucoe.elvn.Task;
import net.kucoe.elvn.controls.SysTray;

/**
 * Base {@link TimerView} implementation.
 * 
 * @author Vitaliy Basyuk
 */
public abstract class TimerViewBase implements TimerView {
    
    private Label timeLabel;
    private Label timeLabelSmall;
    private VBox view;
    private Task lastTask;
    private MediaPlayer player;
    private StringProperty stage = new SimpleStringProperty();
    private ObjectProperty<Paint> style = new SimpleObjectProperty<>();
    
    @Override
    public void hide() {
        SysTray.showInTray(false);
        back();
    }
    
    @Override
    public void playOnTime() {
        initPlayer("ontime");
        SysTray.showInTray(false);
    }
    
    @Override
    public void playOnStart() {
        initPlayer("ticking");
        showSmall();
    }
    
    @Override
    public void silent() {
        // do nothing
    }
    
    @Override
    public void showSmall() {
        toFloatingWindow(buildFloatView(lastTask));
        SysTray.showInTray(true);
    }
    
    @Override
    public void show(final Task task, final TaskStage stage, final int seconds) {
        if (stage != null) {
            this.stage.set(stage.toString());
            Color color = Color.ORANGERED;
            if (TaskStage.Break.equals(stage) || TaskStage.Elvn.equals(stage)) {
                color = Color.GREEN;
            }
            style.set(color);
        }
        if (view != null && (task == null || task == lastTask)) {
            renderTo(view);
        } else {
            lastTask = task;
            view = buildView(task);
        }
    }
    
    @Override
    public void update(final int seconds) {
        int mins = (seconds / 60);
        int secs = seconds % 60;
        String divider = secs < 10 ? ":0" : ":";
        String text = "" + mins + divider + secs;
        if (timeLabel != null) {
            timeLabel.setText(text);
        }
        if (timeLabelSmall != null) {
            timeLabelSmall.setText(text);
        }
        update(text);
        SysTray.updateTooltip(text);
    }
    
    protected VBox buildView(final Task task) {
        timeLabel = new Label();
        timeLabel.setId("time-label");
        HBox timeBar = new HBox(0);
        timeBar.setAlignment(Pos.BASELINE_CENTER);
        timeBar.getChildren().addAll(timeLabel);
        
        final Button pauseBtn = new Button();
        final Button playBtn = new Button();
        pauseBtn.setId("pause-button");
        pauseBtn.setGraphic(new ImageView(new Image(getClass().getResourceAsStream(
                "/net/kucoe/elvn/resources/icons/pause.png"))));
        pauseBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent actionEvent) {
                Timer.pause();
                pauseBtn.setVisible(false);
                playBtn.setVisible(true);
            }
        });
        pauseBtn.managedProperty().bind(pauseBtn.visibleProperty());
        playBtn.setId("play-button");
        playBtn.setGraphic(new ImageView(new Image(getClass().getResourceAsStream(
                "/net/kucoe/elvn/resources/icons/play.png"))));
        playBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent actionEvent) {
                Timer.resume();
                playBtn.setVisible(false);
                pauseBtn.setVisible(true);
            }
        });
        playBtn.setVisible(false);
        playBtn.managedProperty().bind(playBtn.visibleProperty());
        Button cancelBtn = new Button();
        cancelBtn.setId("cancel-button");
        cancelBtn.setGraphic(new ImageView(new Image(getClass().getResourceAsStream(
                "/net/kucoe/elvn/resources/icons/cancel.png"))));
        cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent actionEvent) {
                Timer.cancel();
            }
        });
        Region spacer = new Region();
        Button homeBtn = new Button();
        homeBtn.setId("home-button");
        homeBtn.setGraphic(new ImageView(new Image(getClass().getResourceAsStream(
                "/net/kucoe/elvn/resources/icons/home.png"))));
        homeBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent actionEvent) {
                Timer.hide();
            }
        });
        HBox controlsBar = new HBox(10);
        controlsBar.setAlignment(Pos.BASELINE_CENTER);
        controlsBar.getChildren().addAll(pauseBtn, playBtn, cancelBtn, spacer, homeBtn);
        controlsBar.setMinHeight(70);
        controlsBar.setPrefHeight(70);
        controlsBar.setMaxHeight(70);
        
        Button completeBtn = new Button();
        completeBtn.setId("complete-button");
        completeBtn.setGraphic(new ImageView(new Image(getClass().getResourceAsStream(
                "/net/kucoe/elvn/resources/icons/complete.png"))));
        completeBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent actionEvent) {
                Timer.fire(true);
            }
        });
        Label taskLabel = new Label();
        taskLabel.getStyleClass().add("inset-label");
        taskLabel.textProperty().bind(Bindings.concat(stage, ": ", task.getText()));
        spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox bottomBar = new HBox(10);
        bottomBar.setAlignment(Pos.BASELINE_LEFT);
        bottomBar.getChildren().addAll(completeBtn, taskLabel, spacer);
        bottomBar.setMinHeight(30);
        bottomBar.setPrefHeight(30);
        bottomBar.setMaxHeight(30);
        
        VBox view = new VBox(10);
        view.setId("timer-box");
        view.getChildren().addAll(timeBar, controlsBar, bottomBar);
        
        renderTo(view);
        
        return view;
    }
    
    protected VBox buildFloatView(final Task task) {
        final Button pauseBtn = new Button();
        final Button playBtn = new Button();
        pauseBtn.setId("pause-button");
        pauseBtn.setGraphic(new ImageView(new Image(getClass().getResourceAsStream(
                "/net/kucoe/elvn/resources/icons/pause-small.png"))));
        pauseBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent actionEvent) {
                Timer.pause();
                pauseBtn.setVisible(false);
                playBtn.setVisible(true);
            }
        });
        pauseBtn.managedProperty().bind(pauseBtn.visibleProperty());
        playBtn.setId("play-button");
        playBtn.setGraphic(new ImageView(new Image(getClass().getResourceAsStream(
                "/net/kucoe/elvn/resources/icons/play-small.png"))));
        playBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent actionEvent) {
                Timer.resume();
                playBtn.setVisible(false);
                pauseBtn.setVisible(true);
            }
        });
        playBtn.setVisible(false);
        playBtn.managedProperty().bind(playBtn.visibleProperty());
        
        timeLabelSmall = new Label();
        timeLabelSmall.setId("time-label-small");
        
        Label taskLabelSmall = new Label();
        taskLabelSmall.setId("task-label-small");
        taskLabelSmall.textProperty().bind(Bindings.concat(stage, ": ", task.getText()));
        taskLabelSmall.textFillProperty().bind(style);
        
        Button homeBtn = new Button();
        homeBtn.setId("home-button");
        homeBtn.setGraphic(new ImageView(new Image(getClass().getResourceAsStream(
                "/net/kucoe/elvn/resources/icons/home-small.png"))));
        homeBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent actionEvent) {
                Timer.hide();
            }
        });
        
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.BASELINE_LEFT);
        bar.getChildren().addAll(pauseBtn, playBtn, timeLabelSmall);
        
        HBox bar2 = new HBox(10);
        bar2.setAlignment(Pos.BASELINE_LEFT);
        bar2.getChildren().addAll(taskLabelSmall, homeBtn);
        
        VBox view = new VBox(10);
        view.setId("timer-box");
        view.setAlignment(Pos.BASELINE_RIGHT);
        view.getChildren().addAll(bar, bar2);
        
        return view;
    }
    
    protected synchronized void initPlayer(final String file) {
        final Media media =
                new Media(getClass().getResource("/net/kucoe/elvn/resources/audio/" + file + ".mp3").toExternalForm());
        player = new MediaPlayer(media);
        player.setOnError(new Runnable() {
            @Override
            public void run() {
                System.out.println("mediaPlayer.getError() = " + player.getError());
            }
        });
        player.setVolume(0.7);
        player.play();
    }
    
    protected abstract void renderTo(VBox view);
    
    protected abstract void back();
    
    protected abstract void toFloatingWindow(VBox view);
    
    protected abstract void update(String text);
    
}
