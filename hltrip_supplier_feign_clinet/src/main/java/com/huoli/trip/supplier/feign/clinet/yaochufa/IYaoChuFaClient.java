package com.huoli.trip.supplier.feign.clinet.yaochufa;

import YaoChuFaVo.CommonResult;
import YaoChuFaVo.OrderStatusResult;
import YaoChuFaVo.VochersResult;
import com.huoli.trip.common.vo.basevo.BaseRequest;
import com.huoli.trip.common.vo.basevo.BaseResponse;
import com.huoli.trip.common.vo.order.BookCheckReq;
import com.huoli.trip.common.vo.order.BookCheckRes;
import com.huoli.trip.common.vo.order.PayOrderReq;
import com.huoli.trip.common.vo.order.PayOrderRes;
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
        ,fallback = YaoChufaClientFallback.class)
public interface IYaoChuFaClient {

    @RequestMapping(method = RequestMethod.POST,path = "/OTA/CheckAvail")
    String getWeather(@RequestBody Map req);

    /**
     * 可预订检查
     * @param= req
     * @return= BookCheckRes
     * @author= wangdm
     * @document http://opensip.yaochufa.com/sip/api
     */
    @RequestMapping(method = RequestMethod.POST,path = "/OTA/CheckAvail")
    BaseResponse<BookCheckRes> getCheckInfos(@RequestBody BaseRequest<BookCheckReq> req);
    /**
     * 支付订单
     * @param= req
     * @return= PayOrderRes
     * @author= wangdm
     * @document http://opensip.yaochufa.com/sip/api
     */
    @RequestMapping(method = RequestMethod.POST,path = "/OTA/payOrder")
    BaseResponse<PayOrderRes> payOrder(@RequestBody BaseRequest<PayOrderReq> req);

    /**
     * 通过订单编号
     * 重发凭证
     * @param partnerOrderId
     * @return
     */
    @RequestMapping(method = RequestMethod.POST,path = "/OTA/resendVoucher")
    CommonResult<VochersResult> getVochers(String partnerOrderId);

    /**
     * 通过订单号获取订单状态等信息
     * @param partnerOrderId
     * @return
     */
    @RequestMapping(method = RequestMethod.POST,path = "/OTA/getOrderStatus")
    CommonResult<OrderStatusResult> getOederStatus(String partnerOrderId);

}
