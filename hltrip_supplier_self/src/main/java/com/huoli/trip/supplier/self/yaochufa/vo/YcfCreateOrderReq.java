package com.huoli.trip.supplier.self.yaochufa.vo;

import lombok.Data;

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
@Data
public class YcfCreateOrderReq {
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
    private List<YcfRoom> roomDetail;
    //门票资源组(房/票/餐，不可同时为空)
    private List<YcfTicket> ticketDetail;
    //餐饮资源组(房/票/餐，不可同时为空)
    private List<YcfFood> foodDetail;
    //出行客人集合(套餐预定规则要求，则必填)
    private List<YcfGuest> guests;
    //价格集合
    private List<YcfPriceItem> priceDetail;
    //订单备注
    private String remark;

}
