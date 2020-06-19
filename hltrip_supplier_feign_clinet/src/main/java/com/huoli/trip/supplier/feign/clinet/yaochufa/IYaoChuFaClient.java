package com.huoli.trip.supplier.feign.clinet.yaochufa;

import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfCommonResult;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfOrderStatusResult;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfVochersResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseRequest;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfBookCheckReq;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfBookCheckRes;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfPayOrderReq;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfPayOrderRes;

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
    YcfCommonResult<YcfBookCheckRes> getCheckInfos(@RequestBody YcfBaseRequest<YcfBookCheckReq> req);
    /**
     * 支付订单
     * @param= req
     * @return= PayOrderRes
     * @author= wangdm
     * @document http://opensip.yaochufa.com/sip/api
     */
    @RequestMapping(method = RequestMethod.POST,path = "/OTA/payOrder")
    YcfCommonResult<YcfPayOrderRes> payOrder(@RequestBody YcfBaseRequest<YcfPayOrderReq> req);

    /**
     * 通过订单编号
     * 重发凭证
     * @param partnerOrderId
     * @return
     */
    @RequestMapping(method = RequestMethod.POST,path = "/OTA/resendVoucher")
    YcfCommonResult<YcfVochersResult> getVochers(String partnerOrderId);

    /**
     * 通过订单号获取订单状态等信息
     * @param partnerOrderId
     * @return
     */
    @RequestMapping(method = RequestMethod.POST,path = "/OTA/getOrderStatus")
    YcfCommonResult<YcfOrderStatusResult> getOederStatus(String partnerOrderId);

}
