package com.huoli.trip.supplier.self.difengyun.vo;

import lombok.Data;

import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/12/8<br>
 */
@Data
public class DfyTicketDetail {

    /**
     * 门票产品ID
     */
    private String productId;

    /**
     * 资源ID
     */
    private String resourceId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 景点id
     */
    private String scenicId;

    /**
     * 单位元,景点价（当前景点的价格，信息可能有滞后，仅供参考）
     */
    private String webPrice;

    /**
     * 单位元,分销起价（所有团期中最低的价格，仅供参考）
     */
    private String salePrice;

    /**
     * 取票方式：1实体票 8预付电子票
     */
    private Integer drawType;

    /**
     * 取票地点,必须展示
     */
    private String drawAddress;

    /**
     * 1门票2 联票3套票4专项
     */
    private Integer subType;

    /**
     * 有效期.如: "指定日期有效（2月2日或者3日）",必须展示
     */
    private String indate;

    /**
     * 预订截止天数,必须展示
     */
    private Integer advanceDay;

    /**
     * 预订截止小时数（表示几点钟）,必须展示
     */
    private Integer advanceHour;

    /**
     * 预订须知,必须展示
     */
    private String bookNotice;

    /**
     * 起订张数
     */
    private Integer limitNumLow;

    /**
     * 最多预订张数
     */
    private Integer limitNumHigh;

    /**
     * 预订项要求
     * 1取票人姓名+手机号；
     * 2取票人姓名+手机号，所有游客姓名+手机号；
     * 3取票人姓名+手机号，所有游客姓名+手机号+证件号；
     * 4取票人姓名+手机号+证件号；
     * 6取票人姓名+手机号+证件号，所有游客姓名+手机号+证件号；
     * 7取票人姓名+手机号+证件号，所有游客姓名+手机号；
     */
    private Integer custInfoLimit;

    /**
     * 证件类型, (逗号分隔) 如”1,2,3,4”
     * 1身份证
     * 2护照
     * 3军官证
     * 4港澳通行证
     * 7台胞证
     * 为空表示只支持1身份证，或者不需要录入证件
     */
    private String certificateType;

    /**
     * 入园方式,必须展示
     */
    private DfyAdmissionVoucher admissionVoucher;

    /**
     * 价格日历。下单价格
     */
    private List<DfyPriceCalendar> priceCalendar;

    /**
     * 其他说明,必须展示
     */
    private String info;

    /**
     * 退改规则
     */
    private String mpLossInfo;

    /**
     * 产品归属: 2:主推
     */
    private String systemFlag;

    /**
     * 门票分类:0特惠;1成人票;2亲子家庭票;3学生票;4儿童票;5老人票
     * 6特殊人群优待票;7套票;8联票;9园内餐饮;10园内交通;11园内其他票
     * 12园内演出
     */
    private String mpType;
}
