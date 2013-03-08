package net.kucoe.elvn;

import net.kucoe.elvn.timer.TaskStage;

/**
 * Information on Timer state
 * 
 * @author Vitaliy Basyuk
 */
public class TimerInfo {
    
    private final Long runId;
    private final TaskStage stage;
    private final int minutes;
    
    /**
     * Constructs TimerInfo.
     * 
     * @param runId
     * @param stage {@link TaskStage}
     * @param minutes
     */
    public TimerInfo(Long runId, TaskStage stage, int minutes) {
        this.runId = runId;
        this.stage = stage;
        this.minutes = minutes;
    }
    
    /**
     * Returns the runId Long.
     * 
     * @return the runId Long.
     */
    public Long getRunId() {
        return runId;
    }
    
    /**
     * Returns the stage TaskStage.
     * 
     * @return the stage TaskStage.
     */
    public TaskStage getStage() {
        return stage;
    }
    
    /**
     * Returns the minutes int.
     * 
     * @return the minutes int.
     */
    public int getMinutes() {
        return minutes;
    }
    
    @Override
    public int hashCode() {
        return (int) (getRunId() ^ (getRunId() >>> 32));
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append(":");
        sb.append(getStage());
        sb.append("-");
        sb.append(getMinutes());
        sb.append("-");
        sb.append(getRunId());
        return sb.toString();
    }
    
}
