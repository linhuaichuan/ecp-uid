package com.myzmds.ecp.core.standard.mq.delayed;

/**
 * @类名称 Message.java
 * @类描述 <pre>消息体对象</pre>
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
public class Message {
    public String id;
    
    public String subject;
    
    public Long publishTime;
    
    public String content;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public Long getPublishTime() {
        return publishTime;
    }
    
    public void setPublishTime(Long publishTime) {
        this.publishTime = publishTime;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
}
