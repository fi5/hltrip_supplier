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
public class OrderCancelRequest extends BaseRequest{
    private String PartnerOrderNo;
    private String orderId;
}
