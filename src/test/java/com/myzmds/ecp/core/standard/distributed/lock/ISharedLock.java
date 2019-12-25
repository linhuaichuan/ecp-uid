package com.myzmds.ecp.core.standard.distributed.lock;

import java.io.Closeable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @类名称 ISharedLock.java
 * @类描述 <pre>分布式锁:支持等待锁，竞争锁；可自动续租/释放资源；</pre>
 * @作者  庄梦蝶殇 linhuaichuan1989@126.com
 * @版本 1.0.0
 *
 * @修改记录
 * <pre>
 *     版本                       修改人 		修改日期 		 修改内容描述
 *     ----------------------------------------------
 *     1.0.0 	       庄梦蝶殇 	2019年12月13日             
 *     ----------------------------------------------
 * </pre>
 */
public interface ISharedLock extends Closeable {
    public static Logger logger = LoggerFactory.getLogger(ISharedLock.class);
    
    /**
     * 线程名-心跳
     */
    public static final String LOCK_HEART_BEAT = ":heart_beat";
    
    /**
     * 取锁日志
     */
    public final static String MSG_LOCK = "{}获得锁";
    
    /**
     * 取锁错误日志
     */
    public final static String MSG_LOCK_ERROR = "{}取锁失败，错误为{}";
    
    /**
     * 续租日志
     */
    public final static String MSG_RELET = "{}续租锁";
    
    /**
     * 解锁日志
     */
    public final static String MSG_UNLOCK = "{}释放锁";
    
    /**
     * 取锁超时日志
     */
    public final static String MSG_LOCK_TIMEOUT = "{}取锁超时";
    
    /**
     * @方法名称 acquire
     * @功能描述 <pre>竞争锁,并自动续租</pre>
     * @param ttl 锁过期时间，单位毫秒
     * @return true-获取锁，false-未获得锁
     * @throws RuntimeException 操作锁失败，需要业务判断是否重试 
     */
    default boolean acquire(int ttl)
        throws RuntimeException {
        if (lock(ttl)) {
            logger.debug(MSG_LOCK, getName());
            startHeartBeatThread();
            return true;
        }
        return false;
    }
    
    /**
     * @方法名称 lock
     * @功能描述 <pre>获取锁</pre>
     * @param ttl 锁过期时间，单位毫秒
     * @return true-获取锁，false-未获得锁
     * @throws RuntimeException 操作锁失败，需要业务判断是否重试 
     */
    boolean lock(int ttl)
        throws RuntimeException;
    
    /**
     * @方法名称 acquireOrWait
     * @功能描述 <pre>竞争锁或等待锁</pre>
     * @param ttl 锁过期时间，单位毫秒
     * @param waitTime 等待时间，单位毫秒
     * @return true-获取锁，false-未获得锁
     * @throws RuntimeException 操作锁失败，需要业务判断是否重试 
     */
    default boolean acquireOrWait(int ttl, int waitTime)
        throws RuntimeException {
        while (!lock(ttl)) {
            waitTime = waitTime - ttl / 2;
            try {
                Thread.sleep(ttl / 2);
            } catch (InterruptedException e) {
                // 忽略睡眠异常
            }
            if (waitTime <= 0) {
                logger.debug(MSG_LOCK_TIMEOUT, getName());
                return false;
            }
        }
        startHeartBeatThread();
        return true;
    }
    
    /**
     * @方法名称 startHeartBeatThread
     * @功能描述 <pre>续租心跳</pre>
     */
    void startHeartBeatThread();
    
    /**
     * @方法名称 close
     * @功能描述 <pre>释放锁</pre>
     */
    @Override
    void close();
    
    /**
     * @方法名称 release
     * @功能描述 <pre>释放锁</pre>
     */
    default void release() {
        close();
    }
    
    /**
     * 获取锁名
     */
    public String getName();
}
