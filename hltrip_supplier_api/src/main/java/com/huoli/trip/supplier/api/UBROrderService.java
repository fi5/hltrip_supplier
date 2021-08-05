package com.huoli.trip.supplier.api;

import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.supplier.self.universal.vo.UBROrderDetailResponse;
import com.huoli.trip.supplier.self.universal.vo.reqeust.UBRTicketOrderLocalRequest;
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
     * 下单，本地单，保存供应商下单需要的参数
     * @param request
     * @return
     */
    BaseResponse createOrder(UBRTicketOrderLocalRequest request);

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

    /**
     * 支付订单，这个是供应商实际下单操作，供应商下单支付是一个接口
     * @param request
     * @return
     */
    UBRBaseResponse<UBRTicketOrderResponse> payOrder(BaseOrderRequest request);
}
