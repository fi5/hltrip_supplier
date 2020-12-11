package com.huoli.trip.supplier.api;

import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.common.vo.response.order.OrderDetailRep;
import com.huoli.trip.supplier.self.difengyun.DfyOrderDetail;
import com.huoli.trip.supplier.self.yaochufa.vo.BaseOrderRequest;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfOrderStatusResult;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfVouchersResult;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseResult;

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
	 * 迪风云订单相关信息
	 * @param request
	 * @return
	 */
	BaseResponse<DfyOrderDetail> orderDetail(BaseOrderRequest request);

	/**
	 * 重新获取凭证
	 * @param request
	 * @return
	 */
	BaseResponse<OrderDetailRep> getVochers(BaseOrderRequest request);

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
