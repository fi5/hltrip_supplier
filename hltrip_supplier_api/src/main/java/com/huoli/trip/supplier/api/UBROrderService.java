package com.huoli.trip.supplier.api;

import com.huoli.trip.supplier.self.universal.vo.UBROrderDetailResponse;
import com.huoli.trip.supplier.self.universal.vo.reqeust.UBRTicketOrderRequest;
import com.huoli.trip.supplier.self.universal.vo.response.UBRBaseResponse;
import com.huoli.trip.supplier.self.universal.vo.response.UBRTicketOrderResponse;
import com.huoli.trip.supplier.self.yaochufa.vo.BaseOrderRequest;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/8/3<br>
 */
public interface UBROrderService {

    /**
     * 下单
     * @param request
     * @return
     */
    UBRBaseResponse<UBRTicketOrderResponse> createOrder(UBRTicketOrderRequest request);

    /**
     * 退款检查
     * @param request
     * @return
     */
    UBRBaseResponse refundCheck(BaseOrderRequest request);

    /**
     * 退款
     * @param request
     * @return
     */
    UBRBaseResponse<UBRTicketOrderResponse> refund(BaseOrderRequest request);

    /**
     * 订单详情
     * @param request
     * @return
     */
    UBRBaseResponse<UBROrderDetailResponse> orderDetail(BaseOrderRequest request);
}
