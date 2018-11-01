package com.myzmds.ecp.core.uid.worker;

/**
 * @类名称 SimpleWorkerIdAssigner.java
 * @类描述 <pre>简单编号分配器(即不用workerId)</pre>
 * @作者 庄梦蝶殇 linhuaichuan1989@126.com
 * @创建时间 2018年9月5日 上午11:43:45
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
public class SimpleWorkerIdAssigner implements WorkerIdAssigner {
    
    @Override
    public long assignWorkerId() {
        return 0;
    }
}
