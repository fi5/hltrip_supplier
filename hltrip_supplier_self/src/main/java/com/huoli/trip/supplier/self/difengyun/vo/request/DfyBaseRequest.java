package com.huoli.trip.supplier.self.difengyun.vo.request;

import lombok.Data;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/12/8<br>
 */
@Data
public class DfyBaseRequest<T> {

    /**
     * 接口标识
     */
    private String apiKey;

    /**
     * 秘钥
     */
    private String secretKey;

    /**
     * 签名
     */
    private String sign;

    /**
     * 时间戳
     */
    private String timestamp;

    private T data;

    public DfyBaseRequest(){
    }

    public DfyBaseRequest(T data){
        this.data = data;
    }
}
