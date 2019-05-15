package com.myzmds.ecp.core.uid.extend.strategy;

import com.myzmds.ecp.core.uid.extend.annotation.UidModel;
import com.myzmds.ecp.core.uid.leaf.ISegmentService;
import com.myzmds.ecp.core.uid.spring.ColumnMaxValueIncrementer;

/**
 * @类名称 SpringStrategy.java
 * @类描述 <pre>spring 分段批量Id策略(可配置asynLoadingSegment-异步标识)</pre>
 * @作者  庄梦蝶殇 linhuaichuan1989@126.com
 * @创建时间 2019年3月15日 下午7:48:58
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
public class SpringStrategy extends LeafSegmentStrategy {
    @Override
    public UidModel getName() {
        return UidModel.step;
    }
    
    @Override
    public ISegmentService getSegmentService(String prefix) {
        ISegmentService segmentService = generatorMap.get(prefix);
        if (null == segmentService) {
            synchronized (generatorMap) {
                if (null == segmentService) {
                    segmentService = new ColumnMaxValueIncrementer(jdbcTemplate, prefix);
                }
                generatorMap.put(prefix, segmentService);
            }
        }
        return segmentService;
    }
    
}
