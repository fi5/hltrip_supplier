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
public class OrderUnpaidCancelRequest extends BaseRequest{
    private String PartnerOrderNo;
    private String orderId;

    public OrderUnpaidCancelRequest(String partnerOrderNo, String orderId) {
        PartnerOrderNo = partnerOrderNo;
        this.orderId = orderId;
    }
}
