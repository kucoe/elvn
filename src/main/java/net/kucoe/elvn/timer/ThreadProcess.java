package net.kucoe.elvn.timer;

/**
 * {@link TimerProcess} implementation.
 * 
 * @author Vitaliy Basyuk
 */
public class ThreadProcess implements TimerProcess {
    
    private boolean paused;
    protected int timeSeconds;
    
    @Override
    public void init(final int timeout, final TimerView timerView, final OnTime onTime) {
        timeSeconds = timeout;
        Runnable run = new Runnable() {
            
            @Override
            public void run() {
                try {
                    while (timeSeconds > 0) {
                        if (!paused) {
                            timeSeconds--;
                            timerView.update(timeSeconds);
                        }
                        Thread.sleep(1000);
                    }
                    if (timeSeconds == 0 || timeSeconds == -1) {
                        boolean completed = timeSeconds < 0;
                        timerView.playOnTime();
                        timeSeconds = 0;
                        onTime.onTime(completed);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        
        Thread thread = new Thread(run);
        thread.setName("Timer");
        thread.setDaemon(true);
        thread.start();
    }
    
    @Override
    public void play() {
        paused = false;
    }
    
    @Override
    public void stop() {
        paused = true;
    }
    
    @Override
    public void cancel() {
        timeSeconds = -2;
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
