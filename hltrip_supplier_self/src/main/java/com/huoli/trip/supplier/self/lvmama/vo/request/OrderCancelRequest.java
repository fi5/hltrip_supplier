package com.huoli.trip.supplier.self.lvmama.vo.request;

import lombok.Data;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2021/3/1517:36
 */
@Data
public class  OrderCancelRequest extends LmmBaseRequest {
    private String partnerOrderNo;
    private String orderId;

    public OrderCancelRequest(String partnerOrderNo, String orderId) {
        this.partnerOrderNo = partnerOrderNo;
        this.orderId = orderId;
    }
}
