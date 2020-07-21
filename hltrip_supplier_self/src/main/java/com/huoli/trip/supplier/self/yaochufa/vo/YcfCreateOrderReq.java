package com.huoli.trip.supplier.self.yaochufa.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 描述: <br>创建订单请求
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：王德铭<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/19<br>
 */
public class YcfCreateOrderReq implements Serializable {
    //【合】订单号
    private String partnerOrderId;
    //要】产品编号
    private String productId;
    //产品名称
    private String productName;
    //产品数量
    private int qunatity;
    //总价，必须>0(底价模式：财务结算总价；)
    private BigDecimal amount;
    //销售总价（底价模式：合作商销售总价，必填）
    private BigDecimal sellAmount;
    //联系人中文姓名(套餐预定规则要求，则必填)
    private String cName;
    //联系人英文姓名(套餐预定规则要求，则必填)
    private String eName;
    //联系人手机(套餐预定规则要求，则必填)
    private String mobile;
    //联系人邮箱(套餐预定规则要求，则必填)
    private String email;
    //联系人证件号(套餐预定规则要求，则必填)
    private String credential;
    //联系人证件类型
    private int credentialType;
    //房资源组(房/票/餐，不可同时为空)
    private List<YcfBookRoom> roomDetail;
    //门票资源组(房/票/餐，不可同时为空)
    private List<YcfBookTicket> ticketDetail;
    //餐饮资源组(房/票/餐，不可同时为空)
    private List<YcfBookFood> foodDetail;
    //出行客人集合(套餐预定规则要求，则必填)
    private List<YcfBookGuest> guests;
    //价格集合
    private List<YcfPriceItem> priceDetail;
    //订单备注
    private String remark;

    public String getPartnerOrderId() {
        return partnerOrderId;
    }

    public void setPartnerOrderId(String partnerOrderId) {
        this.partnerOrderId = partnerOrderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQunatity() {
        return qunatity;
    }

    public void setQunatity(int qunatity) {
        this.qunatity = qunatity;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getSellAmount() {
        return sellAmount;
    }

    public void setSellAmount(BigDecimal sellAmount) {
        this.sellAmount = sellAmount;
    }

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

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }

    public int getCredentialType() {
        return credentialType;
    }

    public void setCredentialType(int credentialType) {
        this.credentialType = credentialType;
    }

    public List<YcfBookRoom> getRoomDetail() {
        return roomDetail;
    }

    public void setRoomDetail(List<YcfBookRoom> roomDetail) {
        this.roomDetail = roomDetail;
    }

    public List<YcfBookTicket> getTicketDetail() {
        return ticketDetail;
    }

    public void setTicketDetail(List<YcfBookTicket> ticketDetail) {
        this.ticketDetail = ticketDetail;
    }

    public List<YcfBookFood> getFoodDetail() {
        return foodDetail;
    }

    public void setFoodDetail(List<YcfBookFood> foodDetail) {
        this.foodDetail = foodDetail;
    }

    public List<YcfBookGuest> getGuests() {
        return guests;
    }

    public void setGuests(List<YcfBookGuest> guests) {
        this.guests = guests;
    }

    public List<YcfPriceItem> getPriceDetail() {
        return priceDetail;
    }

    public void setPriceDetail(List<YcfPriceItem> priceDetail) {
        this.priceDetail = priceDetail;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
