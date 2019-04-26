package com.myzmds.ecp.core.uid.baidu;

import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;

import com.myzmds.ecp.core.uid.baidu.UidGenerator;

/**
 * @类名称 UidGeneratorProvider.java
 * @类描述 <pre>UidGenerator多例对象提供器</pre>
 * @作者 庄梦蝶殇 linhuaichuan1989@126.com
 * @创建时间 2019年3月15日 下午2:05:47
 * @版本 1.0.0
 *
 * @修改记录
 * <pre>
 *     版本                       修改人 		修改日期 		 修改内容描述
 *     ----------------------------------------------
 *     1.0.0 	       庄梦蝶殇 	2019年3月15日             
 *     ----------------------------------------------
 * </pre>
 */
@Component("uidGeneratorProvider")
public abstract class UidGeneratorProvider {
    /**
     * @方法名称 get
     * @功能描述 <pre>获取uidGenerator 对象</pre>
     */
    @Lookup("uidGenerator")
    public abstract UidGenerator get();
}
