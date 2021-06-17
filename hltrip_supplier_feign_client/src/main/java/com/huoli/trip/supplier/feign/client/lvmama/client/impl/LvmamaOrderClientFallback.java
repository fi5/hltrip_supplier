package com.huoli.trip.supplier.feign.client.lvmama.client.impl;

import com.huoli.trip.supplier.feign.client.lvmama.client.ILvmamaOrderClient;
import com.huoli.trip.supplier.self.lvmama.vo.response.*;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;


/**
* @Description: 驴妈妈接口对接
* @return ${return_type}
* @throws
* @author lunatic
* @date 2021/3/15 18:11
*/
@Component
@Slf4j
public class LvmamaOrderClientFallback implements FallbackFactory<ILvmamaOrderClient> {


    @Override
    public ILvmamaOrderClient create(Throwable throwable) {
        String msg = throwable == null ? "" : throwable.getMessage();
        if (!StringUtils.isEmpty(msg)) {
            log.error(msg);
        }
        return new ILvmamaOrderClient() {

            @Override
            public LmmOrderDetailResponse orderDetail(@RequestParam("request") String request) {
                return null;
            }

            @Override
            public LmmBaseResponse getCheckInfos(@RequestParam("request") String request) {
                return null;
            }

            @Override
            public OrderResponse payOrder(@RequestParam("request") String request) {
                return null;
            }

            @Override
            public OrderResponse createOrder(@RequestParam("request") String request) {
                return null;
            }

            @Override
            public OrderResponse cancelOrder(@RequestParam("partnerOrderNo") String partnerOrderNo, @RequestParam("orderId") String orderId) {
                return null;
            }

            @Override
            public LmmBaseResponse refundTicket(@RequestParam("partnerOrderNo") String partnerOrderNo, @RequestParam("orderId") String orderId) {
                return null;
            }

            @Override
            public LmmBaseResponse resendCode(@RequestParam("request") String request){
                return null;
            }
        };
    }
}
