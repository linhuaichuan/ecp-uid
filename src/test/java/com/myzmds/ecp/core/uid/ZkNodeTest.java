package com.myzmds.ecp.core.uid;

import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.myzmds.ecp.core.uid.worker.WorkerIdAssigner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/app-zk.xml"})
public class ZkNodeTest {
    @Autowired
    private WorkerIdAssigner assigner;
    
    @Test
    public void testZkNode()
        throws Exception {
        System.out.println(assigner.assignWorkerId());
        for (int i = 0; i < 5; i++) {
            Thread.sleep(6000);
        }
        Thread.sleep(Integer.MAX_VALUE);
    }
    
    /** zookeeper地址 */
    static final String CONNECT_ADDR = "172.16.51.123:2181";
    
    /** connection超时时间 单位ms */
    static final int CONNECTION_OUTTIME = 5000;
    
    public static void testZk()
        throws Exception {
        ZkClient zkc = new ZkClient(new ZkConnection(CONNECT_ADDR), CONNECTION_OUTTIME);
        zkc.deleteRecursive("/test");
        // 对父节点添加监听子节点变化。
        zkc.subscribeChildChanges("/test", new IZkChildListener() {
            @Override
            public void handleChildChange(String parentPath, List<String> currentChilds)
                throws Exception {
                System.out.println("parentPath: " + parentPath);
                System.out.println("currentChilds: " + currentChilds);
            }
        });
        Thread.sleep(3000);
        zkc.createPersistent("/test");
        Thread.sleep(5000);
        // parentPath: /test
        // currentChilds: []
        zkc.createPersistent("/test" + "/" + "aa", "aa内容");
        Thread.sleep(5000);
        // parentPath: /test
        // currentChilds: [aa]
        zkc.createPersistent("/test" + "/" + "bb", "bb内容");
        Thread.sleep(5000);
        // parentPath: /test
        // currentChilds: [aa, bb]
        zkc.delete("/test/bb");
        Thread.sleep(5000);
        // parentPath: /test
        // currentChilds: [aa]
        zkc.deleteRecursive("/test");// 因为此方法为递归删除，所以触发2次
        // parentPath: /test
        // currentChilds: null //删除后，子节点集合为null
        // parentPath: /test
        // currentChilds: null
        Thread.sleep(Integer.MAX_VALUE);
    }
}
