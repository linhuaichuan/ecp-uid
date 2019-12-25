package com.myzmds.ecp.core.standard.mq.delayed;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @类名称 RedisDelayeQueue.java
 * @类描述 <pre>reids 延迟消息服务</pre>
 * @作者  庄梦蝶殇 linhuaichuan1989@126.com
 * @创建时间 2019年12月25日 上午11:04:45
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
public class RedisDelayMsgServiceImpl implements IDelayMsgService {
    public final static String KEY_PREFIX = "task:mq:";
    
    public final static String TOPIC_DELAYE = KEY_PREFIX + "delaye";
    
    public final static String TOPIC_READY = KEY_PREFIX + "ready";
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private IMessageDao messageDao;
    
    public Integer queueSize = 20;
    
    public RedisDelayMsgServiceImpl() {
    }
    
    public RedisDelayMsgServiceImpl(Integer queueSize) {
        super();
        this.queueSize = queueSize;
    }
    
    private int mod(int id) {
        return id % queueSize;
    }
    
    @Override
    public void push(Message queue) {
        redisTemplate.boundZSetOps(TOPIC_DELAYE + mod(queue.getId().hashCode())).add(queue.getId(), queue.getPublishTime());
        messageDao.insert(queue);
    }
    
    @Override
    public List<Message> pull() {
        Set<String> ids = redisTemplate.boundSetOps(TOPIC_READY).members();
        return messageDao.select(ids.toArray(new String[ids.size()]));
    }
    
    @Override
    public void ack(List<String> ids) {
        redisTemplate.boundZSetOps(TOPIC_READY).remove(ids.toArray());
        messageDao.delete(ids.toArray(new String[ids.size()]));
    }
}
