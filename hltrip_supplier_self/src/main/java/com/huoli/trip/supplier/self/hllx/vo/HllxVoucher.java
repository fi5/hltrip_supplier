package com.huoli.trip.supplier.self.hllx.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class HllxVoucher implements Serializable {
    /**
     * 凭证信息
     */
    private String voucherInfo;

    /**
     * 入园方式
     * 0凭兑换凭证直接入园
     * 1凭兑换凭证换票入园
     */
    private Integer inType;

    /**
     *   附件类型 1.纯文本  2.二维码 3.PDF 4.手机号 5.身份证 6.二维码或身份证 7.数字码 8.其他证件
     */
    private Integer type;

    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 更新时间
     */
    private String updateTime;


}
