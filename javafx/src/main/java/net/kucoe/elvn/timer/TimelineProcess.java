package net.kucoe.elvn.timer;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

/**
 * {@link TimerProcess} implementation using {@link Timeline}
 * 
 * @author Vitaliy Basyuk
 */
public class TimelineProcess implements TimerProcess {
    
    private Timeline timeline;
    private int timeSeconds;
    
    @Override
    public void init(final int timeout, final TimerView timerView, final OnTime onTime) {
        if (timeline != null) {
            timeline.stop();
        }
        timeSeconds = timeout;
        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
            // KeyFrame event handler
            public void handle(final ActionEvent event) {
                try {
                    timeSeconds--;
                    // update timerLabel
                    timerView.update(timeSeconds);
                    if (timeSeconds <= 0) {
                        boolean completed = timeSeconds < 0;
                        timeline.stop();
                        timerView.playOnTime();
                        timeSeconds = 0;
                        timeline = null;
                        onTime.onTime(completed);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
        timeline.playFromStart();
    }
    
    @Override
    public void play() {
        if (timeline != null) {
            timeline.play();
        }
    }
    
    @Override
    public void stop() {
        if (timeline != null) {
            timeline.stop();
        }
    }
    
    @Override
    public void cancel() {
        if (timeline != null) {
            timeline.stop();
        }
        timeSeconds = 0;
        timeline = null;
    }
    
    @Override
    public void fire(final boolean complete) {
        if (complete) {
            timeSeconds = -1;
        } else {
            timeSeconds = 0;
        }
    }
    
}
