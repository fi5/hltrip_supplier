package com.huoli.trip.supplier.feign.client.universal.client;

import com.huoli.trip.supplier.feign.client.universal.client.impl.UBRClientFallback;
import com.huoli.trip.supplier.feign.client.universal.interceptor.UBRFeignInterceptor;
import com.huoli.trip.supplier.self.universal.vo.UBROrderDetailResponse;
import com.huoli.trip.supplier.self.universal.vo.UBRTicketList;
import com.huoli.trip.supplier.self.universal.vo.reqeust.UBRLoginRequest;
import com.huoli.trip.supplier.self.universal.vo.reqeust.UBRTicketListRequest;
import com.huoli.trip.supplier.self.universal.vo.reqeust.UBRTicketOrderRequest;
import com.huoli.trip.supplier.self.universal.vo.response.UBRBaseResponse;
import com.huoli.trip.supplier.self.universal.vo.response.UBRLoginResponse;
import com.huoli.trip.supplier.self.universal.vo.response.UBRRefundCheckResponse;
import com.huoli.trip.supplier.self.universal.vo.response.UBRTicketOrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/12/8<br>
 */
@FeignClient(name = "ubr", url = "${btg.host.server}"
        , configuration = UBRFeignInterceptor.class
        , fallbackFactory = UBRClientFallback.class)
public interface IUBRClient {

    /**
     * 登录
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, path = "/api/v1/user/login")
    UBRBaseResponse<UBRLoginResponse> login(@RequestBody UBRLoginRequest request);

    /**
     * 刷新token
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, path = "/api/v1/user/refresh")
    UBRBaseResponse<UBRLoginResponse> refreshToken();

    /**
     * 获取门票列表
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, path = "/api/v1/ticket")
    UBRBaseResponse<UBRTicketList> getTicketList(@RequestParam("type") String type);

    /**
     * 初始化数据
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, path = "/api/v1/init")
    UBRBaseResponse init();

    /**
     * 初始化数据
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, path = "/api/v1/virtual-stock/ticket")
    UBRBaseResponse getStock(@RequestParam("startAt") String startAt,
                             @RequestParam("endAt") String endAt,
                             @RequestParam("category") String category);


    // ------------- 订单 ------------------


    /**
     * 下单
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, path = "/api/v1/order")
    UBRBaseResponse<UBRTicketOrderResponse> order(@RequestBody UBRTicketOrderRequest request);

    /**
     * 退款预检查
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, path = "/api/v1/policy/refund/{uid}")
    UBRBaseResponse<UBRRefundCheckResponse> refundCheck(@PathVariable("uid") String orderId);

    /**
     * 订单退款
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, path = "/api/v1/refund/ticket/order/{uid}")
    UBRBaseResponse refund(@PathVariable("uid") String orderId);

    /**
     * 订单详情
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, path = "/api/v1/order/query/{order_uid}")
    UBRBaseResponse<UBROrderDetailResponse> orderDetail(@PathVariable("order_uid") String orderId);
}
