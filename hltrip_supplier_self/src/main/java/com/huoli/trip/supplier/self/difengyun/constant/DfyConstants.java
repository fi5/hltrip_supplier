package com.huoli.trip.supplier.self.difengyun.constant;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/19<br>
 */
public class DfyConstants {
    // 通知类型
    /**
     * 更新
     */
    public static final int NOTICE_TYPE_UPDATE = 1;
    /**
     * 下线
     */
    public static final int NOTICE_TYPE_INVALID = 2;
    /**
     * 上线
     */
    public static final int NOTICE_TYPE_VALID= 3;

    // 一级品类
    /**
     * 跟团
     */
    public static final int BRAND_GROUP = 1;
    /**
     * 自助
     */
    public static final int BRAND_SELF = 2;

    // 取票方式
    /**
     * 实体票
     */
    public static final int DRAW_TYPE_PAPER = 1;
    /**
     * 电子票
     */
    public static final int DRAW_TYPE_ELECT = 8;

    // 门票类型
    /**
     * 普通
     */
    public static final int TICKET_TYPE_NORMAL = 1;
    /**
     * 联票
     */
    public static final int TICKET_TYPE_COUPON = 2;
    /**
     * 套票
     */
    public static final int TICKET_TYPE_PACKAGE = 3;
    /**
     * 专票
     */
    public static final int TICKET_TYPE_SPECIAL = 4;

    // 预定项要求
    /**
     * 取票人姓名+手机号；
     */
    public static final int BOOK_RULE_1 = 1;
    /**
     * 取票人姓名+手机号；所有游客姓名+手机号；
     */
    public static final int BOOK_RULE_2 = 2;
    /**
     * 取票人姓名+手机号；所有游客姓名+手机号+证件号；
     */
    public static final int BOOK_RULE_3 = 3;
    /**
     * 取票人姓名+手机号+证件号；
     */
    public static final int BOOK_RULE_4 = 4;
    /**
     * 取票人姓名+手机号+证件号；所有游客姓名+手机号+证件号；
     */
    public static final int BOOK_RULE_6 = 6;
    /**
     * 取票人姓名+手机号+证件号；所有游客姓名+手机号；
     */
    public static final int BOOK_RULE_7 = 7;

    // 证件类型
    /**
     * 身份证
     */
    public static final int CRED_TYPE_ID = 1;
    /**
     * 护照
     */
    public static final int CRED_TYPE_PP = 2;
    /**
     * 军官证
     */
    public static final int CRED_TYPE_OF = 3;
    /**
     * 港澳通行证；
     */
    public static final int CRED_TYPE_HK = 4;
    /**
     * 台胞证
     */
    public static final int CRED_TYPE_TW = 7;

}
