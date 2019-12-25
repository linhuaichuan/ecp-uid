package com.myzmds.ecp.core.standard.mq.delayed;

/**
 * 任务
 */
public class TimerTask {
    
    /**
     * 延迟时间
     */
    private long delayTime;
    
    /**
     * 任务
     */
    private Runnable task;
    
    /**
     * 时间槽
     */
    protected TimerTaskList timerTaskList;
    
    /**
     * 描述
     */
    public String desc;
    
    public TimerTask(long delayTime, Runnable task) {
        this.delayTime = System.currentTimeMillis() + delayTime;
        this.task = task;
        this.timerTaskList = null;
    }
    
    public Runnable getTask() {
        return task;
    }
    
    public long getDelayTime() {
        return delayTime;
    }
    
    @Override
    public String toString() {
        return desc;
    }
}
