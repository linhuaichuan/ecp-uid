package com.myzmds.ecp.core.standard.mq.delayed;

import java.util.List;

/**
 * @类名称 IDelayeQueue.java
 * @类描述 <pre>延迟队列接口</pre>
 * @作者  庄梦蝶殇 linhuaichuan1989@126.com
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
public interface IDelayMsgService {
    /**
     * @方法名称 push
     * @功能描述 <pre>发布延迟消息</pre>
     * @param obj 消息体
     */
    public void push(Message queue);
    
    /**
     * @方法名称 pull
     * @功能描述 <pre>拉取过期消息</pre>
     * @return 过期消息列表
     */
    public List<Message> pull();
    
    /**
     * @方法名称 ack
     * @功能描述 <pre>消费成功</pre>
     * @param ids 被消费成功的
     */
    public void ack(List<String> ids);
    
}
