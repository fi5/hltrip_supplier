package com.huoli.trip.supplier.feign.client.difengyun.client;

import com.huoli.trip.supplier.feign.client.difengyun.client.impl.DiFengYunClientFallback;
import com.huoli.trip.supplier.feign.client.difengyun.interceptor.DiFengYunFeignInterceptor;
import com.huoli.trip.supplier.self.difengyun.DfyOrderDetail;
import com.huoli.trip.supplier.self.difengyun.vo.DfyScenicDetail;
import com.huoli.trip.supplier.self.difengyun.vo.DfyTicketDetail;
import com.huoli.trip.supplier.self.difengyun.vo.request.*;
import com.huoli.trip.supplier.self.difengyun.vo.response.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/12/8<br>
 */
@FeignClient(name = "diFengYun", url = "${difengyun.host.server}"
        , configuration = DiFengYunFeignInterceptor.class
        , fallbackFactory = DiFengYunClientFallback.class)
public interface IDiFengYunClient {

    /**
     * 获取景点列表
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST,path = "/Ticket/scenicList")
    DfyBaseResult<DfyScenicListResponse> getScenicList(@RequestBody DfyBaseRequest<DfyScenicListRequest> request);

    /**
     * 获取景点详情
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST,path = "/Ticket/scenicDetail")
    DfyBaseResult<DfyScenicDetail> getScenicDetail(@RequestBody DfyBaseRequest<DfyScenicDetailRequest> request);

    /**
     * 获取门票详情
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST,path = "/Ticket/detail")
    DfyBaseResult<DfyTicketDetail> getTicketDetail(@RequestBody DfyBaseRequest<DfyTicketDetailRequest> request);

    /**
     * 获取订单详情
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST,path = "/Ticket/orderDetail")
    DfyBaseResult<DfyOrderDetail> orderDetail(@RequestBody DfyBaseRequest<DfyOrderDetailRequest> request);

    /**
     * 创建订单
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST,path = "/Public/bill/query")
    DfyBaseResult<DfyBillResponse> queryBill(@RequestBody DfyBaseRequest<DfyBillQueryDataReq> request);

    /**
     * 创建订单
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST,path = "/Ticket/createOrderNew")
    DfyBaseResult<DfyCreateOrderResponse> createOrder(@RequestBody DfyBaseRequest<DfyCreateOrderRequest> request);

    /**
     * 取消订单申请
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST,path = "/Ticket/cancelOrder")
    DfyBaseResult cancelOrder(@RequestBody DfyBaseRequest<DfyCreateOrderRequest> request);


    /**
     * 获取订单状态
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST,path = "/Ticket/orderStatus")
    DfyBaseResult<DfyOrderStatusResponse> orderStatus(@RequestBody DfyBaseRequest<DfyOrderStatusRequest> request);

    /**
     * 申请退票
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST,path = "/Ticket/tuiPiao")
    DfyBaseResult<DfyRefundTicketResponse> refundTicket(@RequestBody DfyBaseRequest<DfyRefundTicketRequest> request);

    /**
     * 出票(代扣)接口
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST,path = "/Ticket/submitOrder")
    DfyBaseResult<DfySubmitOrderResponse> submitOrder(@RequestBody DfyBaseRequest<DfySubmitOrderRequest> request);


}
