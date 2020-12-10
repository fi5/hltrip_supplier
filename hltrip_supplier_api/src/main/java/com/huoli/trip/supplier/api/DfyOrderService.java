package com.huoli.trip.supplier.api;

import com.huoli.trip.supplier.self.difengyun.vo.request.DfyBookCheckRequest;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyCancelOrderRequest;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyPayOrderRequest;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyBaseResult;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyCreateOrderResponse;
import com.huoli.trip.supplier.self.yaochufa.vo.*;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseResult;

/**
 * @author :zhouwenbin
 * @time   :2020/12/10
 * @comment:迪风云订单dubbo服务接口定义
 **/
public interface DfyOrderService {

    /**
     * 可预订检查
     */
    DfyBaseResult getCheckInfos(DfyBookCheckRequest bookCheckReq);
    /**
     * 支付订单
     */
    DfyBaseResult payOrder(DfyPayOrderRequest payOrderRequest);
    /**
     * 创建订单
     */
    DfyBaseResult<DfyCreateOrderResponse> createOrder(DfyCancelOrderRequest createOrderReq);
    /**
     * 取消订单
     */
    DfyBaseResult cancelOrder(DfyCancelOrderRequest cancelOrderReq);

}
