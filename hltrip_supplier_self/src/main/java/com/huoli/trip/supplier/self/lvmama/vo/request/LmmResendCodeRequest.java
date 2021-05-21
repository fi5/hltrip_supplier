package com.huoli.trip.supplier.self.lvmama.vo.request;

import lombok.Data;

@Data
public class LmmResendCodeRequest extends LmmBaseRequest {
    /**
     * 分销商订单号
     */
    private String partnerOrderNo;

    /**
     * 驴妈妈订单号
     */
    private String orderId;

    public LmmResendCodeRequest(String partnerOrderNo, String orderId) {
        this.partnerOrderNo = partnerOrderNo;
        this.orderId = orderId;
    }
}
