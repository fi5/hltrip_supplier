package com.huoli.trip.supplier.feign.clinet.yaochufa;

import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfCommonResult;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfOrderStatusResult;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfVochersResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseRequest;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfBookCheckReq;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfBookCheckRes;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfPayOrderReq;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfPayOrderRes;

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
    public YcfCommonResult<YcfBookCheckRes> getCheckInfos(YcfBaseRequest<YcfBookCheckReq> req) {
        log.info("***********进入getCheckInfos降级处理逻辑***************");
        return null;
    }

    @Override
    public YcfCommonResult<YcfPayOrderRes> payOrder(YcfBaseRequest<YcfPayOrderReq> req) {
        log.info("***********进入payOrder降级处理逻辑***************");
        return null;
    }

    @Override
    public YcfCommonResult<YcfVochersResult> getVochers(String partnerOrderId) {
        return null;
    }

    @Override
    public YcfCommonResult<YcfOrderStatusResult> getOederStatus(String partnerOrderId) {
        return null;
    }
}
