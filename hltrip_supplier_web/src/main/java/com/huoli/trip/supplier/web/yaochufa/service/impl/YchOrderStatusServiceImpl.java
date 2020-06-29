package com.huoli.trip.supplier.web.yaochufa.service.impl;

import com.huoli.trip.supplier.api.YcfOrderStatusService;
import com.huoli.trip.supplier.feign.client.yaochufa.client.IYaoChuFaClient;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfOrderBaSeRequest;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfOrderStatusResult;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfVouchersResult;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseRequest;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseResult;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 要出发订单dubbo服务接口实现
 */

@Service(timeout = 10000,group = "hllx")
public class YchOrderStatusServiceImpl  implements YcfOrderStatusService {
    @Autowired
    private IYaoChuFaClient iYaoChuFaClient;

    @Override
    public YcfBaseResult<YcfOrderStatusResult> getOrder(String orderId) {
            YcfBaseRequest request = new YcfBaseRequest();
            YcfOrderBaSeRequest orderBaSeRequest = new YcfOrderBaSeRequest();
            orderBaSeRequest.setPartnerOrderId(orderId);
            request.setData(orderBaSeRequest);
            return iYaoChuFaClient.getOrderStatus(request);
    }

    @Override
    public YcfBaseResult<YcfVouchersResult> getVouchers(String orderId) {
        YcfBaseRequest request = new YcfBaseRequest();
        YcfOrderBaSeRequest orderBaSeRequest = new YcfOrderBaSeRequest();
        orderBaSeRequest.setPartnerOrderId(orderId);
        request.setData(orderBaSeRequest);
        return iYaoChuFaClient.getVouchers(request);
    }


}

