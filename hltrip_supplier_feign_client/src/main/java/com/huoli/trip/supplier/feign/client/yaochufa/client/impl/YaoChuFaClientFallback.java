package com.huoli.trip.supplier.feign.client.yaochufa.client.impl;

import com.huoli.trip.supplier.feign.client.yaochufa.client.IYaoChuFaClient;
import com.huoli.trip.supplier.self.yaochufa.vo.*;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseRequest;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseResult;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
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
public class YaoChuFaClientFallback implements FallbackFactory<IYaoChuFaClient> {


    @Override
    public IYaoChuFaClient create(Throwable throwable) {
        String msg = throwable == null ? "" : throwable.getMessage();
        if (!StringUtils.isEmpty(msg)) {
            log.error(msg);
        }
        return new IYaoChuFaClient() {

            @Override
            public YcfBaseResult<List<YcfProductItem>> getPoi(YcfBaseRequest<YcfGetPoiRequest> request){
                return null;
            }

            @Override
            public String getWeather(Map req) {
                return null;
            }

            @Override
            public YcfBaseResult<YcfBookCheckRes> getCheckInfos(YcfBaseRequest<YcfBookCheckReq> req) {
                return null;
            }

            @Override
            public YcfBaseResult<YcfPayOrderRes> payOrder(YcfBaseRequest<YcfPayOrderReq> req) {
                return null;
            }

            @Override
            public YcfBaseResult<YcfVouchersResult> getVouchers(YcfBaseRequest<YcfOrderBaSeRequest> request) {
                return null;
            }

            @Override
            public YcfBaseResult<YcfOrderStatusResult> getOrderStatus(YcfBaseRequest<YcfOrderBaSeRequest> request) {
                return null;
            }

            @Override
            public YcfBaseResult<YcfCreateOrderRes> createOrder(YcfBaseRequest<YcfCreateOrderReq> req) {
                return null;
            }

            @Override
            public YcfBaseResult<YcfCancelOrderRes> cancelOrder(YcfBaseRequest<YcfCancelOrderReq> req) {
                return null;
            }
        };

    }
}
