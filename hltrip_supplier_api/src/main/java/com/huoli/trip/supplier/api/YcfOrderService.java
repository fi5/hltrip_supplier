package com.huoli.trip.supplier.api;

import com.huoli.trip.supplier.self.yaochufa.vo.YcfBookCheckReq;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfBookCheckRes;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfOrderStatusResult;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfVochersResult;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfCommonResult;

/**
 * 要出发订单dubbo服务接口定义
 */
public interface YcfOrderService {
    /**
     * 主动获取要出发订单相关信息
     * @param orderId
     * @return
     */
    YcfCommonResult<YcfOrderStatusResult> getOrder(String orderId);

    /**
     * 重新获取凭证
     * @param orderId
     * @return
     */
    YcfCommonResult<YcfVochersResult> getVochers(String orderId);
    /**
     * 可预订检查
     * @param= req
     * @return= BookCheckRes
     * @author= wangdm
     *
     */
    YcfCommonResult<YcfBookCheckRes> getCheckInfos(YcfBookCheckReq req);
}
