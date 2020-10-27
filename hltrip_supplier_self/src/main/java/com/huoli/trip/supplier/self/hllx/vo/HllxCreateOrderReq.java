package com.huoli.trip.supplier.self.hllx.vo;

import com.huoli.trip.common.vo.request.TraceRequest;
import lombok.Data;

import java.io.Serializable;

@Data
public class HllxCreateOrderReq  extends TraceRequest implements Serializable {
    private String productId;
    private int qunatity;
    private String date;
}
