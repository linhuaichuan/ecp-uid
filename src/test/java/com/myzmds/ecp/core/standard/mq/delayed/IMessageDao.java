package com.myzmds.ecp.core.standard.mq.delayed;

import java.util.List;

/**
 * @类名称 IMessageDao.java
 * @类描述 <pre>消息数据接口</pre>
 * @作者  庄梦蝶殇 linhuaichuan@veredholdings.com
 * @创建时间 2019年12月25日 下午3:21:27
 * @版本 1.0.0
 *
 * @修改记录
 * <pre>
 *     版本                       修改人 		修改日期 		 修改内容描述
 *     ----------------------------------------------
 *     1.0.0 	       庄梦蝶殇 	2019年12月25日             
 *     ----------------------------------------------
 * </pre>
 */
public interface IMessageDao {
    
    public void insert(Message queue);
    
    public List<Message> select(String[] ids);
    
    public void delete(String[] ids);
}
