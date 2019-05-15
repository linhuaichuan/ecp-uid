package com.myzmds.ecp.core.uid.idleaf;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.myzmds.ecp.core.uid.leaf.SegmentServiceImpl;

import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import net.sourceforge.groboutils.junit.v1.TestRunnable;

/**
 * @类名称 SegmentServiceImplTest.java
 * @类描述 <pre>Segment多线程并发测试</pre>
 * @作者  庄梦蝶殇 linhuaichuan1989@126.com
 * @创建时间 2019年3月6日 下午4:42:59
 * @版本 1.0.0
 *
 * @修改记录
 * <pre>
 *     版本                       修改人 		修改日期 		 修改内容描述
 *     ----------------------------------------------
 *     1.0.0 	       庄梦蝶殇 	2019年3月6日             
 *     ----------------------------------------------
 * </pre>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/idleaf/app-leaf.xml"})
public class SegmentServiceImplTest extends Thread {
    
    @Autowired
    SegmentServiceImpl segmentServiceImpl;
    
    @Test
    public void testSych() {
        TestRunnable runner = new TestRunnable() {
            @Override
            public void runTest()
                throws Throwable {
                System.out.println(Thread.currentThread().getName() + ":" + segmentServiceImpl.getId());
            }
        };
        // 开13，23，43个线程进行测试,step设置为10
        int runnerCount = 43;
        TestRunnable[] trs = new TestRunnable[runnerCount];
        for (int i = 0; i < runnerCount; i++) {
            trs[i] = runner;
        }
        
        MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(trs);
        try {
            mttr.runTestRunnables();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        
    }
    
    class LeafThread extends Thread {
        @Override
        public void run() {
            System.out.println(this.getName() + "的id：" + segmentServiceImpl.getId());
        }
    }
}