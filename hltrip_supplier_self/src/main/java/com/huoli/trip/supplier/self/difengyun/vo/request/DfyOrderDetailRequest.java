package com.huoli.trip.supplier.self.difengyun.vo.request;

import com.huoli.trip.common.vo.request.TraceRequest;
import lombok.Data;

import java.io.Serializable;

@Data
public class DfyOrderDetailRequest  extends TraceRequest implements Serializable {

    /**
     * supplier订单IDD
     */
    private String orderId;
}
