package com.huoli.trip.supplier.feign.client.lvmama.client;

import com.huoli.trip.supplier.feign.client.lvmama.client.impl.LvmamaClientFallback;
import com.huoli.trip.supplier.feign.client.lvmama.interceptor.LvMaMaFeignInterceptor;
import com.huoli.trip.supplier.self.lvmama.vo.request.LmmProductListRequest;
import com.huoli.trip.supplier.self.lvmama.vo.request.*;
import com.huoli.trip.supplier.self.lvmama.vo.response.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
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
@FeignClient(name = "lvmama", url = "${lvmama.host.server}"
        ,configuration = LvMaMaFeignInterceptor.class
        ,fallbackFactory = LvmamaClientFallback.class)
public interface ILvmamaClient {

    /**
     * 批量获取景区
     */
    @RequestMapping(method = RequestMethod.GET,path = "/scenic/product/distributorApi/2.0/api/ticketProd/scenicInfoListByPage")
    LmmScenicListResponse getScenicList(@RequestParam("currentPage") int currentPage);

    /**
     * 按id获取景区
     */
    @RequestMapping(method = RequestMethod.GET,path = "/scenic/product/distributorApi/2.0/api/ticketProd/scenicInfoList")
    LmmScenicListResponse getScenicListById(@RequestParam("scenicId") String scenicId);

    /**
     * 批量获取产品
     */
    @RequestMapping(method = RequestMethod.GET,path = "/scenic/product/distributorApi/2.0/api/ticketProd/productInfoListByPage")
    LmmProductListResponse getProductList(@RequestParam("currentPage") int currentPage);

    /**
     * 根据id获取产品
     */
    @RequestMapping(method = RequestMethod.GET,path = "/scenic/product/distributorApi/2.0/api/ticketProd/productInfoList")
    LmmProductListResponse getProductListById(@RequestParam("productIds") String productIds);

    /**
     * 根据id获取商品
     */
    @RequestMapping(method = RequestMethod.GET,path = "/scenic/product/distributorApi/2.0/api/ticketProd/goodInfoList")
    LmmGoodsListByIdResponse getGoodsListById(@RequestParam("goodsIds") String goodsIds);

    /**
     * 获取价格
     */
    @RequestMapping(method = RequestMethod.GET,path = "/scenic/product/distributorApi/2.0/api/ticketProd/goodPriceList")
    LmmPriceResponse getPriceList(@RequestParam("goodsIds") String goodsIds, @RequestParam("beginDate") String beginDate, @RequestParam("endDate") String endDate);

    /**
     * 订单详情
     */
    @RequestMapping(method = RequestMethod.GET,path = "/scenic/order/distributorApi/2.0/api/order/getOrderInfo")
    LmmOrderDetailResponse orderDetail(@RequestParam("request") String request);

    /**
     * 可预订检查
     */
    @RequestMapping(method = RequestMethod.GET,path = "/scenic/order/distributorApi/2.0/api/ticket/validateOrder")
    LmmBaseResponse getCheckInfos(@RequestParam("request") String request);
    /**
     * 支付订单
     */
    @RequestMapping(method = RequestMethod.GET,path = "/scenic/order/distributorApi/2.0/api/ticket/orderPayment")
    OrderResponse payOrder(@RequestParam("request") String request);

    /**
     * 创建订单
     */
    @RequestMapping(method = RequestMethod.GET,path = "/scenic/order/distributorApi/2.0/api/ticket/createOrder")
    OrderResponse createOrder(@RequestParam("request") String request);

    /**
     * 申请取消订单
     */
    @RequestMapping(method = RequestMethod.GET,path = "/scenic/order/distributorApi/2.0/api/ticket/orderUnpaidCancel")
    OrderResponse cancelOrder(@RequestParam("partnerOrderNo") String partnerOrderNo, @RequestParam("orderId") String orderId);

    /**
     * 退票申请
     */
    @RequestMapping(method = RequestMethod.GET,path = "/scenic/order/distributorApi/2.0/api/ticket/orderCancel")
    LmmBaseResponse refundTicket(@RequestParam("partnerOrderNo") String partnerOrderNo, @RequestParam("orderId") String orderId);

    /**
     * 重发凭证
     */
    @RequestMapping(method = RequestMethod.GET,path = "/scenic/order/distributorApi/2.0/api/ticket/resendCode")
    LmmBaseResponse resendCode(@RequestParam("request") String request);

}
