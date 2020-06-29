package com.huoli.trip.supplier.self.yaochufa.vo;

import lombok.Data;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/24<br>
 */
@Data
public class YcfPushProductResponse {

    /**
     * 推送数据处理状态
     * 0：待处理
     * 1：处理成功
     * 2：处理失败
     */
    private Integer handleStatus;

    /**
     * 产品状态
     * 0：下线
     * 1：上线
     */
    private Integer productStatus;

    /**
     * 异步处理状态
     * 0：同步即时处理
     * 1：异步处理
     * 为0时handleStatus和productStatus为必填项
     */
    private Integer async;

    public YcfPushProductResponse(){

    }

    public YcfPushProductResponse(int async){
        this.async = async;
    }

    public YcfPushProductResponse(int handleStatus, int productStatus, int async){
        this.async = async;
    }
}
