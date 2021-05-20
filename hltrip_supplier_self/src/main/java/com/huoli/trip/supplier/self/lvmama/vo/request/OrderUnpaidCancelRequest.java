package com.huoli.trip.supplier.self.lvmama.vo.request;

import lombok.Data;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2021/3/1517:32
 */
@Data
public class OrderUnpaidCancelRequest extends LmmBaseRequest {
    private String partnerOrderNo;
    private String orderId;

    public OrderUnpaidCancelRequest(String partnerOrderNo, String orderId) {
        this.partnerOrderNo = partnerOrderNo;
        this.orderId = orderId;
    }
}
