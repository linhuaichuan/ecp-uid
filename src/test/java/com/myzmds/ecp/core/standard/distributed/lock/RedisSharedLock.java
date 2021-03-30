package com.myzmds.ecp.core.standard.distributed.lock;

import java.util.Random;
import java.util.UUID;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import com.myzmds.ecp.core.uid.baidu.utils.NamingThreadFactory;

/**
 * @类名称 RedisSharedLock.java
 * @类描述 <pre>redis 分布式锁(redis是ap模式，无法保证强一致性: 集群可能造成多重获锁,Redissond红锁 RedissonRedLock一样存在极端情况重复取锁问题)</pre>
 * @作者 庄梦蝶殇 linhuaichuan1989@126.com
 * @创建时间 2018年5月8日 下午8:09:15
 * @版本 1.3.0
 *
 * @修改记录
 * <pre>
 *     版本                       修改人 		修改日期 		 修改内容描述
 *     ----------------------------------------------
 *     1.0.0 	庄梦蝶殇    2018年05月08日             
 *     1.1.0    庄梦蝶殇    2018年11月21日             Redis pipeline操作，一致性增强，精简代码 (pipeline不保证原子性)
 *     1.2.0    庄梦蝶殇    2019年01月16日             改用Lua脚本，保证原子性
 *     1.3.0    庄梦蝶殇    2019年12月13日             统一锁接口
 *     ----------------------------------------------
 * </pre>
 */
public class RedisSharedLock implements ISharedLock {
    /**
     * 加锁脚本
     */
    private static final String SCRIPT_LOCK = "if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then redis.call('pexpire', KEYS[1], ARGV[2]) return 1 else return 0 end";
    
    /**
     * 解锁脚本
     */
    private static final String SCRIPT_UNLOCK = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
    
    /**
     * 加锁脚本sha1值
     */
    private static final String SCRIPT_LOCK_SHA1 = "8525317db2b346fffb9050797aee5fa8b8231872";
    
    /**
     * 解锁脚本sha1值
     */
    private static final String SCRIPT_UNLOCK_SHA1 = "e9f69f2beb755be68b5e456ee2ce9aadfbc4ebf4";
    
    /**
     * 成功标识
     */
    private static final Long SUCCESS = 1L;
    
    /**
     * 警告消息:降级删锁
     */
    private static final String WARN_MSG_EVAL = "Redis不支持EVAL命令，使用降级方式解锁：{}";
    
    /**
     * redis lock 前缀，方便redis key管理
     */
    private static final String LOCK_PREFIX = "lock:";
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 心跳线程池
     */
    private ScheduledExecutorService scheduledpool;
    
    /**
     * 锁名，即key值
     */
    public String lockKey;
    
    /**
     * 锁值，即value值
     */
    public String lockValue;
    
    /**
     * 锁过期时间，毫秒
     */
    public int ttl;
    
    public RedisSharedLock(String name) {
        lockValue = UUID.randomUUID().toString();
        lockKey = getLockKey(name);
    }
    
    public RedisSharedLock(RedisTemplate<String, Object> redisTemplate, String name) {
        this(name);
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public boolean lock(int ttl)
        throws RuntimeException {
        this.ttl = ttl;
        // 锁不存在时：上锁并过期时间，最后跳出。
        Long result = redisTemplate.execute(new RedisScript<Long>() {
            @Override
            public String getSha1() {
                return SCRIPT_LOCK_SHA1;
            }
            
            @Override
            public Class<Long> getResultType() {
                return Long.class;
            }
            
            @Override
            public String getScriptAsString() {
                return SCRIPT_LOCK;
            }
            
        }, Collections.singletonList(lockKey), lockValue, String.valueOf(ttl));
        if (SUCCESS.equals(result)) {
            return true;
        }
        return false;
    }
    
    @Override
    public void startHeartBeatThread() {
        scheduledpool = new ScheduledThreadPoolExecutor(1, new NamingThreadFactory(Thread.currentThread().getName().concat(LOCK_HEART_BEAT), true));
        scheduledpool.scheduleAtFixedRate(() -> {
            redisTemplate.expire(lockKey, ttl, TimeUnit.MILLISECONDS);
            logger.debug(MSG_RELET, Thread.currentThread().getName(), lockKey);
        }, 0L, ttl / 3, TimeUnit.MILLISECONDS);
        
    }
    
    @Override
    public void close() {
        if (null != scheduledpool && redisTemplate.hasKey(lockKey)) {
            try {
                redisTemplate.execute(new RedisScript<Long>() {
                    @Override
                    public Class<Long> getResultType() {
                        return Long.class;
                    }
                    
                    @Override
                    public String getScriptAsString() {
                        return SCRIPT_UNLOCK;
                    }
                    
                    @Override
                    public String getSha1() {
                        return SCRIPT_UNLOCK_SHA1;
                    }
                }, Collections.singletonList(lockKey), lockValue);
            } catch (Exception e) {
                logger.warn(WARN_MSG_EVAL, e.getMessage());
                redisTemplate.delete(lockKey);
            }
        }
    }
    
    /**
     * @方法名称 getLockKey
     * @功能描述 <pre>获取锁key</pre>
     * @param name 锁名
     * @return 锁key
     */
    public static String getLockKey(String name) {
        return LOCK_PREFIX.concat(name);
    }
    
    @Override
    public String getName() {
        return lockKey;
    }
}
