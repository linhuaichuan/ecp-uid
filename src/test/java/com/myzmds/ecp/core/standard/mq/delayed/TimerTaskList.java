package com.myzmds.ecp.core.standard.mq.delayed;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 时间槽
 */
public class TimerTaskList implements Delayed {
    
    /**
     * 过期时间
     */
    private AtomicLong expiration = new AtomicLong(-1L);
    
    /**
     * 设置过期时间
     */
    public boolean setExpiration(long expire) {
        return expiration.getAndSet(expire) != expire;
    }
    
    /**
     * 获取过期时间
     */
    public long getExpiration() {
        return expiration.get();
    }
    
    @Override
    public long getDelay(TimeUnit unit) {
        return Math.max(0, unit.convert(expiration.get() - System.currentTimeMillis(), TimeUnit.MILLISECONDS));
    }
    
    @Override
    public int compareTo(Delayed o) {
        if (o instanceof TimerTaskList) {
            return Long.compare(expiration.get(), ((TimerTaskList)o).expiration.get());
        }
        return 0;
    }
}
