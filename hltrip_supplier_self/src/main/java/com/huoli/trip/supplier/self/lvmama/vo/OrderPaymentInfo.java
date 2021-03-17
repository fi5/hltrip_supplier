package com.huoli.trip.supplier.self.lvmama.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2021/3/1517:29
 */
@Data
public class OrderPaymentInfo implements Serializable {
    private String partnerOrderNo;
    private String orderId;
    private String serialNum;
}
