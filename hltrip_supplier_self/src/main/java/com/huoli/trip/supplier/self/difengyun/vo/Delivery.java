package com.huoli.trip.supplier.self.difengyun.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description: 笛风配送信息
 * @date 2020/12/1011:00
 */
@Data
public class Delivery implements Serializable {
    private static final long serialVersionUID = -3615440669828581370L;
    /**
     * 1.配送
     */
    private Integer deliveryType;

    /**
     * 收件人
     *deliveryType=1时，必填；
     */
    private String receiverName;

    /**
     * 收件电话；deliveryType=1时，必填；
     */
    private String telNum;

    /**
     * 收件地址；deliveryType=1时，必填；
     */
    private String deliveryEndAddress;

    /**
     * 邮编；deliveryType=1时，必填；
     */
    private String zipCode;
}
