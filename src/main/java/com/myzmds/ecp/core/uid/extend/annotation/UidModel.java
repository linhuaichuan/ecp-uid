package com.myzmds.ecp.core.uid.extend.annotation;

/**
 * @类名称 UidModel.java
 * @类描述 <pre>id生产模式</pre>
 * @作者  庄梦蝶殇 linhuaichuan1989@126.com
 * @创建时间 2018年9月3日 上午9:54:09
 * @版本 1.00
 *
 * @修改记录
 * <pre>
 *     版本                       修改人 		修改日期 		 修改内容描述
 *     ----------------------------------------------
 *     1.00 	庄梦蝶殇 	2018年9月3日             
 *     ----------------------------------------------
 * </pre>
 */
public enum UidModel {
    /**
     * 步长自增(空实现,依赖数据库步长设置)
     */
    step("step"),
    /**
     * 分段批量(基于leaf)
     */
    segment("segment"),
    /**
     * Snowflake算法(源自twitter)
     */
    snowflake("snowflake"),
    /**
     * 百度UidGenerator
     */
    baidu("baidu");
    
    private UidModel(String name) {
        this.name = name;
    }
    
    private String name;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
