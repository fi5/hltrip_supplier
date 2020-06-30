package com.huoli.trip.supplier.feign.client.yaochufa.client;

import com.huoli.trip.supplier.feign.client.yaochufa.Interceptor.YaoChuFaFeignInterceptor;
import com.huoli.trip.supplier.feign.client.yaochufa.client.impl.YaoChuFaClientFallback;
import com.huoli.trip.supplier.self.yaochufa.vo.*;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseRequest;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

/**
 * 描述：要出发客户端连接<br>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：顾刘川<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/18<br>
 */
@FeignClient(name = "yaoChuFa", url = "${yaochufa.host.server}"
        ,configuration = YaoChuFaFeignInterceptor.class
        ,fallbackFactory = YaoChuFaClientFallback.class)
public interface IYaoChuFaClient {

    /**
     * 获取poi信息
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST,path = "/OTA/GetProductPois")
    YcfBaseResult<YcfGetPoiResponse> getPoi(@RequestBody YcfBaseRequest<YcfGetPoiRequest> request);

    /**
     * 获取价格日历
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST,path = "/OTA/StockPriceRequest")
    YcfBaseResult<YcfGetPriceResponse> getPrice(@RequestBody YcfBaseRequest<YcfGetPriceRequest> request);

    @RequestMapping(method = RequestMethod.POST,path = "/OTA/getProductPois")
    String getWeather(@RequestBody Map req);

    /**
     * 可预订检查
     * @param= req
     * @return= BookCheckRes
     * @author= wangdm
     * @document http://opensip.yaochufa.com/sip/api
     */
    @RequestMapping(method = RequestMethod.POST,path = "/OTA/CheckAvail")
    YcfBaseResult<YcfBookCheckRes> getCheckInfos(@RequestBody YcfBaseRequest<YcfBookCheckReq> req);
    /**
     * 支付订单
     * @param= req
     * @return= PayOrderRes
     * @author= wangdm
     * @document http://opensip.yaochufa.com/sip/api
     */
    @RequestMapping(method = RequestMethod.POST,path = "/OTA/payOrder")
    YcfBaseResult<YcfPayOrderRes> payOrder(@RequestBody YcfBaseRequest<YcfPayOrderReq> req);

    /**
     * 通过订单编号
     * 重发凭证
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST,path = "/OTA/resendVoucher")
    YcfBaseResult<YcfVouchersResult> getVouchers(@RequestBody YcfBaseRequest<YcfOrderBaSeRequest> request);

    /**
     * 通过订单号获取订单状态等信息
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST,path = "/OTA/getOrderStatus")
    YcfBaseResult<YcfOrderStatusResult> getOrderStatus(@RequestBody YcfBaseRequest<YcfOrderBaSeRequest> request);

    /**
     * 创建订单
     * @param= req
     * @return= YcfCreateOrderRes
     * @author= wangdm
     * @document http://opensip.yaochufa.com/sip/api
     */
    @RequestMapping(method = RequestMethod.POST,path = "/OTA/createOrder")
    YcfBaseResult<YcfCreateOrderRes> createOrder(@RequestBody YcfBaseRequest<YcfCreateOrderReq> req);

    /**
     * 申请取消订单
     * @param= req
     * @return= YcfCancelOrderRes
     * @author= wangdm
     * @document http://opensip.yaochufa.com/sip/api
     */
    @RequestMapping(method = RequestMethod.POST,path = "/OTA/cancelOrder")
    YcfBaseResult<YcfCancelOrderRes> cancelOrder(YcfBaseRequest<YcfCancelOrderReq> req);
}
