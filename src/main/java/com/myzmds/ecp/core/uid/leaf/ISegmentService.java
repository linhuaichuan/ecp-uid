package com.myzmds.ecp.core.uid.leaf;

/**
 * @类名称 IdLeafService.java
 * @类描述 <pre>Leaf理论的分段批量id生成服务</pre>
 * @作者 庄梦蝶殇 linhuaichuan1989@126.com
 * @创建时间 2018年9月5日 上午11:46:37
 * @版本 1.00
 *
 * @修改记录
 * <pre>
 *     版本                       修改人 		修改日期 		 修改内容描述
 *     ----------------------------------------------
 *     1.00 	庄梦蝶殇 	2018年9月5日             
 *     ----------------------------------------------
 * </pre>
 */
public interface ISegmentService {
    /**
     * 获取id
     * @return id
     */
    public Long getId();

    /**
     * 设置业务标识
     * @param bizTag 业务标识
     */
    public void setBizTag(String bizTag);
}
