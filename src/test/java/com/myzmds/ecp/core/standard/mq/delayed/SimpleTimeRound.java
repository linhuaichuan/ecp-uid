package com.myzmds.ecp.core.standard.mq.delayed;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.myzmds.ecp.core.uid.baidu.utils.NamingThreadFactory;

/**
 * @类名称 SimpleTimeRound.java
 * @类描述 <pre>简单时间轮实现-单轮时间轮</pre>
 * @作者  庄梦蝶殇 linhuaichuan1989@126.com
 * @创建时间 2019年12月25日 下午3:23:17
 * @版本 1.0.0
 *
 * @修改记录
 * <pre>
 *     版本                       修改人 		修改日期 		 修改内容描述
 *     ----------------------------------------------
 *     1.0.0 	       庄梦蝶殇 	2019年12月25日             
 *     ----------------------------------------------
 * </pre>
 */
public class SimpleTimeRound extends TimeRound<TimerTask> {
    private Lock lock = new ReentrantLock();
    
    /**
     * 过期任务执行线程
     */
    private ExecutorService workerThreadPool;
    
    public SimpleTimeRound(int cycle, int bucketSize) {
        super(cycle, bucketSize);
        workerThreadPool = new ThreadPoolExecutor(100, 200, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024), new NamingThreadFactory("", true));
    }
    
    @Override
    public void invoke() {
        List<TimerTask> bucket = bucketBuffer[currentPos];
        bucket.stream().forEach(item -> {
            workerThreadPool.submit(item.getTask());
        });
    }
    
    /**
     * @方法名称 addTask
     * @功能描述 <pre>增加时间桶任务</pre>
     * @param task 时间桶任务
     */
    public void addTask(TimerTask task) {
        try {
            lock.lock();
            // 当前时间轮可以容纳该任务 加入时间槽
            Long virtualId = task.getDelayTime() / currentPos;
            int index = (int)(virtualId % bucketSize);
            List<TimerTask> bucket = bucketBuffer[index];
            if (null == bucket) {
                bucket = new ArrayList<TimerTask>();
            }
            bucket.add(task);
            bucketBuffer[index] = bucket;
        } finally {
            lock.unlock();
        }
    }
    
}
