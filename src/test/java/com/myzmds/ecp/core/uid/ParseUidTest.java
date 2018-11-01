package com.myzmds.ecp.core.uid;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import com.myzmds.ecp.core.uid.baidu.impl.DefaultUidGenerator;
import com.myzmds.ecp.core.uid.extend.strategy.TwitterSnowflakeStrategy;
import com.myzmds.ecp.core.uid.twitter.SnowflakeIdWorker;
import com.myzmds.ecp.core.uid.worker.SimpleWorkerIdAssigner;

public class ParseUidTest {
    public static void testParseUid() {
        System.out.println(new SnowflakeIdWorker(10, 12).nextId());
        System.out.println(new SnowflakeIdWorker(10, 12).parseUID("91543914239533056"));
        System.out.println(new SnowflakeIdWorker(10, 12).parseUID(91543914239533056L));
        
        // 百度
        DefaultUidGenerator uidGenerator = new DefaultUidGenerator();
        uidGenerator.setWorkerIdAssigner(new SimpleWorkerIdAssigner());
        uidGenerator.setSeqBits(13);
        uidGenerator.setTimeBits(29);
        uidGenerator.setWorkerBits(21);
        uidGenerator.setEpochStr("2017-12-25");
        try {
            uidGenerator.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(uidGenerator.getUID());
        System.out.println(uidGenerator.parseUID(374964732433530880L));
    }

    public static void testBuildRule() {
        Long did = TwitterSnowflakeStrategy.getMachineNum(31L);
        System.out.println(did);
        System.out.println(TwitterSnowflakeStrategy.getProcessNum(did, 31L));
        System.out.println(getMachineNum(31L));
    }
    
    public static void main(String[] args) {
        testBuildRule();
        try {
//            testZkNode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static long getMachineNum(long maxWorkerId) {
        StringBuilder sb = new StringBuilder();
        Enumeration<NetworkInterface> e = null;
        try {
            e = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e1) {
            e1.printStackTrace();
        }
        while (e.hasMoreElements()) {
            NetworkInterface ni = e.nextElement();
            sb.append(ni.toString());
        }
        return (sb.toString().hashCode() & 0xffff) % (maxWorkerId + 1);
    }

}
