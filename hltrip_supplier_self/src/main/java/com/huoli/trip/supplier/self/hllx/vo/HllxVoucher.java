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
     * 凭证类型
     * 1.纯文本  2.二维码 3.PDF 4.手机尾号
     */
    private int type;

    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 更新时间
     */
    private String updateTime;


}
