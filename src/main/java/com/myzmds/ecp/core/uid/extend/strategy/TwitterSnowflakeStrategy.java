package com.myzmds.ecp.core.uid.extend.strategy;

import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.myzmds.ecp.core.uid.extend.annotation.UidModel;
import com.myzmds.ecp.core.uid.twitter.SnowflakeIdWorker;
import com.myzmds.ecp.core.uid.util.NetUtils;
import com.myzmds.ecp.core.uid.worker.WorkerIdAssigner;

/**
 * @类名称 TwitterSnowflakeStrategy.java
 * @类描述 <pre>Twitter策略(workerId的获取可使用WorkerIdAssigner的各种实例)</pre>
 * @作者  庄梦蝶殇 linhuaichuan1989@126.com
 * @创建时间 2018年8月31日 下午5:05:15
 * @版本 1.00
 *
 * @修改记录
 * <pre>
 *     版本                       修改人 		修改日期 		 修改内容描述
 *     ----------------------------------------------
 *     1.00 	庄梦蝶殇 	2018年8月31日             
 *     ----------------------------------------------
 * </pre>
 */
public class TwitterSnowflakeStrategy implements IUidStrategy {
    /**
     * 生成器集合
     */
    private static Map<String, SnowflakeIdWorker> generatorMap = new ConcurrentHashMap<>();
    
    /**
     * 机器ID
     */
    private Long workerId;
    
    /**
     * 数据中心id
     */
    private Long datacenterId;
    
    protected WorkerIdAssigner assigner;
    
    @Override
    public UidModel getName() {
        return UidModel.snowflake;
    }
    
    @Override
    public long getUID(String group) {
        return getSnowflakeId(group).nextId();
    }
    
    @Override
    public String parseUID(long uid, String group) {
        return getSnowflakeId(group).parseUID(uid);
    }
    
    /**
     * 获取uid生成器
     * @方法名称 getSnowflakeId
     * @功能描述 <pre>获取uid生成器</pre>
     * @param prefix 前缀
     * @return uid生成器
     */
    public SnowflakeIdWorker getSnowflakeId(String prefix) {
        SnowflakeIdWorker snowflakeIdWorker = generatorMap.get(prefix);
        if (null == snowflakeIdWorker) {
            synchronized (generatorMap) {
                if (null == snowflakeIdWorker) {
                    // 数据中心id--默认取机器码
                    Long realDid = null == datacenterId ? getMachineNum(31) : datacenterId;
                    // 机器id--默认取进程id
                    long realWid;
                    if (null != assigner) {
                        realWid = assigner.assignWorkerId();
                    } else if (null != workerId) {
                        realWid = workerId;
                    } else {
                        realWid = getProcessNum(realDid, 31);
                    }
                    snowflakeIdWorker = new SnowflakeIdWorker(realWid, realDid);
                    snowflakeIdWorker.setClock(true);
                }
                generatorMap.put(prefix, snowflakeIdWorker);
            }
        }
        return snowflakeIdWorker;
    }
    
    /**
     * 获取机器码
     * 
     * @param maxId 最大值
     */
    public static long getMachineNum(long maxId) {
        byte[] mac = NetUtils.getMachineNum();
        long id = 0L;
        if (mac == null) {
            id = 1L;
        } else {
            id = ((0x000000FF & (long)mac[mac.length - 1]) | (0x0000FF00 & (((long)mac[mac.length - 2]) << 8))) >> 6;
            id = id % (maxId + 1);
        }
        return id;
    }
    
    /**
     * 获取 进程id
     *
     * @param dataCenterId 数据中心id
     * @param maxWorkerId 最大机器id
     */
    public static long getProcessNum(long dataCenterId, long maxWorkerId) {
        StringBuilder mpid = new StringBuilder();
        mpid.append(dataCenterId);
        String name = ManagementFactory.getRuntimeMXBean().getName();
        if (null != name && !"".equals(name)) {
            // 获取 jvm Pid
            mpid.append(name.split("@")[0]);
        }
        // dataCenterId + PID 的 hashcode 获取16个低位
        return (mpid.toString().hashCode() & 0xffff) % (maxWorkerId + 1);
    }
    
    public Long getWorkerId() {
        return workerId;
    }
    
    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }
    
    public Long getDatacenterId() {
        return datacenterId;
    }
    
    public void setDatacenterId(Long datacenterId) {
        this.datacenterId = datacenterId;
    }
    
    public void setAssigner(WorkerIdAssigner assigner) {
        this.assigner = assigner;
    }
}
