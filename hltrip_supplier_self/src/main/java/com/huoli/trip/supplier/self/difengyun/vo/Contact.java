package com.huoli.trip.supplier.self.difengyun.vo;


import lombok.Data;

import java.io.Serializable;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2020/12/119:39
 */
@Data
public class Contact implements Serializable {
    private static final long serialVersionUID = 5729446386648730437L;
    /**
     * 取票人姓名
     */
    private  String contactName;
    /**
     * 取票人邮箱
     *
     * 当“门票详情接口->admissionVoucherCode”=205或302时必传，否则不要传。
     */
    private String contactEmail;
    /**
     * 取票人手机号码（入园凭证会发送到此手机号码上，请务必保证手机号码真实有效）
     */
    private  String contactTel;
    /**
     * 证件类型。
     *
     * 当“门票详情接口->custInfoLimit”=4、6、7时必传；否则不要传。
     *
     * 选项参照“门票详情接口->certificateType”字段。
     */
    private  Integer psptType;
    /**
     * 证件号码。
     * 当“门票详情接口->custInfoLimit”=4、6、7时必传；否则不要传。
     *
     */
    private String psptId;

}
