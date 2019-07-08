package com.myzmds.ecp.core.uid.util;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

/**
 * @类名称 WorkerIdUtils.java
 * @类描述 <pre>workid 文件操作类</pre>
 * @作者  庄梦蝶殇 linhuaichuan1989@126.com
 * @创建时间 2019年1月16日 下午2:31:31
 * @版本 1.0.0
 *
 * @修改记录
 * <pre>
 *     版本                       修改人 		修改日期 		 修改内容描述
 *     ----------------------------------------------
 *     1.0.0 		庄梦蝶殇 	2019年1月16日             
 *     ----------------------------------------------
 * </pre>
 */
public class WorkerIdUtils {
    /**
     * workerID文件 分隔符
     */
    public static final String WORKER_SPLIT = "_";
    
    /**
     * @方法名称 getPidName
     * @功能描述 <pre>获取workId文件名</pre>
     * @param pidPort 使用端口(同机多uid应用时区分端口)
     * @param socket 
     * @return
     */
    public static String getPidName(Integer pidPort, ServerSocket socket) {
        String pidName = NetUtils.getLocalInetAddress().getHostAddress();
        if (-1 != pidPort) {
            // 占用端口
            pidPort = null != pidPort && pidPort > 0 ? pidPort : NetUtils.getAvailablePort();
            try {
                socket = new ServerSocket(pidPort);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return pidName + WorkerIdUtils.WORKER_SPLIT + pidPort;
    }
    
    /**
     * @方法名称 getPid
     * @功能描述 <pre>查找pid文件，根据前缀获取workid</pre>
     * @param pidHome workerID文件存储路径
     * @param prefix workerID文件前缀
     * @return workerID值
     */
    public static Long getPid(String pidHome, String prefix) {
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
                return Long.valueOf(pid.substring(pid.lastIndexOf(WORKER_SPLIT) + 1));
            }
        } else {
            home.mkdirs();
        }
        return null;
    }
    
    /**
     * @方法名称 sleepMs
     * @功能描述 <pre>回拨时间睡眠等待</pre>
     * @param ms 平均心跳时间
     * @param diff 回拨差时间
     */
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
    
    /**
     * @方法名称 writePidFile
     * @功能描述 <pre>创建workerID文件(workerID文件已经存在,则不创建,返回一个false；如果没有,则返回true)</pre>
     * @param name
     */
    public static void writePidFile(String name) {
        File pidFile = new File(name);
        try {
            pidFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
