package com.huoli.trip.supplier.feign.client.lvmama.client;

import com.huoli.trip.supplier.feign.client.lvmama.client.impl.LvmamaClientFallback;
import com.huoli.trip.supplier.self.lvmama.vo.request.*;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmBaseResponse;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmOrderDetailResponse;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmScenicResponse;
import com.huoli.trip.supplier.self.lvmama.vo.response.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 描述：要出发客户端连接<br>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：顾刘川<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/18<br>
 */
@FeignClient(name = "lvmam", url = "${lvmama.host.server}"
        /*,configuration = YaoChuFaFeignInterceptor.class*/
        ,fallbackFactory = LvmamaClientFallback.class)
public interface ILvmamaClient {

    /**
     * 订单详情
     */
    @RequestMapping(method = RequestMethod.POST,path = "/getOrderInfo")
    LmmOrderDetailResponse orderDetail(@RequestBody LmmOrderDetailRequest request);

    /*
     * 获取景区列表
     */
    @RequestMapping(method = RequestMethod.POST,path = "/scenicInfoListByPage")
    LmmScenicResponse getScenicList(@RequestBody LmmScenicRequest request);

    /**
     * 可预订检查
     */
    @RequestMapping(method = RequestMethod.POST,path = "/validateOrder")
    LmmBaseResponse getCheckInfos(@RequestBody ValidateOrderRequest request);
    /**
     * 支付订单
     */
    @RequestMapping(method = RequestMethod.POST,path = "/orderPayment")
    OrderResponse payOrder(@RequestBody OrderPaymentRequest req);

    /**
     * 创建订单
     */
    @RequestMapping(method = RequestMethod.POST,path = "/createOrder")
    OrderResponse createOrder(@RequestBody CreateOrderRequest req);

    /**
     * 申请取消订单
     */
    @RequestMapping(method = RequestMethod.POST,path = "/orderUnpaidCancel")
    OrderResponse cancelOrder(OrderUnpaidCancelRequest req);

    /**
     * 退票申请
     */
    @RequestMapping(method = RequestMethod.POST,path = "/orderCancel")
    LmmBaseResponse rufundTicket(OrderCancelRequest request);
}
