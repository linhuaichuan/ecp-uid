package com.myzmds.ecp.core.standard.mq.delayed;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

/**
 * @类名称 RedisDelayeTimeRound.java
 * @类描述 <pre>redis延时消息时间轮</pre>
 * @作者  庄梦蝶殇 linhuaichuan@veredholdings.com
 * @创建时间 2019年12月25日 下午3:22:29
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
public class RedisDelayeTimeRound extends TimeRound<String> {
    
    @Autowired
    public RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    RedisDelayMsgServiceImpl msgQueue;
    
    public Integer queueSize = 20;
    
    public RedisDelayeTimeRound(int queueSize) {
        super(3600);
    }
    
    /**
     * @方法名称 lockLoad
     * @功能描述 <pre>需要一小时加载一次</pre>
     */
    @Override
    public void lockLoad() {
        // 一小时的数据量
        long start = System.currentTimeMillis();
        long end = start + 3600000;
        Set<TypedTuple<String>> set = new HashSet<>();
        for (int i = 0; i < queueSize; i++) {
            set.addAll(redisTemplate.boundZSetOps(RedisDelayMsgServiceImpl.TOPIC_DELAYE + i).rangeByScoreWithScores(start, end));
        }
        set.stream().collect(Collectors.groupingBy(TypedTuple::getScore)).entrySet().stream().forEach(item -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(item.getKey().longValue());
            bucketBuffer[calendar.get(Calendar.SECOND)] = item.getValue().stream().map(TypedTuple::getValue).collect(Collectors.toList());
        });
    }
    
    public void invoke() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.SECOND, currentPos);
        calendar.set(Calendar.MILLISECOND, 0);
        for (int i = 0; i < queueSize; i++) {
            redisTemplate.boundZSetOps(RedisDelayMsgServiceImpl.TOPIC_DELAYE + i).removeRangeByScore(calendar.getTimeInMillis() - 100, calendar.getTimeInMillis());
        }
        List<String> bucket = bucketBuffer[currentPos];
        redisTemplate.boundSetOps(RedisDelayMsgServiceImpl.TOPIC_READY).add(bucket.toArray(new String[bucket.size()]));
    }
    
    /**
     * @方法名称 addTask
     * @功能描述 <pre>增加时间桶任务</pre>
     * @param task 时间桶任务
     */
    public void addTask(Message task) {
        msgQueue.push(task);
    }
}
