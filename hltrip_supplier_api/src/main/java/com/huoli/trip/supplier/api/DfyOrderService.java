package com.huoli.trip.supplier.api;

import com.huoli.trip.common.entity.TripRefundNotify;
import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.common.vo.response.order.OrderDetailRep;
import com.huoli.trip.supplier.self.difengyun.DfyOrderDetail;
import com.huoli.trip.supplier.self.difengyun.vo.DfyToursOrderDetail;
import com.huoli.trip.supplier.self.difengyun.vo.request.*;
import com.huoli.trip.supplier.self.difengyun.vo.response.*;
import com.huoli.trip.supplier.self.yaochufa.vo.BaseOrderRequest;

/**
 * @author :zhouwenbin
 * @time   :2020/12/10
 * @comment:迪风云订单dubbo服务接口定义
 **/
public interface DfyOrderService {

	/**
	 * 迪风云门票订单相关信息
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
	 * 账单查询
	 * @param billQueryDataReq
	 * @return
	 */
	DfyBaseResult<DfyBillResponse> queryBill(DfyBillQueryDataReq billQueryDataReq);

    /**
     * 可预订检查
     */
    DfyBaseResult<DfyBookCheckResponse> getCheckInfos(DfyBookCheckRequest bookCheckReq);
    /**
     * 支付订单
     */
    DfyBaseResult payOrder(DfyPayOrderRequest payOrderRequest);
    /**
     * 创建订单
     */
    DfyBaseResult<DfyCreateOrderResponse> createOrder(DfyCreateOrderRequest createOrderReq);
    /**
     * 取消订单
     */
    DfyBaseResult cancelOrder(DfyCancelOrderRequest cancelOrderReq);

	/**
	 * 退票申请
	 * @param request
	 * @return
	 */
	DfyBaseResult<DfyRefundTicketResponse> rufundTicket(DfyRefundTicketRequest request);

	/**
	 * 处理退款通知
	 * @param item
	 */
	void processNotify(TripRefundNotify item);
	DfyBaseResult<DfyOrderStatusResponse> orderStatus(DfyOrderStatusRequest request);


	/**
	 * 迪风云跟团游戏订单详情
	 * @param request
	 * @return
	 */
	BaseResponse<DfyToursOrderDetail> toursOrderDetail(BaseOrderRequest request);

	/**
	 * 跟团游订单创建
	 * @param createOrderReq
	 * @return
	 */
	DfyBaseResult<DfyCreateOrderResponse> createToursOrder(DfyCreateToursOrderRequest createOrderReq);


	DfyBaseResult<DfyVerifyOrderResponse> verifyOrder(DfyBaseRequest<DfyOrderDetailRequest> request);


}
