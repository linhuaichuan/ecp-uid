package com.myzmds.ecp.core.standard.distributed.lock;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

/**
 * @类名称 ZkUtils.java
 * @类描述 <pre>zk辅助类</pre>
 * @作者  庄梦蝶殇 linhuaichuan1989@126.com
 * @创建时间 2019年3月27日 下午2:54:29
 * @版本 1.0.0
 *
 * @修改记录
 * <pre>
 *     版本                       修改人 		修改日期 		 修改内容描述
 *     ----------------------------------------------
 *     1.0.0 	       庄梦蝶殇 	2019年3月27日             
 *     ----------------------------------------------
 * </pre>
 */
public class ZkUtil {
    /**
     * zk节点分隔符
     */
    public final static String PATH_SPILT = "/";
    
    /**
     * 计数器根目录
     */
    public static final String COUNT_PATH = "/count";
    
    /**
     * @方法名称 getSortedChildren
     * @功能描述 <pre>获取子节点列表，并由小到大排序</pre>
     * @param client zk客户端对象
     * @param basePath 顺序节点根目录
     * @param lockName 锁名
     * @return 子节点列表
     */
    public static List<String> getSortedChildren(ZooKeeper client, String basePath, final String lockName) {
        try {
            if (null == client.exists(basePath, false)) {
                return null;
            }
            List<String> children = client.getChildren(basePath, false);
            Collections.sort(children, new Comparator<String>() {
                @Override
                public int compare(String lhs, String rhs) {
                    return getLockNodeNumber(lhs, lockName).compareTo(getLockNodeNumber(rhs, lockName));
                }
            });
            return children;
        } catch (InterruptedException | KeeperException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * @方法名称 getLockNodeNumber
     * @功能描述 <pre>获取节点版本标识</pre>
     * @param str 节点完整名(含版本标识)
     * @param lockName 节点名
     * @return 节点版本标识
     */
    public static String getLockNodeNumber(String str, String lockName) {
        int index = str.lastIndexOf(lockName);
        if (index >= 0) {
            index += lockName.length();
            return index <= str.length() ? str.substring(index) : "";
        }
        return str;
    }
}
