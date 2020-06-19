package com.huoli.trip.supplier.feign.clinet.yaochufa;

import com.huoli.trip.supplier.self.yaochufa.vo.CommonResult;
import com.huoli.trip.supplier.self.yaochufa.vo.OrderStatusResult;
import com.huoli.trip.supplier.self.yaochufa.vo.VochersResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.huoli.trip.supplier.self.yaochufa.vo.vo.basevo.BaseRequest;
import com.huoli.trip.supplier.self.yaochufa.vo.vo.basevo.BaseResponse;
import com.huoli.trip.supplier.self.yaochufa.vo.vo.order.BookCheckReq;
import com.huoli.trip.supplier.self.yaochufa.vo.vo.order.BookCheckRes;
import com.huoli.trip.supplier.self.yaochufa.vo.vo.order.PayOrderReq;
import com.huoli.trip.supplier.self.yaochufa.vo.vo.order.PayOrderRes;

import java.util.Map;

/**
 * 描述: <br> YaoChuFaClient服务降级
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：王德铭<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/19<br>
 */
@Component
@Slf4j
public class YaoChufaClientFallback implements IYaoChuFaClient {
    @Override
    public String getWeather(Map req) {
        return null;
    }

    @Override
    public BaseResponse<BookCheckRes> getCheckInfos(BaseRequest<BookCheckReq> req) {
        log.info("***********进入getCheckInfos降级处理逻辑***************");
        return null;
    }

    @Override
    public BaseResponse<PayOrderRes> payOrder(BaseRequest<PayOrderReq> req) {
        log.info("***********进入payOrder降级处理逻辑***************");
        return null;
    }

    @Override
    public CommonResult<VochersResult> getVochers(String partnerOrderId) {
        return null;
    }

    @Override
    public CommonResult<OrderStatusResult> getOederStatus(String partnerOrderId) {
        return null;
    }
}
