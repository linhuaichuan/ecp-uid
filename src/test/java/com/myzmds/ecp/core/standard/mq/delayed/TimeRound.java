package com.myzmds.ecp.core.standard.mq.delayed;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.myzmds.ecp.core.standard.distributed.lock.ISharedLock;
import com.myzmds.ecp.core.uid.baidu.utils.NamingThreadFactory;

/**
 * @类名称 TimeRound.java
 * @类描述 <pre>时间轮类</pre>
 * @版本 1.0.0
 *
 * @修改记录
 * <pre>
 *     版本                       修改人 		修改日期 		 修改内容描述
 *     ----------------------------------------------
 *     1.0.0 	       庄梦蝶殇 	2019年12月23日             
 *     ----------------------------------------------
 * </pre>
 */
public abstract class TimeRound<T> {
    private final static String ELECT_LEADER = "elect_leader";
    
    /**
     * 滴答单位，默认为秒
     */
    protected TimeUnit ticktack = TimeUnit.SECONDS;
    
    /**
     * 滴答频率，默认为1
     */
    protected int ticktackVal = 1;
    
    /**
     * 时间桶任务
     */
    protected List<T>[] bucketBuffer;
    
    /**
     * 时间桶数量
     */
    protected int bucketSize;
    
    /**
     * 当前指针
     */
    protected int currentPos = 0;
    
    @Autowired
    ISharedLock sharedLock;
    
    /**
     * @param bucketSize 时间桶数量
     */
    @SuppressWarnings("unchecked")
    public TimeRound(int bucketSize) {
        this.bucketSize = bucketSize;
        this.bucketBuffer = new ArrayList[bucketSize];
    }
    
    /**
     * @param cycle 时间轮周期时间，时间单位默认为秒
     * @param bucketSize 时间桶数量
     */
    public TimeRound(int cycle, int bucketSize) {
        this(bucketSize);
        Assert.isTrue(cycle % bucketSize == 0, "时间轮周期必须被时间桶数整除!");
        this.ticktackVal = cycle / bucketSize;
        this.bucketSize = bucketSize;
    }
    
    /**
     * @param cycle 时间轮周期时间
     * @param bucketSize 时间桶数量
     * @param ticktack 滴答时间单位
     */
    public TimeRound(int cycle, int bucketSize, TimeUnit ticktack) {
        this(cycle, bucketSize);
        this.ticktack = ticktack;
    }
    
    /**
     * @方法名称 invoke
     * @功能描述 <pre>时间桶 滴答逻辑</pre>
     * @param bucket 时间桶任务TimeTask
     */
    public abstract void invoke();
    
    public void lockLoad() {
    }
    
    /**
     * @方法名称 start
     * @功能描述 <pre>启动时间轮：竞锁启动，支持分布式部署</pre>
     */
    public void start() {
        // 分布式抢锁
        try {
            if (sharedLock.acquireOrWait(50, 200)) {
                lockLoad();
                ScheduledThreadPoolExecutor executorService = new ScheduledThreadPoolExecutor(1, new NamingThreadFactory(ELECT_LEADER, true));
                executorService.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        // 执行时间轮 滴答逻辑
                        invoke();
                        // 重置拨零
                        currentPos++;
                        if (currentPos >= bucketSize) {
                            currentPos = 0;
                        }
                    }
                }, 0, ticktackVal, ticktack);// 1、滴答逻辑，2、延迟启动时间，3、滴答间隔，4、滴答力度
            }
        } finally {
            sharedLock.close();
        }
    }
}
