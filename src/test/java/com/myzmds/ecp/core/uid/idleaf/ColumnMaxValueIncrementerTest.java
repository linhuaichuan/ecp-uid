package com.myzmds.ecp.core.uid.idleaf;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author sunff
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/idleaf/app-springId.xml" })
public class ColumnMaxValueIncrementerTest {

    @Autowired
    @Qualifier("productNoIncrementer")
    private DataFieldMaxValueIncrementer incrementer;

    @Test
    public void test() {
        int i = 0;
        while (i < 10) {
            System.out.println("long id=" + incrementer.nextLongValue());
            System.out.println("int id=" + incrementer.nextIntValue());
            System.out.println("string id=" + incrementer.nextStringValue());
            i++;
        }
    }
}
