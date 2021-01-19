package com.huoli.trip.supplier.self.difengyun.vo.push;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:订单变更推送
 * @date 2020/12/1014:56
 */
@Data
public class DfyOrderPushRequest implements Serializable {
    private static final long serialVersionUID = 6202899833814899182L;
    private OrderInfo data;

    @Data
    public static class OrderInfo implements Serializable {
        private String orderId;
    }
}
