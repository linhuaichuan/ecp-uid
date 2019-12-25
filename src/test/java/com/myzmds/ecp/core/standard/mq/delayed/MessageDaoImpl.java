package com.myzmds.ecp.core.standard.mq.delayed;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

/**
 * @类名称 MessageDaoImpl.java
 * @类描述 <pre>消息数据接口实现类：依赖spring-jdbc框架，可替换</pre>
 * @作者  庄梦蝶殇 linhuaichuan1989@126.com
 * @创建时间 2019年12月25日 下午3:21:42
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
public class MessageDaoImpl implements IMessageDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public final static String SQL_INSERT = "INSERT INTO base_msg_delaye(id, subject, publish_time, content) VALUES (?, ?, ?, ?)";
    
    public final static String SQL_SELECT = "select * from base_msg_delaye where id in (%s)";
    
    public final static String SQL_DELETE = "delete base_msg_delaye where id in (%s)";
    
    @Override
    public void insert(Message queue) {
        this.jdbcTemplate.update(SQL_INSERT, new Object[] {queue.getId(), queue.getSubject(), queue.getPublishTime(), queue.getContent()});
    }
    
    @Override
    public List<Message> select(String[] ids) {
        List<Message> list = new ArrayList<Message>(ids.length);
        jdbcTemplate.query(String.format(SQL_SELECT, String.join(",", ids)), new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs)
                throws SQLException {
                Message queue = new Message();
                queue.setId(rs.getString("id"));
                queue.setSubject(rs.getString("subject"));
                queue.setPublishTime(rs.getLong("publish_time"));
                queue.setContent(rs.getString("content"));
                list.add(queue);
            }
        });
        return list;
    }
    
    @Override
    public void delete(String[] ids) {
        this.jdbcTemplate.update(String.format(SQL_SELECT, String.join(",", ids)));
        
    }
}
