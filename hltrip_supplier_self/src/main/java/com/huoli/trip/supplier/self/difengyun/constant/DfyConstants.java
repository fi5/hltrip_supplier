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
     * 特惠
     */
    public static final int TICKET_TYPE_0 = 0;
    /**
     * 成人票
     */
    public static final int TICKET_TYPE_1 = 1;
    /**
     * 亲子家庭票
     */
    public static final int TICKET_TYPE_2 = 2;
    /**
     * 学生票
     */
    public static final int TICKET_TYPE_3 = 3;
    /**
     * 儿童票
     */
    public static final int TICKET_TYPE_4 = 4;
    /**
     * 老人票
     */
    public static final int TICKET_TYPE_5 = 5;
    /**
     * 特殊人群优待票
     */
    public static final int TICKET_TYPE_6 = 6;
    /**
     * 套票
     */
    public static final int TICKET_TYPE_7 = 7;
    /**
     * 联票
     */
    public static final int TICKET_TYPE_8 = 8;
    /**
     * 园内餐饮
     */
    public static final int TICKET_TYPE_9 = 9;
    /**
     * 园内交通
     */
    public static final int TICKET_TYPE_10 = 10;
    /**
     * 园内其他票
     */
    public static final int TICKET_TYPE_11 = 11;
    /**
     * 园内演出
     */
    public static final int TICKET_TYPE_12 = 12;

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
