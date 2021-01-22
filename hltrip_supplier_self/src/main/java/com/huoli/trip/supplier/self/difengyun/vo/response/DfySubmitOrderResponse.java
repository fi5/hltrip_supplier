package com.huoli.trip.supplier.self.difengyun.vo.response;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:代扣返回
 * @date 2020/12/1415:12
 */
@Data
public class DfySubmitOrderResponse implements Serializable {
    private static final long serialVersionUID = 1932598646144291542L;
    private String orderId;
    /**
     * 代扣交易流水号
     */
    private String outTradeNo;
}
