package com.huoli.trip.supplier.self.yaochufa.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 描述: <br>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：王德铭<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/19<br>
 */
public class YcfBookGuest implements Serializable {
    //中文姓名(套餐预定规则要求，则必填)
    private String cName;
    //英文姓名(套餐预定规则要求，则必填)
    private String eName;
    //手机号码(套餐预定规则要求，则必填)
    private String mobile;
    //邮箱(套餐预定规则要求，则必填)
    private String email;
    //证件类型(套餐预定规则要求，则必填)
    private int credentialType;
    //证件号(套餐预定规则要求，则必填)
    private String credential;

    public String getCname() {
        return cName;
    }

    public void setCname(String cName) {
        this.cName = cName;
    }

    public String getEname() {
        return eName;
    }

    public void setEname(String eName) {
        this.eName = eName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getCredentialType() {
        return credentialType;
    }

    public void setCredentialType(int credentialType) {
        this.credentialType = credentialType;
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }
}
