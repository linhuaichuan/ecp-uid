package com.myzmds.ecp.core.uid.leaf;

import java.util.Date;

/**
 * @类名称 IdSegment.java
 * @类描述 <pre>分段ID实体</pre>
 * @作者 庄梦蝶殇 linhuaichuan1989@126.com
 * @创建时间 2018年9月6日 下午3:58:29
 * @版本 1.00
 *
 * @修改记录
 * <pre>
 *     版本                       修改人 		修改日期 		 修改内容描述
 *     ----------------------------------------------
 *     1.00 	庄梦蝶殇 	2018年9月6日             
 *     ----------------------------------------------
 * </pre>
 */
public class IdSegment {
    /**
     * 最小id
     */
    private Long minId;
    
    /**
     * 最大id
     */
    private Long maxId;
    
    /**
     * 步长
     */
    private Long step;
    
    /**
     * 中间值(缓存阈值-用于更新双buffer的阈值。目前阈值比是50%)
     */
    private Long middleId;
    
    /**
     * 上次更新时间
     */
    private Date lastUpdateTime;
    
    /**
     * 本次更新时间
     */
    private Date currentUpdateTime;
    
    public IdSegment() {
        
    }
    
    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }
    
    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
    
    public Date getCurrentUpdateTime() {
        return currentUpdateTime;
    }
    
    public void setCurrentUpdateTime(Date currentUpdateTime) {
        this.currentUpdateTime = currentUpdateTime;
    }
    
    public Long getMiddleId() {
        if (this.middleId == null) {
            this.middleId = this.maxId - (long)Math.round(step / 2);
        }
        return middleId;
    }
    
    public Long getMinId() {
        if (this.minId == null) {
            if (this.maxId != null && this.step != null) {
                this.minId = this.maxId - this.step;
            } else {
                throw new RuntimeException("maxid or step is null");
            }
        }
        return minId;
    }
    
    public Long getMaxId() {
        return maxId;
    }
    
    public void setMaxId(Long maxId) {
        this.maxId = maxId;
    }
    
    public Long getStep() {
        return step;
    }
    
    public void setStep(Long step) {
        this.step = step;
    }
}