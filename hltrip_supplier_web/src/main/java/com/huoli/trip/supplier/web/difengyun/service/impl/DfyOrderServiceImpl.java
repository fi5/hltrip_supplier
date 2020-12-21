package com.huoli.trip.supplier.web.difengyun.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.huoli.trip.common.constant.CentralError;
import com.huoli.trip.common.entity.TripRefundNotify;
import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.common.vo.response.order.OrderDetailRep;
import com.huoli.trip.supplier.api.DfyOrderService;
import com.huoli.trip.supplier.feign.client.difengyun.client.IDiFengYunClient;
import com.huoli.trip.supplier.self.difengyun.vo.DfyScenic;
import com.huoli.trip.supplier.self.difengyun.vo.DfyScenicDetail;
import com.huoli.trip.supplier.self.difengyun.vo.request.*;
import com.huoli.trip.supplier.self.difengyun.DfyOrderDetail;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyBaseRequest;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyOrderDetailRequest;
import com.huoli.trip.supplier.self.difengyun.vo.response.*;
import com.huoli.trip.supplier.self.yaochufa.vo.BaseOrderRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author :zhouwenbin
 * @time   :2020/12/10
 * @comment:
 **/
@Slf4j
@Service(timeout = 10000,group = "hltrip")
public class DfyOrderServiceImpl implements DfyOrderService {

    @Autowired
    private IDiFengYunClient diFengYunClient;

    public BaseResponse<DfyOrderDetail> orderDetail(BaseOrderRequest request){

        DfyOrderDetailRequest dfyOrderDetailBody=new DfyOrderDetailRequest();
        DfyBaseRequest<DfyOrderDetailRequest> dfyOrderDetailReq = new DfyBaseRequest<>(dfyOrderDetailBody);
        dfyOrderDetailBody.setOrderId(request.getSupplierOrderId());
        try {
            DfyBaseResult<DfyOrderDetail> baseResult = diFengYunClient.orderDetail(dfyOrderDetailReq);


            DfyOrderDetail detail = baseResult.getData();
            return BaseResponse.success(detail);
        } catch (Exception e) {
        	log.error("信息{}",e);
            return BaseResponse.fail(CentralError.ERROR_UNKNOWN);
        }


    }

    @Override
    public BaseResponse<OrderDetailRep> getVochers(BaseOrderRequest request) {
        DfyOrderDetailRequest dfyOrderDetailBody=new DfyOrderDetailRequest();
        DfyBaseRequest<DfyOrderDetailRequest> dfyOrderDetailReq = new DfyBaseRequest<>(dfyOrderDetailBody);
        dfyOrderDetailBody.setOrderId(request.getSupplierOrderId());
        try {
            DfyBaseResult<DfyOrderDetail> baseResult = diFengYunClient.orderDetail(dfyOrderDetailReq);


            DfyOrderDetail dfyOrderDetail = baseResult.getData();
            OrderDetailRep detailResp=new OrderDetailRep();
            detailResp.setOrderId(dfyOrderDetail.getOrderId());

            return BaseResponse.success(detailResp);
        } catch (Exception e) {
            log.error("信息{}",e);
            return BaseResponse.fail(CentralError.ERROR_UNKNOWN);
        }
    }

    @Override
    public DfyBaseResult<DfyBillResponse> queryBill(DfyBillQueryDataReq billQueryDataReq) {
        try {
            DfyBaseRequest dfyBaseRequest = new DfyBaseRequest();
            dfyBaseRequest.setData(billQueryDataReq);
            DfyBaseResult<DfyBillResponse> dfyBillResponse = diFengYunClient.queryBill(dfyBaseRequest);

            return dfyBillResponse;
        } catch (Exception e) {
            log.error("信息{}",e);
            return null;
        }
    }

    @Override
    public DfyBaseResult getCheckInfos(DfyBookCheckRequest bookCheckReq) {
        return new DfyBaseResult("success",true);
    }

    @Override
    public DfyBaseResult payOrder(DfyPayOrderRequest payOrderRequest) {
        return new DfyBaseResult("success",true);
    }

    @Override
    public DfyBaseResult<DfyCreateOrderResponse> createOrder(DfyCreateOrderRequest createOrderReq) {
        DfyBaseRequest dfyBaseRequest = new DfyBaseRequest();
        dfyBaseRequest.setData(createOrderReq);
        return diFengYunClient.createOrder(dfyBaseRequest);
    }

    @Override
    public DfyBaseResult cancelOrder(DfyCancelOrderRequest cancelOrderReq) {
        DfyBaseRequest dfyBaseRequest = new DfyBaseRequest();
        dfyBaseRequest.setData(cancelOrderReq);
        return diFengYunClient.cancelOrder(dfyBaseRequest);
    }

    @Override
    public DfyBaseResult<DfyRefundTicketResponse> rufundTicket(DfyRefundTicketRequest request) {
        DfyBaseRequest dfyBaseRequest = new DfyBaseRequest();
        dfyBaseRequest.setData(request);
        return diFengYunClient.refundTicket(dfyBaseRequest);
    }

    @Override
    public void processNotify(TripRefundNotify item) {

    }
}
