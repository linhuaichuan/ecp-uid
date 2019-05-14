package com.myzmds.ecp.core.uid.baidu;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.myzmds.ecp.core.uid.UidContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:uid/simple-uid-baidu.xml"})
public class SimpleUidTest {
    @Autowired
    private UidContext context;

    @Test
    public void test() {
        System.out.println(context.getUID());
        System.out.println("test:" +context.getUID("test"));
        System.out.println(context.getUID("qwer"));
        System.out.println("test:" +context.getUID("test"));
    }
}
