package com.huoli.trip.supplier.self.hllx.vo;

import com.huoli.trip.common.vo.request.TraceRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class HllxOrderOperationRequest  extends TraceRequest implements Serializable {
    /**
     * 订单编号
     */
    private String orderId;
    /**
     * 旧的状态
     */
    private int oldStatus;
    /**
     * 新订单状态
     */
    private int newStatus;
    /**
     * 操作人
     */
    private String operator;
    /**
     * 操作时间
     */
    private String updateTime;
    /**
     * 说明
     */
    private String explain;

    private List<HllxVoucher> vochers;



}
