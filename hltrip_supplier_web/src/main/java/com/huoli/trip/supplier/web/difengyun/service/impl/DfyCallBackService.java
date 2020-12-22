package com.huoli.trip.supplier.web.difengyun.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.huoli.trip.common.constant.CentralError;
import com.huoli.trip.common.constant.ConfigConstants;
import com.huoli.trip.common.entity.TripOrderRefund;
import com.huoli.trip.common.entity.TripPayOrder;
import com.huoli.trip.common.entity.TripRefundNotify;
import com.huoli.trip.common.util.ConfigGetter;
import com.huoli.trip.common.util.HttpUtil;
import com.huoli.trip.common.vo.request.PushOrderStatusReq;
import com.huoli.trip.common.vo.request.central.OrderStatusKafka;
import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.supplier.api.DfyOrderService;
import com.huoli.trip.supplier.self.difengyun.DfyOrderDetail;
import com.huoli.trip.supplier.self.difengyun.vo.push.DfyOrderPushRequest;
import com.huoli.trip.supplier.self.difengyun.vo.push.DfyOrderPushResponse;
import com.huoli.trip.supplier.self.yaochufa.vo.BaseOrderRequest;
import com.huoli.trip.supplier.web.mapper.TripOrderMapper;
import com.huoli.trip.supplier.web.mapper.TripOrderRefundMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description: 笛风状态推送
 * @date 2020/12/1014:54
 */
@Service
@Slf4j
public class DfyCallBackService {


    @Autowired
    TripOrderMapper tripOrderMapper;
    @Autowired
    TripOrderRefundMapper tripOrderRefundMapper;
    @Autowired
    DfyOrderService dfyOrderService;

    public BaseResponse orderStatusNotice(DfyOrderPushRequest request) {
        String url= ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_NAME_COMMON,"hltrip.centtral")+"/recSupplier/orderStatusNotice";
        try {
            String string = JSONObject.toJSONString(request);
            log.info("中台订单推送传参json:"+string);
            BaseOrderRequest orderDetailReq=new BaseOrderRequest();
            BaseResponse<DfyOrderDetail> dfyOrderDetail = dfyOrderService.orderDetail(orderDetailReq);
            DfyOrderDetail orderDetail = dfyOrderDetail.getData();

            List<TripPayOrder> orderPayList = tripOrderMapper.getOrderPayList(request.getOrderId());
            boolean payed=false;
            for(TripPayOrder payOrder:orderPayList){
                if(payOrder.getStatus()==1){
                    payed=true;
                    break;
                }
            }
            //只有当有支付订单并支付成功后 ,订单详情有取消的,才去考虑退款单
            if(payed&&orderDetail.getOrderStatus().equals("已取消")){
                TripOrderRefund refundOrder = tripOrderRefundMapper.getRefundingOrderByOrderId(request.getOrderId());
                if(refundOrder==null){
                    log.info("未找到待处理的退款单"+request.getOrderId());
                }else{
                    TripRefundNotify refundNotify = tripOrderRefundMapper.getRefundNotify(refundOrder.getOrderId(), refundOrder.getId());
                    TripRefundNotify notify=new TripRefundNotify();
                    notify.setOrderId(refundOrder.getOrderId());
                    notify.setRefundId(refundOrder.getId());
                    notify.setChannel("dfy");
                    notify.setStatus(0);
                    tripOrderRefundMapper.saveTripRefundNotify(notify);
                }

            }else{
                PushOrderStatusReq req =new PushOrderStatusReq();
                req.setStrStatus(orderDetail.getOrderStatus());
                req.setPartnerOrderId(request.getOrderId());
                String res = HttpUtil.doPostWithTimeout(url, JSONObject.toJSONString(req), 10000, null);
                log.info("中台orderStatusNotice返回:"+res);
            }


            return BaseResponse.success(null);
        } catch (Exception e) {
            log.info("",e);
            return BaseResponse.fail(CentralError.ERROR_SERVER_ERROR);
//            return new DfyOrderPushResponse(false,"内部通信异常","500");
        }
    }

}
