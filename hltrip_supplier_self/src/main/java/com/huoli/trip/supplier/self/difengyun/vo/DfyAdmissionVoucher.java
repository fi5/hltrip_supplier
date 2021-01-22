package com.huoli.trip.supplier.self.difengyun.vo;

import lombok.Data;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/12/8<br>
 */
@Data
public class DfyAdmissionVoucher {

    /**
     * 入园方式编码
     * 1实体票入园，
     * 201换票入园短信，
     * 202换票入园二维码，
     * 203换票入园数字码，
     * 204换票入园换票证，
     * 205换票入园邮件，
     * 206换票入园身份证，
     * 207换票入园护照，
     * 208换票入园港澳通行证，
     * 209换票入园军官证，
     * 210换票入园台胞证，
     * 301直接验证入园二维码，
     * 302直接验证入园邮件，
     * 303直接验证入园身份证，
     * 304直接验证入园护照，
     * 305直接验证入园港澳通行证，
     * 306直接验证入园军官证，
     * 307直接验证入园台胞证
     */
    private String admissionVoucherCode;

    /**
     * 入园方式描述
     */
    private String admissionVoucherDesc;

    /**
     * 短信内容
     */
    private String smsMessage;
}
