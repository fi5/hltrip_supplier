package com.huoli.trip.supplier.self.lvmama.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2021/3/1515:26
 */
@Data
public class Recipient implements Serializable {
    /**
     * 收件人
     */
    private String recipients;
    /**
     * 根据商品分
     * 类决定
     * 收件手机号
     */
    private String contactNumber;
    /**
     * 根据商品分
     * 类决定
     * 地址
     */
    private String address;
    /**
     * 根据商品分
     * 类决定
     * 邮编
     */
    private String postcode;
}
