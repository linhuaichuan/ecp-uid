package com.myzmds.ecp.core.uid.baidu;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:uid/zk-uid-baidu.xml"})
public class UidTest {
    @Autowired
    private UidGenerator uidOne;

    @Autowired
    private UidGenerator uidTwo;
    
    @Test
    public void test() {
        // Generate UID
        long uid = uidOne.getUID();
        System.out.println("one:" + uid);
        System.out.println("one:" + uidOne.getUID());
        // Parse UID into [Timestamp, WorkerId, Sequence]
        // {"UID":"180363646902239241","parsed":{ "timestamp":"2017-01-19 12:15:46", "workerId":"4", "sequence":"9" }}
        System.out.println("one:" + uidOne.parseUID(uid));
        
        Runnable threadOne= new  Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    System.out.println("one:" + uidOne.getUID());
                }
            }
        };

        Runnable threadTwo= new  Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    System.out.println("two:" + uidTwo.getUID());
                }
            }
        };
        threadOne.run();
        threadTwo.run();
        
        uid = uidTwo.getUID();
        System.out.println("two:" + uid);
        System.out.println("two:" + uidTwo.getUID());
        // Parse UID into [Timestamp, WorkerId, Sequence]
        // {"UID":"180363646902239241","parsed":{ "timestamp":"2017-01-19 12:15:46", "workerId":"4", "sequence":"9" }}
        System.out.println("two:" + uidTwo.parseUID(uid));
    }
}
