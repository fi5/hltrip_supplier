package com.huoli.trip.supplier.api;

import com.huoli.trip.supplier.self.yaochufa.vo.YcfOrderStatusResult;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfCommonResult;

/**
 * 要出发订单dubbo服务接口定义
 */
public interface YcfOrderStatusService {
    /**
     * 主动获取要出发订单相关信息
     * @param partnerOrderId
     * @return
     */
    YcfCommonResult<YcfOrderStatusResult> getOrder(String partnerOrderId);


}
