package com.huoli.trip.supplier.feign.client.lvmama.client.impl;

import com.huoli.trip.supplier.self.lvmama.vo.response.LmmOrderDetailResponse;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmScenicResponse;
import com.huoli.trip.supplier.self.lvmama.vo.response.OrderResponse;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;


/**
* @Description: 驴妈妈接口对接
* @return ${return_type}
* @throws
* @author lunatic
* @date 2021/3/15 18:11
*/
@Component
@Slf4j
public class LvmamaClientFallback implements FallbackFactory<ILvmamaClient> {


    @Override
    public ILvmamaClient create(Throwable throwable) {
        String msg = throwable == null ? "" : throwable.getMessage();
        if (!StringUtils.isEmpty(msg)) {
            log.error(msg);
        }
        return new ILvmamaClient() {

            @Override
            public LmmScenicResponse getScenicList(@RequestBody LmmScenicRequest request){
                return null;
            }
            @Override
            public LmmOrderDetailResponse orderDetail(@RequestBody LmmOrderDetailRequest request) {
                return null;
            }

            @Override
            public LmmBaseResponse getCheckInfos(ValidateOrderRequest request) {
                return null;
            }

            @Override
            public OrderResponse payOrder(OrderPaymentRequest req) {
                return null;
            }

            @Override
            public OrderResponse createOrder(CreateOrderRequest req) {
                return null;
            }

            @Override
            public OrderResponse cancelOrder(OrderUnpaidCancelRequest req) {
                return null;
            }

            @Override
            public LmmBaseResponse rufundTicket(OrderCancelRequest request) {
                return null;
            }
        };
    }
}
