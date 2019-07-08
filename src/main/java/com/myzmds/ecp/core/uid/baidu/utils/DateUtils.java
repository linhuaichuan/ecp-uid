package com.myzmds.ecp.core.uid.baidu.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @类名称 DateUtils.java
 * @类描述 <pre>简单时间辅助类(SimpleDateFormat是线程不安全的，因此做了静态锁处理)</pre>
 * @作者  庄梦蝶殇 linhuaichuan1989@126.com
 * @创建时间 2019年7月8日 上午11:15:29
 * @版本 1.0.0
 *
 * @修改记录
 * <pre>
 *     版本                       修改人 		修改日期 		 修改内容描述
 *     ----------------------------------------------
 *     1.0.0 	庄梦蝶殇 	2019年7月8日             
 *     ----------------------------------------------
 * </pre>
 */
public class DateUtils {
    /**
     * 日期-格式
     */
    public static final String DAY_PATTERN = "yyyy-MM-dd";
    
    /**
     * 日期时间-格式
     */
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * 日期时间(带毫秒)-格式
     */
    public static final String DATETIME_MS_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    
    /**
     * 加锁的df集合
     */
    private static ThreadLocal<Map<String, DateFormat>> dfMap = new ThreadLocal<Map<String, DateFormat>>() {
        @Override
        protected Map<String, DateFormat> initialValue() {
            return new HashMap<String, DateFormat>(10);
        }
    };
    
    /**
     * @方法名称 getDateFormat
     * @功能描述 <pre>返回一个DateFormat,每个线程只会new一次pattern对应的sdf</pre>
     * @param pattern 格式 表达式
     * @return DateFormat对象
     */
    private static DateFormat getDateFormat(final String pattern) {
        Map<String, DateFormat> tl = dfMap.get();
        DateFormat df = tl.get(pattern);
        if (df == null) {
            df = new SimpleDateFormat(pattern);
            tl.put(pattern, df);
        }
        return df;
    }
    
    /**
     * @方法名称 formatByDateTimePattern
     * @功能描述 <pre>获取'yyyy-MM-dd HH:mm:ss'格式的时间字符串</pre>
     * @param date 时间对象
     * @return 时间字符串
     */
    public static String formatByDateTimePattern(Date date) {
        return getDateFormat(DATETIME_PATTERN).format(date);
    }
    
    /**
     * @方法名称 parseByDayPattern
     * @功能描述 <pre>解析'yyyy-MM-dd'格式的时间</pre>
     * @param str 时间字符串
     * @return 时间对象
     */
    public static Date parseByDayPattern(String str) {
        return parseDate(str, DAY_PATTERN);
    }
    
    /**
     * @方法名称 parseDate
     * @功能描述 <pre>解析指定格式的时间</pre>
     * @param str 时间字符串
     * @param pattern 格式表达式
     * @return 时间对象
     */
    public static Date parseDate(String str, String pattern) {
        try {
            return getDateFormat(pattern).parse(str);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
