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
    // 下面这些暂时用不到
    /**
     * 回乡证
     */
    public static final int CRED_TYPE_HK_2 = 8;
    /**
     * 户口簿
     */
    public static final int CRED_TYPE_AC = 9;
    /**
     * 出生证明
     */
    public static final int CRED_TYPE_BC = 10;
    /**
     * 台湾通行证
     */
    public static final int CRED_TYPE_TW_2 = 11;


    // 库存类型
    /**
     * 库存
     */
    public static final int STOCK_TYPE_NOM = 1;
    /**
     * FS（无限量）
     */
    public static final int STOCK_TYPE_UNLIMITED = 2;
    /**
     * 现询（人工确认）
     */
    public static final int STOCK_TYPE_OFFLINE = 3;

    /*
     * 资源模块类型 1-景点，2-酒店 3-小交通，4-餐饮，5-购物，6-活动，7-提醒,9-游轮
     * 资源模块名 scenic-景点 hotel-酒店 traffic-小交通 food-餐饮 shopping-购物 activity-活动 reminder-提醒 ship-游轮
     */
    public static final int MODULE_TYPE_SCENIC = 1;

    public static final int MODULE_TYPE_HOTEL = 2;

    public static final int MODULE_TYPE_TRAFFIC = 3;

    public static final int MODULE_TYPE_FOOD = 4;

    public static final int MODULE_TYPE_SHOPPING = 5;

    public static final int MODULE_TYPE_ACTIVITY = 6;

    public static final int MODULE_TYPE_REMINDER = 7;

    public static final int MODULE_TYPE_SHIP = 9;


    /*
    交通方式1 飞机，2 火车卧铺，3 火车硬卧，4 火车软座，5 火车硬座，6 汽车，7 邮轮，
    8 火车，9 动车组，10 游船，11 高铁二等座，12 高铁一等座，13 高铁商务座，14 自行安排
     */
    public static final int TRAFFIC_TYPE_1 = 1;

    public static final int TRAFFIC_TYPE_2 = 2;

    public static final int TRAFFIC_TYPE_3 = 3;

    public static final int TRAFFIC_TYPE_4 = 4;

    public static final int TRAFFIC_TYPE_5 = 5;

    public static final int TRAFFIC_TYPE_6 = 6;

    public static final int TRAFFIC_TYPE_7 = 7;

    public static final int TRAFFIC_TYPE_8 = 8;

    public static final int TRAFFIC_TYPE_9 = 9;

    public static final int TRAFFIC_TYPE_10 = 10;

    public static final int TRAFFIC_TYPE_11 = 11;

    public static final int TRAFFIC_TYPE_12 = 12;

    public static final int TRAFFIC_TYPE_13 = 13;

    public static final int TRAFFIC_TYPE_14 = 14;

}
