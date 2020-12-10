package com.huoli.trip.supplier.self.difengyun.vo.response;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lunatic
 * @Title: DfyCreateOrderResponse
 * @Package
 * @Description: 笛风创建订单返回
 * @date 2020/12/1011:04
 */
@Data
public class DfyCreateOrderResponse implements Serializable {
    private static final long serialVersionUID = -5019457817390047704L;
    /**
     * 笛风订单号
     */
    private String orderId;

    /**
     * 笛风订单号是否新生成，1：是，0：否。
     *
     * sourceOrderId非空时，可防止重复下单。
     *
     * 若重复下单，则返回之前对应已生成的orderId，且isNewFlag=0；其它情况isNewFlag=1。
     */
    private Integer isNewFlag;
}
