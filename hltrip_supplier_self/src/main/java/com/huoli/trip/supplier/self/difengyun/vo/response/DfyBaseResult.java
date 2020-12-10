package com.huoli.trip.supplier.self.difengyun.vo.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/12/8<br>
 */
@Data
public class DfyBaseResult<T> implements Serializable {
    private static final long serialVersionUID = 3957777073403240039L;
    private String errorCode;
    private String msg;
    private boolean success;

    private T data;

    public DfyBaseResult() {
    }

    public DfyBaseResult(String msg, boolean success) {
        this.msg = msg;
        this.success = success;
    }

    public DfyBaseResult(String errorCode, String msg, boolean success) {
        this.errorCode = errorCode;
        this.msg = msg;
        this.success = success;
    }
}
