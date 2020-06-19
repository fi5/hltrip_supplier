package com.huoli.trip.supplier.self.yaochufa.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class YcfCommonResult<T> implements Serializable {
    /**
     * 是否处理成功
     * boolean
     * 必填
     * true:处理成功
     * false:处理失败
     */
    private String success;

    /**
     * 请求处理状态
     * int
     * 必填
     * 200: 成功
     * 400: 请求错误
     * 500:服务错误
     */
    private String statusCode;

    /**
     * 错误信息
     * String
     * 非必填
     * success=false的错误原因
     */
    private String message;

    /**
     * 合作人Id（由【要】分配）
     * 必填
     */
    private String partnerId;
    /**
     * 请求处理后的返回数据，具体对应每个接口返回的具体结构
     */
    private T data;
}
