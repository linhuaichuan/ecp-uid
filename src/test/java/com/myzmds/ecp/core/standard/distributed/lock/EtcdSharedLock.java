package com.myzmds.ecp.core.standard.distributed.lock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSONObject;
import com.myzmds.ecp.core.uid.baidu.utils.NamingThreadFactory;

/**
 * @类名称 EtcdSharedLock.java
 * @类描述 <pre>etcd分布式锁：支持等待锁，竞争锁；可自动续租/释放资源；CP模式，强一致性</pre>
 * @作者  庄梦蝶殇 linhuaichuan1989@126.com
 * @创建时间 2019年12月16日 上午11:02:17
 * @版本 1.0.0
 *
 * @修改记录
 * <pre>
 *     版本                       修改人 		修改日期 		 修改内容描述
 *     ----------------------------------------------
 *     1.0.0 	       庄梦蝶殇 	2019年12月16日             
 *     ----------------------------------------------
 * </pre>
 */
public class EtcdSharedLock implements ISharedLock {
    
    /**
     * 过期时间命令
     */
    private static String PREFIX_TTL = "ttl=";
    
    /**
     * 锁值命令
     */
    private static String PREFIX_VAL = "?prevValue=";
    
    /**
     * 默认锁值
     */
    private static final String DEFAULT_VALUE = "1";
    
    /**
     * 默认etcd服务地址
     */
    private static String DEFAULT_URL = "http://172.16.104.105:2379/v2/keys/";
    
    /**
     * 整锁命令
     */
    private static final List<String> LOCK_APPLY = Arrays.asList(new String[] {"-XPUT", "-d", "value=1", "-d", "prevExist=false", "-d"});
    
    /**
     * 续租命令
     */
    private static final List<String> LOCK_RELET = Arrays.asList(new String[] {"-XPUT", "-d", "refresh=true", "-d", "prevExist=true", "-d"});
    
    /**
     * 解锁命令
     */
    private static final List<String> LOCK_DELETE = Arrays.asList(new String[] {"-XDELETE"});
    
    /**
     * 配置地址
     */
    public String url;
    
    /**
     * 锁名
     */
    public String name;
    
    /**
     * 锁值
     */
    public String value;
    
    /**
     * 锁过期时间，毫秒
     */
    public int ttl;
    
    /**
     * 心跳线程池
     */
    private ScheduledExecutorService scheduledpool;
    
    public EtcdSharedLock(String name) {
        this(name, DEFAULT_VALUE);
    }
    
    public EtcdSharedLock(String name, String value) {
        this(DEFAULT_URL, name, value);
    }
    
    public EtcdSharedLock(String url, String name, String value) {
        this.url = url;
        this.name = name;
        this.value = value;
    }

    @Override
    public boolean lock(int ttl)
        throws RuntimeException {
        this.ttl = ttl;
        List<String> commonds = new ArrayList<String>();
        commonds.add(url.concat(name));
        commonds.addAll(LOCK_APPLY);
        commonds.add(PREFIX_TTL + ttl);
        String result = CurlUtil.execCurl(commonds);
        if (null == result || "".equals(result)) {
            return false;
        }
        JSONObject jsonObject = JSONObject.parseObject(result);
        if (jsonObject.containsKey("errorCode")) {
            if ("105".equals(jsonObject.getString("errorCode"))) {
                return false;
            } else {
                logger.warn(MSG_LOCK_ERROR, name, result);
                throw new RuntimeException(result);
            }
        }
        return true;
    }

    @Override
    public void startHeartBeatThread() {
        scheduledpool = new ScheduledThreadPoolExecutor(1, new NamingThreadFactory(Thread.currentThread().getName().concat(LOCK_HEART_BEAT), true));
        scheduledpool.scheduleAtFixedRate(() -> {
            List<String> commonds = new ArrayList<String>();
            commonds.add(url.concat(name).concat(PREFIX_VAL).concat(value));
            commonds.addAll(LOCK_RELET);
            commonds.add(PREFIX_TTL + ttl);
            CurlUtil.execCurl(commonds);
            logger.debug(MSG_RELET, Thread.currentThread().getName(), name);
        }, 0L, ttl / 3, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public void close() {
        // 有锁才释放
        if (null != scheduledpool) {
            scheduledpool.shutdown();
            List<String> commonds = new ArrayList<String>();
            commonds.add(url.concat(name).concat(PREFIX_VAL).concat(value));
            commonds.addAll(LOCK_DELETE);
            String result = CurlUtil.execCurl(commonds);
            logger.debug(result);
        }
    }
    
    @Override
    public String getName() {
        return name;
    }
}
