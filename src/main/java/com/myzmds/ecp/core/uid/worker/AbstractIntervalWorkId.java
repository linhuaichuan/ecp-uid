package com.myzmds.ecp.core.uid.worker;

import java.io.File;
import java.net.ServerSocket;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.InitializingBean;

import com.myzmds.ecp.core.uid.baidu.utils.NamingThreadFactory;
import com.myzmds.ecp.core.uid.util.WorkerIdUtils;

/**
 * @类名称 AbstractIntervalWorkId.java
 * @类描述 <pre>WorkId生成基类</pre>
 * @作者  庄梦蝶殇 linhuaichuan1989@126.com
 * @创建时间 2019年1月17日 下午4:21:17
 * @版本 1.0.0
 *
 * @修改记录
 * <pre>
 *     版本                       修改人 		修改日期 		 修改内容描述
 *     ----------------------------------------------
 *     1.0.0 		庄梦蝶殇 	2019年1月17日             
 *     ----------------------------------------------
 * </pre>
 */
public abstract class AbstractIntervalWorkId implements WorkerIdAssigner, InitializingBean {
    /**
     * 本地workid文件跟目录
     */
    public static final String PID_ROOT = "/data/pids/";
    
    /**
     * 线程名-心跳
     */
    public static final String THREAD_HEARTBEAT_NAME = "zk_heartbeat";
    
    /**
     * 心跳原子标识
     */
    protected AtomicBoolean active = new AtomicBoolean(false);
    
    /**
     * 心跳间隔
     */
    protected Long interval = 3000L;
    
    /**
     * workerID 文件存储路径
     */
    protected String pidHome = PID_ROOT;
    
    /**
     * 因子ID
     */
    protected Long workerId;
    
    /**
     * 使用端口(同机多uid应用时区分端口)
     */
    private Integer pidPort = -1;
    
    protected String pidName;
    
    protected ServerSocket socket;
    
    @Override
    public void afterPropertiesSet()
        throws Exception {
        try {
            /**
             * 1、检查workId文件是否存在。文件名为ip:port-redis顺序编号
             */
            pidName = WorkerIdUtils.getPidName(pidPort, socket);
            workerId = WorkerIdUtils.getPid(pidHome, pidName);
            /**
             * 3、获取本地时间，跟uid 机器节点心跳列表的时间平均值做比较(uid 机器节点心跳列表 用于存储活跃节点的上报时间，每隔一段时间上报一次临时节点时间)
             */
            long diff = System.currentTimeMillis() - action();
            if (diff < 0) {
                // 当前时间小于活跃节点的平均心跳时间，证明出现时间回拨，进入等待。
                WorkerIdUtils.sleepMs(interval * 2, diff);
            }
            if (null != workerId) {
                startHeartBeatThread();
                // 赋值workerId
                WorkerIdUtils.writePidFile(pidHome + File.separatorChar + pidName + WorkerIdUtils.WORKER_SPLIT + workerId);
            }
        } catch (Exception e) {
            active.set(false);
            if (null != socket) {
                socket.close();
            }
            throw e;
        }
    }
    
    /**
     * @方法名称 action
     * @功能描述 <pre>workId文件不存在时的操作</pre>
     * @return 机器节点列表的 活跃时间平均值
     */
    public abstract long action();
    
    /**
     * @方法名称 startHeartBeatThread
     * @功能描述 <pre>心跳线程，用于每隔一段时间上报一次临时节点时间</pre>
     */
    protected void startHeartBeatThread() {
        ScheduledExecutorService scheduledpool = new ScheduledThreadPoolExecutor(1, new NamingThreadFactory(THREAD_HEARTBEAT_NAME, true));
        scheduledpool.scheduleAtFixedRate(() -> {
            if (active.get() == false) {
                scheduledpool.shutdownNow();
            } else if (where()) {
                report();
            }
        }, 0L, interval, TimeUnit.MILLISECONDS);
    }
    
    /**
     * @方法名称 where
     * @功能描述 <pre>心跳条件</pre>
     * @return true:执行心跳上报，false:空动作
     */
    public abstract boolean where();
    
    /**
     * @方法名称 report
     * @功能描述 <pre>心跳上报</pre>
     */
    public abstract void report();
    
    @Override
    public long assignWorkerId() {
        return workerId;
    }
    
    public Long getInterval() {
        return interval;
    }
    
    public void setInterval(Long interval) {
        this.interval = interval;
    }
    
    public String getPidHome() {
        return pidHome;
    }
    
    public void setPidHome(String pidHome) {
        this.pidHome = pidHome;
    }
    
    public Integer getPidPort() {
        return pidPort;
    }
    
    public void setPidPort(Integer pidPort) {
        this.pidPort = pidPort;
    }
}
