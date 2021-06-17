package com.huoli.trip.supplier.feign.client.lvmama.client;

import com.huoli.trip.supplier.feign.client.lvmama.client.impl.LvmamaOrderClientFallback;
import com.huoli.trip.supplier.feign.client.lvmama.interceptor.LvMaMaFeignInterceptor;
import com.huoli.trip.supplier.self.lvmama.vo.response.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 描述：要出发客户端连接<br>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：顾刘川<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/18<br>
 */
@FeignClient(name = "lvmamaorder", url = "${lvmama.host.server.order}"
        ,configuration = LvMaMaFeignInterceptor.class
        ,fallbackFactory = LvmamaOrderClientFallback.class)
public interface ILvmamaOrderClient {
    /**
     * 订单详情
     */
    @RequestMapping(method = RequestMethod.GET,path = "/distributorApi/2.0/api/order/getOrderInfo")
    LmmOrderDetailResponse orderDetail(@RequestParam("request") String request);

    /**
     * 可预订检查
     */
    @RequestMapping(method = RequestMethod.GET,path = "/distributorApi/2.0/api/ticket/validateOrder")
    LmmBaseResponse getCheckInfos(@RequestParam("request") String request);
    /**
     * 支付订单
     */
    @RequestMapping(method = RequestMethod.GET,path = "/distributorApi/2.0/api/order/orderPayment")
    OrderResponse payOrder(@RequestParam("request") String request);

    /**
     * 创建订单
     */
    @RequestMapping(method = RequestMethod.GET,path = "/distributorApi/2.0/api/ticket/createOrder")
    OrderResponse createOrder(@RequestParam("request") String request);

    /**
     * 申请取消订单
     */
    @RequestMapping(method = RequestMethod.GET,path = "/distributorApi/2.0/api/ticket/orderUnpaidCancel")
    OrderResponse cancelOrder(@RequestParam("partnerOrderNo") String partnerOrderNo, @RequestParam("orderId") String orderId);

    /**
     * 退票申请
     */
    @RequestMapping(method = RequestMethod.GET,path = "/distributorApi/2.0/api/ticket/orderCancel")
    LmmBaseResponse refundTicket(@RequestParam("partnerOrderNo") String partnerOrderNo, @RequestParam("orderId") String orderId);

    /**
     * 重发凭证
     */
    @RequestMapping(method = RequestMethod.GET,path = "/distributorApi/2.0/api/order/resendCode")
    LmmBaseResponse resendCode(@RequestParam("request") String request);

}
