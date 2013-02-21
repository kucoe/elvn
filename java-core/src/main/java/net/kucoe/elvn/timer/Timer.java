package net.kucoe.elvn.timer;

import net.kucoe.elvn.Task;

/**
 * System-wide timer.
 * 
 * @author Vitaliy Basyuk
 */
public class Timer {
    
    /**
     * Working time interval
     */
    public static final int WORK_TIME = 15;
    
    /**
     * Break time interval
     */
    public static final int BREAK_TIME = 2;
    
    /**
     * Long break time interval
     */
    public static final int LONG_BREAK_TIME = 11;
    
    private static boolean running;
    private static TimerView timerView;
    private static TimerProcess process;
    
    /**
     * Overrides timerView the timerView.
     * 
     * @param timerView the timerView to set.
     */
    public static void setTimerView(final TimerView timerView) {
        Timer.timerView = timerView;
    }
    
    /**
     * Overrides process the process.
     * 
     * @param process the process to set.
     */
    public static void setProcess(final TimerProcess process) {
        if (running) {
            shutdown();
        }
        Timer.process = process;
    }
    
    /**
     * Runs timer according to Elvn specification: <br/>
     * Start with task and relax for 11 mins <br/>
     * Work for 15 mins <br/>
     * Break for 2 mins <br/>
     * Work for 15 mins <br/>
     * Break for 2 mins <br/>
     * Work for 15 mins <br/>
     * <br/>
     * Elvn complete start new. <br/>
     * If timer is running that invocation will be skipped.
     * 
     * @param task
     * @param onTime
     */
    public static void runElvn(final Task task, final OnTime onTime) {
        run(LONG_BREAK_TIME, task, TaskStage.Elvn, new OnTime() {
            @Override
            public void onTime(final boolean completed) throws Exception {
                if (completed) {
                    onTime.onTime(completed);
                } else {
                    iterateTillDone(task, onTime, 1);
                }
            }
        });
    }
    
    /**
     * Runs timer for a minutes defined with next event when timer finished. If timer is running
     * that invocation will be skipped.
     * 
     * @param mins
     * @param task
     * @param stage
     * @param onTime
     */
    public static void run(final int mins, final Task task, final TaskStage stage, final OnTime onTime) {
        if (timerView == null || process == null) {
            return;
        }
        if (running) {
            timerView.show(null, null, 0);
            return;
        }
        TaskStage st = stage;
        if (st == null) {
            st = TaskStage.Work;
            if (BREAK_TIME == mins) {
                st = TaskStage.Break;
            } else if (LONG_BREAK_TIME == mins) {
                st = TaskStage.Elvn;
            }
        }
        int timeSeconds = mins * 60;
        timerView.show(task, st, timeSeconds);
        timerView.playOnStart();
        process.init(timeSeconds, timerView, new OnTime() {
            public void onTime(final boolean completed) throws Exception {
                clear();
                onTime.onTime(completed);
            }
        });
        running = true;
    }
    
    /**
     * Resumes timer.If timer is not running that invocation will be skipped.
     */
    public static void resume() {
        if (!running || process == null) {
            return;
        }
        process.play();
    }
    
    /**
     * Pauses timer.If timer is not running that invocation will be skipped.
     */
    public static void pause() {
        if (!running || process == null) {
            return;
        }
        process.stop();
    }
    
    /**
     * Cancels timer.If timer is not running that invocation will be skipped.
     */
    public static void cancel() {
        if (!running || timerView == null || process == null) {
            return;
        }
        timerView.hide();
        process.cancel();
        running = false;
    }
    
    protected static void clear() {
        running = false;
    }
    
    /**
     * Fires timer.If timer is not running that invocation will be skipped.
     * 
     * @param complete whether complete whole process or stage
     */
    public static void fire(final boolean complete) {
        if (!running || process == null) {
            return;
        }
        process.fire(complete);
    }
    
    /**
     * Shows timer view.
     */
    public static void show() {
        if (timerView != null) {
            timerView.show(null, null, 0);
        }
    }
    
    /**
     * Shows timer floating view.
     */
    public static void showFloating() {
        if (timerView != null) {
            timerView.showSmall();
        }
    }
    
    /**
     * Hides timer view. But continue calculations.
     */
    public static void hide() {
        if (timerView != null) {
            timerView.hide();
        }
    }
    
    /**
     * Stops timer.
     */
    public static void shutdown() {
        cancel();
    }
    
    /**
     * Returns the running boolean.
     * 
     * @return the running boolean.
     */
    public static boolean isRunning() {
        return running;
    }
    
    /**
     * Toggle time periodical show.
     */
    public static void silent() {
        if (timerView != null) {
            timerView.silent();
        }
    }
    
    private static void iterateTillDone(final Task task, final OnTime onTime, final int cycle) {
        if (cycle == 3) {
            run(WORK_TIME, task, TaskStage.Work3, onTime);
        } else {
            run(WORK_TIME, task, cycle == 2 ? TaskStage.Work2 : TaskStage.Work, new OnTime() {
                public void onTime(final boolean completed) throws Exception {
                    if (completed) {
                        onTime.onTime(completed);
                    } else {
                        run(BREAK_TIME, task, TaskStage.Break, new OnTime() {
                            public void onTime(final boolean completed) throws Exception {
                                if (completed) {
                                    onTime.onTime(completed);
                                } else {
                                    iterateTillDone(task, onTime, cycle + 1);
                                }
                            }
                        });
                    }
                }
            });
        }
    }
    
}
