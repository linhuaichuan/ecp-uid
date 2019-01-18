package com.myzmds.ecp.core.uid.worker;

import java.util.List;

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;

/**
 * @类名称 ZkWorkerIdAssigner.java
 * @类描述 <pre>zk编号分配器{可设置interval-心跳间隔、pidHome-workerId文件存储目录、zkAddress-zk地址、pidPort-使用端口(默认不开,同机多uid应用时区分端口)}</pre>
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
public class ZkWorkerIdAssigner extends AbstractIntervalWorkId {
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
    
    //
    private ZkClient zkClient;
    
    /**
     * zk注册地址
     */
    private String zkAddress = "127.0.0.1:2181";
    
    @Override
    public long action() {
        zkClient = new ZkClient(zkAddress, SESSION_TIMEOUT, CONNECTION_TIMEOUT);
        if (!zkClient.exists(UID_ROOT)) {
            zkClient.createPersistent(UID_ROOT, true);
        }
        String workNode = UID_FOREVER + ZK_SPLIT + pidName;
        
        /**
         * 2、文件不存在，检查zk上是否存在ip:port的节点
         */
        if (!zkClient.exists(UID_FOREVER)) {
            zkClient.createPersistent(UID_FOREVER, true);
        }
        if (null == workerId || !zkClient.exists(workNode)) {
            // a、 检查zk上是否存在ip:port的节点,存在，获取节点的顺序编号
            List<String> uidWork = zkClient.getChildren(UID_FOREVER);
            for (String nodePath : uidWork) {
                if (nodePath.startsWith(pidName)) {
                    workerId = Long.valueOf(nodePath.substring(nodePath.length() - 10));
                    break;
                }
            }
            // b、 不存在，创建ip:port节点
            if (null == workerId) {
                String nodePath = zkClient.create(workNode, new byte[0], CreateMode.PERSISTENT_SEQUENTIAL);
                workerId = Long.valueOf(nodePath.substring(nodePath.length() - 10));
            }
        }
        
        /**
         * 3、创建临时节点
         */
        if (!zkClient.exists(UID_TEMPORARY)) {
            zkClient.createPersistent(UID_TEMPORARY, true);
        }
        zkClient.createEphemeral(UID_TEMPORARY + ZK_SPLIT + workerId, System.currentTimeMillis());
        // 临时节点在下线时自动删除，无需监控
        // zkClient.subscribeDataChanges(UID_TEMPORARY + ZK_SPLIT + serverId, new IZkDataListener() {
        // @Override
        // public void handleDataDeleted(String dataPath)
        // throws Exception {
        // System.out.println("节点" + dataPath + "的数据：");
        // }
        //
        // @Override
        // public void handleDataChange(String dataPath, Object data)
        // throws Exception {
        // System.out.println("节点" + dataPath + "的数据：" + data);
        // }
        // });
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
        return sumTime / activeNodes.size();
    }
    
    @Override
    public boolean where() {
        return null != workerId && null != zkClient;
    }
    
    @Override
    public void report() {
        zkClient.writeData(UID_TEMPORARY + ZK_SPLIT + workerId, System.currentTimeMillis());
    }
    
    public String getZkAddress() {
        return zkAddress;
    }
    
    public void setZkAddress(String zkAddress) {
        this.zkAddress = zkAddress;
    }
}
