package com.myzmds.ecp.core.uid.worker;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @类名称 RedisWorkIdAssigner.java
 * @类描述 <pre>Redis编号分配器</pre>
 * @作者  庄梦蝶殇 linhuaichuan1989@126.com
 * @创建时间 2019年1月16日 下午3:32:07
 * @版本 1.0.1
 *
 * @修改记录
 * <pre>
 *     版本                       修改人 		修改日期 		 修改内容描述
 *     ----------------------------------------------
 *     1.0.0 		庄梦蝶殇 	2019年1月16日             
 *     1.0.1        庄梦蝶殇    2019年1月18日      完善代码
 *     ----------------------------------------------
 * </pre>
 */
public class RedisWorkIdAssigner extends AbstractIntervalWorkId {
    /**
     * redis上uid 机器节点的key前缀
     */
    public static final String UID_ROOT = "ecp:uid:";
    
    /**
     * uid 机器节点列表
     */
    public static final String UID_FOREVER = UID_ROOT.concat("forever");
    
    /**
     * uid 活跃节点心跳列表(用于保存活跃节点及活跃心跳)
     */
    public static final String UID_TEMPORARY = UID_ROOT.concat("temporary:");
    
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;
    
    @Override
    public long action() {
        /**
         * 1、文件不存在，检查redis上是否存在ip:port的机器节点
         */
        Set<Object> uidWork = redisTemplate.opsForZSet().range(UID_FOREVER, 0, -1);
        if (null == workerId) {
            // a、 检查redis上是否存在ip:port的节点,存在，获取节点的顺序编号
            Long i = 0L;
            for (Object item : uidWork) {
                i++;
                if (item.toString().equals(pidName)) {
                    workerId = i;
                    break;
                }
            }
            // b、 不存在，创建ip:port节点
            if (null == workerId) {
                workerId = (long)uidWork.size();
                // 使用zset 时间排序，保证有序性
                redisTemplate.opsForZSet().add(UID_FOREVER, pidName, System.currentTimeMillis());
                uidWork.add(pidName);
            }
        }
        /**
         * 2、创建临时机器节点的时间
         */
        redisTemplate.opsForValue().set(UID_TEMPORARY + pidName, System.currentTimeMillis(), interval * 3, TimeUnit.MILLISECONDS);
        active.set(true);
        
        /**
         * 3、获取本地时间，跟uid 活跃节点心跳列表的时间平均值做比较(uid 活跃节点心跳列表 用于存储活跃节点的上报时间，每隔一段时间上报一次临时节点时间)
         */
        Long sumTime = 0L;
        if (null != uidWork && uidWork.size() > 0) {
            for (Object itemName : uidWork) {
                Object itemTime = redisTemplate.opsForValue().get(UID_TEMPORARY + itemName);
                sumTime += null == itemTime ? 0 : Long.valueOf(itemTime.toString());
            }
            return sumTime / uidWork.size();
        }
        return 0;
    }
    
    @Override
    public boolean where() {
        return null != workerId;
    }
    
    @Override
    public void report() {
        redisTemplate.opsForValue().set(UID_TEMPORARY + pidName, System.currentTimeMillis(), interval * 3, TimeUnit.MILLISECONDS);
    }
}
