package com.huoli.trip.supplier.self.hllx.vo;

import com.huoli.trip.common.vo.request.TraceRequest;
import lombok.Data;

import java.io.Serializable;

@Data
public class HllxPayOrderReq extends TraceRequest implements Serializable {
    private String channelCode;
    private String channelOrderId;
}
