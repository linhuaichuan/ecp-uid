package com.myzmds.ecp.core.uid.idleaf;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.myzmds.ecp.core.uid.leaf.SegmentServiceImpl;

import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import net.sourceforge.groboutils.junit.v1.TestRunnable;

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
        // 开13个线程，模拟13个用户
        int runnerCount = 23;
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