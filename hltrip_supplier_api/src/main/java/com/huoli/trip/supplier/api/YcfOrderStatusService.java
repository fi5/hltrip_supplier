package com.huoli.trip.supplier.api;

import com.huoli.trip.supplier.self.yaochufa.vo.YcfOrderStatusResult;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfVochersResult;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseResult;

/**
 * 要出发订单dubbo服务接口定义
 */
public interface YcfOrderStatusService {
    /**
     * 主动获取要出发订单相关信息
     * @param orderId
     * @return
     */
    YcfBaseResult<YcfOrderStatusResult> getOrder(String orderId);

    /**
     * 重新获取凭证
     * @param orderId
     * @return
     */
    YcfBaseResult<YcfVochersResult> getVouchers(String orderId);
}
