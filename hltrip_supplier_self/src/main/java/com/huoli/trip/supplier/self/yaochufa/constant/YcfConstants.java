package com.huoli.trip.supplier.self.yaochufa.constant;

import com.google.common.collect.ImmutableList;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/19<br>
 */
public class YcfConstants {

    // 返回状态

    public static final int RESULT_CODE_SUCCESS = 200;

    public static final int RESULT_CODE_REQUEST_ERROR = 400;

    public static final int RESULT_CODE_SERVICE_ERROR = 500;

    // 成功状态

    public static final Boolean RESULT_STATUS_SUCCESS = true;

    public static final Boolean RESULT_STATUS_FAIL = false;

    // 产品处理状态
    /**
     * 待处理
     */
    public static final int PRODUCT_HANDLE_STATUS_WAIT = 0;

    /**
     * 处理成功
     */
    public static final int PRODUCT_HANDLE_STATUS_SUCCESS = 1;

    /**
     * 处理失败
     */
    public static final int PRODUCT_HANDLE_STATUS_FAIL = 2;

    // 产品状态
    /**
     * 上线
     */
    public static final int PRODUCT_STATUS_VALID = 1;
    /**
     * 下线
     */
    public static final int PRODUCT_STATUS_INVALID = 0;

    // 处理类型
    /**
     * 同步
     */
    public static final int HANDLE_TYPE_ASYNC = 0;
    /**
     * 异步
     */
    public static final int HANDLE_TYPE_SYNC = 1;

    // 产品类型
    /**
     * 要出发类型集合
     */
    public static final ImmutableList<Integer> PRODUCT_TYPE_LIST = ImmutableList.of(0, 1, 2, 3);
    /**
     * 套餐
     */
    public static final int PRODUCT_TYPE_PACKAGE = 0;
    /**
     * 单房
     */
    public static final int PRODUCT_TYPE_ROOM = 1;
    /**
     * 单票
     */
    public static final int PRODUCT_TYPE_TICKET = 2;
    /**
     * 单餐
     */
    public static final int PRODUCT_TYPE_FOOD = 3;


}
