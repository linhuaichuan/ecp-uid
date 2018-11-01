package com.myzmds.ecp.core.uid.worker;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import com.myzmds.ecp.core.uid.util.NetUtils;
import com.myzmds.ecp.core.uid.util.ZkNodeUtils;

/**
 * zk节点编号分配器
 * @类名称 ZkNodeWorkerIdAssigner.java
 * @类描述 <pre>zk节点编号分配器{可设置interval-心跳间隔、pidHome-workerId文件存储目录、zkAddress-zk地址、pidPort-心跳端口}</pre>
 * @作者 庄梦蝶殇 linhuaichuan1989@126.com
 * @创建时间 2018年4月27日 下午8:14:21
 * @版本 1.00
 *
 * @修改记录
 * <pre>
 *     版本                       修改人 		修改日期 		 修改内容描述
 *     ----------------------------------------------
 *     1.0.0 	庄梦蝶殇 	2018年4月27日             利用zk的版本实现workid
 *     2.0.0    庄梦蝶殇      2018年9月4日               引入leaf的理念基于zkclient进行开发
 *     ----------------------------------------------
 * </pre>
 */
public class ZkNodeWorkerIdAssigner implements WorkerIdAssigner, InitializingBean {
    public static final String ZK_SPLIT = "/";
    
    /**
     * ZK上uid根目录
     */
    public static final String UID_ROOT = "/ecp-uid";
    
    /**
     * 持久顺序节点根目录(用于保存节点的顺序编号)
     */
    public static final String UID_FOREVER = UID_ROOT.concat("/forever");
    
    /**
     * 临时节点根目录(用于保存活跃节点及活跃心跳)
     */
    public static final String UID_TEMPORARY = UID_ROOT.concat("/temporary");

    /**
     * 本地workid文件跟目录
     */
    public static final String PID_ROOT = "/data/pids/";
    
    /**
     * session失效时间
     */
    public static final int SESSION_TIMEOUT = 3000;
    
    /**
     * connection连接时间
     */
    public static final int CONNECTION_TIMEOUT = 3000;
    
    // 心跳原子标识
    private AtomicBoolean active = new AtomicBoolean(false);
    
    // 因子ID
    private String workerId;
    
    //
    private ServerSocket socket;
    
    //
    private ZkClient zkClient;
    
    /**
     * 心跳间隔
     */
    private Long interval = 3000L;
    
    /**
     * zk注册地址
     */
    private String zkAddress = "127.0.0.1:2181";
    
    /**
     * workerID 文件存储路径
     */
    private String pidHome = PID_ROOT;

    /**
     * 使用端口(用于区分同机器多应用。)
     */
    private Integer pidPort = null;

    @Override
    public long assignWorkerId() {
        return Long.valueOf(workerId);
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        zkClient = new ZkClient(zkAddress, SESSION_TIMEOUT, CONNECTION_TIMEOUT);
        try {
            if (!zkClient.exists(UID_ROOT)) {
                zkClient.createPersistent(UID_ROOT, true);
            }
            /**
             * 1、检查workId文件是否存在。文件名为ip:port-zk顺序编号
             */
            String pidName = NetUtils.getLocalInetAddress().getHostAddress();
            if (null == pidPort) {// 占用端口
                pidPort = NetUtils.getAvailablePort();
                try {
                    socket = new ServerSocket(pidPort);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            pidName += ZkNodeUtils.WORKER_SPLIT + pidPort;
            String serverId = ZkNodeUtils.getPid(pidHome, pidName);
            String workNode = UID_FOREVER + ZK_SPLIT + pidName;
            
            /**
             * 2、文件不存在，检查zk上是否存在ip:port的节点
             */
            if (!zkClient.exists(UID_FOREVER)) {
                zkClient.createPersistent(UID_FOREVER, true);
            }
            if (StringUtils.isEmpty(serverId)) {
                List<String> uidWork = zkClient.getChildren(UID_FOREVER);
                for (int i = 0; i < uidWork.size(); i++) {// a、 检查zk上是否存在ip:port的节点,存在，获取节点的顺序编号
                    if (uidWork.get(i).startsWith(pidName)) {
                        serverId = String.valueOf(i);
                        break;
                    }
                }
                if (StringUtils.isEmpty(serverId)) {// b、 不存在，创建ip:port节点
                    serverId = String.valueOf(uidWork.size());
                    zkClient.create(workNode, new byte[0], CreateMode.PERSISTENT_SEQUENTIAL);
                }
            }
            
            /**
             * 3、创建临时节点
             */
            if (!zkClient.exists(UID_TEMPORARY)) {
                zkClient.createPersistent(UID_TEMPORARY, true);
            }
            zkClient.createEphemeral(UID_TEMPORARY + ZK_SPLIT + serverId, System.currentTimeMillis());
            // 临时节点在下线时自动删除，无需监控
            //            zkClient.subscribeDataChanges(UID_TEMPORARY + ZK_SPLIT + serverId, new IZkDataListener() {
            //                @Override
            //                public void handleDataDeleted(String dataPath)
            //                    throws Exception {
            //                    System.out.println("节点" + dataPath + "的数据：");
            //                }
            //                
            //                @Override
            //                public void handleDataChange(String dataPath, Object data)
            //                    throws Exception {
            //                    System.out.println("节点" + dataPath + "的数据：" + data);
            //                }
            //            });
            active.set(true);
            
            /**
             * 4、获取本地时间，跟zk临时节点列表的时间平均值做比较(zk临时节点用于存储活跃节点的上报时间，每隔一段时间上报一次临时节点时间)
             */
            List<String> activeNodes = zkClient.getChildren(UID_TEMPORARY);
            Long sumTime = 0L;
            for (String itemNode : activeNodes) {
                Long nodeTime = zkClient.readData(UID_TEMPORARY + ZK_SPLIT + itemNode);
                sumTime += nodeTime;
            }
            long diff = System.currentTimeMillis() - sumTime / activeNodes.size();
            if (diff < 0) {// 当前时间小于活跃节点的平均心跳时间，证明出现时间回拨，进入等待。
                ZkNodeUtils.sleepMs(interval * 2, diff);
            }
            // 赋值workerId,启动线程心跳
            workerId = serverId;
            ZkNodeUtils.writePidFile(pidHome + File.separatorChar + pidName + ZkNodeUtils.WORKER_SPLIT + serverId);
            startHeartBeatThread();
        } catch (Exception e) {
            active.set(false);
            zkClient.close();
            if (!StringUtils.isEmpty(workerId) && zkClient.exists(UID_TEMPORARY + ZK_SPLIT + workerId)) {
                zkClient.delete(UID_TEMPORARY + ZK_SPLIT + workerId);
            }
            if (null != socket) {
                socket.close();
            }
        }
    }
    
    /**
     * @方法名称 startHeartBeatThread
     * @功能描述 <pre>心跳线程，用于每隔一定时间跟zk汇报最后活跃时间</pre>
     */
    private void startHeartBeatThread() {
        Thread heartBeat = new Thread(new Runnable() {
            @Override
            public void run() {
                while (active.get() == true) {
                    if (workerId != null && zkClient != null) {
                        try {
                            zkClient.writeData(UID_TEMPORARY + ZK_SPLIT + workerId, System.currentTimeMillis());
                        } catch (Exception e) {
                        }
                    }
                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                        
                    }
                }
            }
        });
        heartBeat.setName("zookeeper uid 节点心跳");
        heartBeat.setDaemon(true);
        heartBeat.start();
    }

    public String getZkAddress() {
        return zkAddress;
    }

    public void setZkAddress(String zkAddress) {
        this.zkAddress = zkAddress;
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
}
