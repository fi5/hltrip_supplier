package com.huoli.trip.supplier.self.yaochufa.vo;

import com.huoli.trip.common.vo.request.TraceRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * 描述: <br>申请取消订单
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：王德铭<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/19<br>
 */
@Data
public class YcfCancelOrderReq extends TraceRequest implements Serializable {

    //【合】订单号
    private String partnerOrderId;
    // 取消原因备注
    private String remark;
}
