package com.myzmds.ecp.core.uid.util;

import java.io.File;
import java.io.IOException;

public class ZkNodeUtils {
    
    public static final String WORKER_SPLIT = "-";
    /**
     * @方法名称 getPid
     * @功能描述 <pre>查找pid文件，根据前缀获取workid</pre>
     * @param pidHome
     * @param prefix
     * @return
     */
    public static String getPid(String pidHome, String prefix) {
        String pid = null;
        File home = new File(pidHome);
        if (home.exists() && home.isDirectory()) {
            File[] files = home.listFiles();
            for (File file : files) {
                if (file.getName().startsWith(prefix)) {
                    pid = file.getName();
                    break;
                }
            }
            if (null != pid) {
                return pid.substring(pid.lastIndexOf(WORKER_SPLIT) + 1);
            }
        } else {
            home.mkdirs();
        }
        return pid;
    }
    
    public static void sleepMs(long ms, long diff) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            
        }
        diff += ms;
        if (diff < 0) {
            sleepMs(ms, diff);
        }
    }

    public static void writePidFile(String name) {
        File pidFile = new File(name);
        try {
            pidFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
