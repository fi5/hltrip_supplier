package com.huoli.trip.supplier.self.lvmama.vo.request;

import com.huoli.trip.common.vo.request.TraceRequest;
import lombok.Data;

import java.io.Serializable;

@Data
public class LmmOrderDetailRequest extends TraceRequest implements Serializable {

    private LmmOrderReq order;

    @Data
    public static class  LmmOrderReq{
        private String partnerOrderNos;//分销商订单编号
    }

}
