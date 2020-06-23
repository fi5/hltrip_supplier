package com.huoli.trip.supplier.self.yaochufa.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 描述：产品<br>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：顾刘川<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/17<br>
 */
@Data
public class YcfProduct {
    /**
     * 产品id
     */
    private String productID;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 产品状态
     * 0：下线
     * 1：上线
     */
    private Integer productStatus;

    /**
     * 产品类型
     * 0：套餐
     * 1：单房
     * 2：单票
     * 3：单餐
     */
    @JsonProperty("ProductType")
    private Integer productType;

    /**
     * 产品图片列表
     */
    private List<YcfImageBase> productImageList;

    /**
     * 酒景编号
     */
    private String poiId;

    /**
     * 提前多少分钟预订
     * 如必须提前一天的下午4点前购买本产品，则为1*24*60+16*60+0=2400，如不限制为0
     */
    private Integer bookAheadMin;

    /**
     * 产品最低购买份数
     * 0为不限制
     * 1为最少购买一份
     */
    private Integer minNum;

    /**
     * 产品最多购买份数
     * 0为不限制
     * 1为最多购买一份
     */
    private Integer maxNum;

    /**
     * 产品最低购买晚数
     * 0为不限制
     * 1为最少购买一晚
     */
    private Integer minNight;

    /**
     * 产品最多购买晚数
     * 0为不限制
     * 1为最多购买一晚
     */
    private Integer maxNight;

    /**
     * 产品市场价
     */
    private BigDecimal marketPrice;

    /**
     * 上架时间
     */
    private Date startDate;

    /**
     * 下架时间
     */
    private Date endDate;

    /**
     * 产品描述 （可以是富文本）
     */
    private String productDescription;

    /**
     * 费用包含说明
     */
    @JsonProperty("feeInlude")
    private String feeInclude;

    /**
     * 费用除外说明
     */
    private String feeExclude;

    /**
     * 退改类型 1 可退 2 不可退 3 条件
     */
    private Integer refundType;

    /**
     * 使用日期前或使用日期后  0：使用日期前 1：使用日期后
     */
    private Integer advanceOrDelayType;

    /**
     * 提前退票时间 分钟
     */
    @JsonProperty("refundPreminute")
    private Integer refundPreMinute;

    /**
     * 退改说明
     */
    private String refundNote;

    /**
     * 房资源组
     */
    private List<YcfResourceRoom> roomList;

    /**
     * 房型必选择数
     * 不支持M选N套餐，忽略该字段；房资源组内有多个房型时，最小的选择数量
     */
    @JsonProperty("roomchoicenum")
    private Integer roomChoiceNum;

    /**
     * 房型可选择数
     * 不支持M选N套餐，忽略该字段；房资源组内有多个房型时，可供选择的数量，如二选一的房，RoomChoiceNum为1，RoomOptionNum为2
     */
    private Integer roomOptionNum;

    /**
     * 门票资源组
     */
    private List<YcfResourceTicket> ticketList;

    /**
     * 门票必选择数
     * 不支持M选N套餐，忽略该字段；门票资源组内有多个票种时，最小的选择数量
     */
    private Integer ticketChoiceNum;

    /**
     * 门票可选择数
     * 不支持M选N套餐，忽略该字段；门票资源组内有多个票种时，可供选择的数量，如二选一的票，TicketChoiceNum为1，TicketOptionNum为2
     */
    private Integer ticketOptionNum;

    /**
     * 门票类型
     * 1： 普通票
     * 2： 成人票
     * 3： 亲子票
     * 4： 家庭票
     * 5： 情侣票
     * 6： 双人票
     * 7： 儿童票
     * 8： 老人票
     * 9： 学生票
     * 10： 军人票
     * 11： 教师票
     * 12： 残疾票
     * 13： 团体票
     * 14： 特殊票
     * 15： 优惠套票
     */
    private Integer ticketType;

    /**
     * 取票方式
     */
    private String getTicketMode;

    /**
     * 餐饮资源组
     */
    private List<YcfResourceFood> foodList;

    /**
     * 餐饮必选择数
     * 不支持M选N套餐，忽略该字段；餐饮资源组内有多个餐饮券时，最小的选择数量
     */
    private Integer foodChoiceNum;

    /**
     * 餐饮可选择数
     * 不支持M选N套餐，忽略该字段；餐饮券资源组内有多个餐饮券时，可供选择的数量，如二选一的券，FoodChoiceNum为1，FoddOptionNum为2
     */
    private Integer foodOptionNum;

    /**
     * 预订规则列表
     */
    private List<YcfProductBookRule> bookRules;

    /**
     * 是否支持全网预售
     */
    private Boolean isGlobalSale;

    /**
     * 展示开始时间
     */
    private Date globalSaleDisplayDateBegin;

    /**
     * 展示结束时间
     */
    private Date globalSaleDisplayDateEnd;

    /**
     * 预售开始时间
     */
    private Date preSaleDateBegin;

    /**
     * 预售结束时间
     */
    private Date preSaleDateEnd;

    /**
     * 预售使用开始时间
     */
    private Date orderDateBegin;

    /**
     * 预售使用结束时间
     */
    private Date orderDateEnd;

    /**
     * 预售说明
     */
    private String preSaleDescription;

    /**
     * 限制规则
     */
    private List<YcfProductLimitRule> limitBuyRules;

}
