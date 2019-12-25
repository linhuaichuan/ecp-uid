package com.myzmds.ecp.core.standard.distributed.lock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.springframework.util.Assert;

/**
 * @类名称 ZkSharedLock.java
 * @类描述 <pre>zk分布式锁：支持等待锁，竞争锁；天然自动续租/释放资源，轮训检测；CP模式，强一致性</pre>
 * @作者 庄梦蝶殇 linhuaichuan1989@126.com
 * @创建时间 2019年12月16日 下午4:38:11
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
public class ZkSharedLock implements ISharedLock {
    /**
     * 锁在zk上的根路径/根节点
     */
    private final static String ROOT_PATH = "/locks";
    
    /**
     * 最大重试次数
     */
    private final static Integer MAX_RETRY_COUNT = 10;
    
    /**
     * zk连接
     */
    public final static String MSG_CON = "zk获取连接成功！";
    
    /**
     * zk客户端
     */
    private ZooKeeper client;
    
    /**
     * zk地址
     */
    private String connectIp;
    
    /**
     * 计数器
     */
    private CountDownLatch latch;
    
    /**
     * 锁名
     */
    public String name;
    
    /**
     * 锁在zk上的完整路径
     */
    public String ourPath;
    
    /**
     * 锁根路径
     */
    public String basePath;
    
    public ZkSharedLock(String connectIp, String name) {
        Assert.notNull(connectIp, "zk地址不能为空！");
        this.name = name;
        this.connectIp = connectIp;
        init();
    }
    
    /**
     * @方法名称 init
     * @功能描述 <pre>初始化</pre>
     */
    public void init() {
        initClient();
        this.basePath = ROOT_PATH.concat(ZkUtil.PATH_SPILT).concat(name);
        try {
            if (null == client.exists(ROOT_PATH, false)) {
                client.create(ROOT_PATH, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            client.close();
        } catch (Exception e) {
            // 忽略 初始化根路径错误
        }
    }
    
    @Override
    public boolean lock(int ttl) {
        initClient();
        List<String> children = getSortLockNode();
        if (null == children) {
            return false;
        }
        // 当前有序节点名(锁名)
        String sequenceNodeName = ourPath.substring(basePath.length() + 1);
        // 计算刚才客户端创建的顺序节点在locker的所有子节点中排序位置，如果是排序为0，则表示获取到了锁
        int ourIndex = children.indexOf(sequenceNodeName);
        // 没有找到之前创建的[临时]顺序节点，这表示可能由于网络闪断而导致 Zookeeper认为连接断开而删除了我们创建的节点，证明获取锁失败
        if (ourIndex < 0) {
            return false;
        }
        // 检验是否未最小节点，是则获取锁
        if (ourIndex == 0) {
            // 获取锁成功
            logger.debug(MSG_LOCK, sequenceNodeName);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean acquireOrWait(int ttl, int waitTime) {
        initClient();
        /**
         * 1、获取锁的所有竞争者列表
         */
        List<String> children = getSortLockNode();
        if (null == children) {
            return false;
        }
        boolean haveTheLock = false;
        // 重置计数器
        latch = new CountDownLatch(1);
        // 当前有序节点名(锁名)
        String sequenceNodeName = ourPath.substring(basePath.length() + 1);
        // 起始时间
        long startMillis = System.currentTimeMillis();
        // 等待时间
        while (!haveTheLock) {
            // 重新获取列表
            children = getSortLockNode();
            if (null == children) {
                return false;
            }
            /**
             * 2、检验是否未最小节点，是则获取锁
             */
            // 计算刚才客户端创建的顺序节点在locker的所有子节点中排序位置，如果是排序为0，则表示获取到了锁
            int ourIndex = children.indexOf(sequenceNodeName);
            // 没有找到之前创建的[临时]顺序节点，这表示可能由于网络闪断而导致 Zookeeper认为连接断开而删除了我们创建的节点，证明获取锁失败
            if (ourIndex < 0) {
                return false;
            }
            // 检验是否未最小节点，是则获取锁
            if (ourIndex == 0) {
                // 获取锁成功
                logger.debug(MSG_LOCK, sequenceNodeName);
                return true;
            }
            
            /**
             * 3、订阅次小节点的监听，进行等待
             */
            // 获取次小节点名
            String pathToWatch = children.get(ourIndex - 1);
            // 如果次小的节点被删除了，则表示当前客户端的节点应该是最小的了，所以使用CountDownLatch来实现等待
            // 上一有序节点名(上个锁名)
            String prevNodePath = basePath.concat(ZkUtil.PATH_SPILT).concat(pathToWatch);
            
            // 查询前一个目录是否存在，并且注册目录事件监听器，监听一次事件后即删除
            try {
                Stat state = client.exists(prevNodePath, new Watcher() {
                    @Override
                    public void process(WatchedEvent event) {
                        if (event.getType().equals(EventType.NodeDeleted) && null != latch) {
                            latch.countDown();
                        }
                    }
                });
                if (null == state) {
                    continue;
                }
                // 发生超时需要删除节点，防止无限等待造成线程垃圾。
                waitTime -= (System.currentTimeMillis() - startMillis);
                startMillis = System.currentTimeMillis();
                if (waitTime <= 0) {
                    logger.debug(MSG_LOCK_TIMEOUT, sequenceNodeName);
                    // 等待超时，删除我们的节点
                    client.close();
                    return false;
                }
                long maxWait = waitTime < client.getSessionTimeout() - 10 ? waitTime : client.getSessionTimeout() - 10;
                // 等待
                latch.await(maxWait, TimeUnit.MICROSECONDS);
            } catch (KeeperException | InterruptedException e) {
                // ignore
            }
        }
        return haveTheLock;
    }
    
    @Override
    public void startHeartBeatThread() {
        
    }
    
    @Override
    public void close() {
        if (null == ourPath) {
            // 空锁时，直接返回
            return;
        }
        try {
            client.delete(ourPath, -1);
            client.close();
        } catch (Exception e) {
        }
    }
    
    /**
     * @方法名称 getSortLockNode
     * @功能描述 <pre>获取本锁路径下的所有子节点，并按小->大排序</pre>
     * @return 当前锁的所有竞争者列表
     */
    private List<String> getSortLockNode() {
        /**
         * 1、zk跟路径检测
         */
        // 锁路径
        String path = basePath.concat(ZkUtil.PATH_SPILT).concat(name);
        try {
            if (null == client.exists(basePath, false)) {
                client.create(basePath, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            // 锁根路径
        }
        
        /**
         * 2、创建 当前锁名 的临时节点(在[basePath持久节点]下创建客户端要获取锁的[临时]顺序节点)
         */
        // 是否需要重试
        boolean isDone = false;
        // 重试次数
        int retryCount = 0;
        // 网络闪断需要重试一试
        while (!isDone) {
            isDone = true;
            try {
                ourPath = client.create(path, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
                /**
                 * 3、获取本锁路径下的所有子节点，并按小->大排序
                 */
                return ZkUtil.getSortedChildren(client, basePath, name);
            } catch (Exception e) {
                if (retryCount++ < MAX_RETRY_COUNT) {
                    isDone = false;
                }
            }
        }
        return null == ourPath ? null : new ArrayList<String>();
    }
    
    private void initClient() {
        try {
            client = new ZooKeeper(connectIp, 300000, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (Event.KeeperState.SyncConnected == event.getState()) {
                        logger.debug(MSG_CON);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public String getName() {
        return ourPath;
    }
}
