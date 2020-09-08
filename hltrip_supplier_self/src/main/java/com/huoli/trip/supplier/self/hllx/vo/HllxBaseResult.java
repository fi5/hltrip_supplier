package com.huoli.trip.supplier.self.hllx.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class HllxBaseResult <T> implements Serializable {
    private static final long serialVersionUID = 6491693684224522496L;
    /**
     * 是否处理成功
     * boolean
     * 必填
     * true:处理成功
     * false:处理失败
     */
    private Boolean success;

    /**
     * 请求处理状态
     * int
     * 必填
     * 200: 成功
     * 400: 请求错误
     * 500:服务错误
     */
    private int statusCode;

    /**
     * 错误信息
     * String
     * 非必填
     * success=false的错误原因
     */
    private String message;

    /**
     * 请求处理后的返回数据，具体对应每个接口返回的具体结构
     */
    private T data;

    public HllxBaseResult(Boolean success, int code, T data){
        this.setStatusCode(code);
        this.setSuccess(success);
        this.setData(data);
    }
}
