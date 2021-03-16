package com.huoli.trip.supplier.self.lvmama.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2021/3/1517:25
 */
@Data
public class CreateOrderInfo implements Serializable {
    private String orderId;
    /**
     * 正常：NORMAL
     * 取消：CANCEL
     */
    private String status;
    /**
     * 未支付：UNPAY，
     * 已支付：PAYED
     * 部分支付：PARTPAY
     */
    private String paymentStatus;

    /**
     * 支付等待时间 yyyy-MM-dd HH:mm:ss
     */
    private String waitPaymentTime;
}
