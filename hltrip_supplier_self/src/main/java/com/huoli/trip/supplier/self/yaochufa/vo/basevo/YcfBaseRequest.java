package com.huoli.trip.supplier.self.yaochufa.vo.basevo;

import lombok.Data;

import java.io.Serializable;

/**
 * 描述: <br> 业务请求实体基类
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：王德铭<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/18<br>
 */
@Data
public class YcfBaseRequest<T> implements Serializable {
    /**
     * 请求体
     */
    private T data;

    public YcfBaseRequest(){

    }

    public YcfBaseRequest(T data){
        this.data = data;
    }
}
