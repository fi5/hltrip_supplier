package com.huoli.trip.supplier.self.universal.constant;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/7/27<br>
 */
public class UBRConstants {

    /**
     * 环球鉴权token key
     */
    public static String AUTH_KEY = "BTG_AUTH_TOKEN_KEY";

    // 配置文件

    public static final String CONFIG_FILE_UBR = "supplier_btg.properties";

    // 配置项
    /**
     * 服务器地址
     */
    public static final String CONFIG_ITEM_HOST_SERVER = "btg.host.server";
    /**
     * 环球账号
     */
    public static String CONFIG_ITEM_ACCOUNT = "btg.account";

    /**
     * 环球密码
     */
    public static String CONFIG_ITEM_PASSWORD = "btg.password";

    /**
     * 景点id
     */
    public static String CONFIG_ITEM_SCENIC_ID = "btg.scenic.id";

    // 票种
    /**
     * 成人
     */
    public static String PERSON_TYPE_ADT = "Adult";

    /**
     * 儿童
     */
    public static String PERSON_TYPE_CHD = "Child";

    /**
     * 老人
     */
    public static String PERSON_TYPE_OLD = "Senior";

    /**
     * 通用
     */
    public static String PERSON_TYPE_GST = "Guest";

    // 订单状态
    /**
     * 处理中
     */
    public static String ORDER_STATUS_PROCESS = "PROCESS";
    /**
     * 正常
     */
    public static String ORDER_STATUS_NORMAL = "NORMAL";
    /**
     * 已退款
     */
    public static String ORDER_STATUS_REFUND = "REFUND";
    /**
     * 退款处理中
     */
    public static String ORDER_STATUS_REFUND_PROCESS = "REFUND_PROCESS";
    /**
     * 取消
     */
    public static String ORDER_STATUS_CANCEL = "CANCEL";
    /**
     * 预定失败
     */
    public static String ORDER_STATUS_BUY_FILED = "BUY_FILED";
    /**
     * 已完成
     */
    public static String ORDER_STATUS_COMPLETED = "COMPLETED";
}
