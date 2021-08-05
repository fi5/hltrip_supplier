package com.huoli.trip.supplier.web.universal.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.huoli.eagle.eye.core.HuoliTrace;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.entity.TripOrderRefund;
import com.huoli.trip.common.entity.TripRefundNotify;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.supplier.api.UBROrderService;
import com.huoli.trip.supplier.feign.client.universal.client.IUBRClient;
import com.huoli.trip.supplier.self.universal.constant.UBRConstants;
import com.huoli.trip.supplier.self.universal.vo.UBROrderDetailResponse;
import com.huoli.trip.supplier.self.universal.vo.reqeust.UBRTicketOrderLocalRequest;
import com.huoli.trip.supplier.self.universal.vo.reqeust.UBRTicketOrderRequest;
import com.huoli.trip.supplier.self.universal.vo.response.UBRBaseResponse;
import com.huoli.trip.supplier.self.universal.vo.response.UBRTicketOrderResponse;
import com.huoli.trip.supplier.self.yaochufa.vo.BaseOrderRequest;
import com.huoli.trip.supplier.web.mapper.TripOrderMapper;
import com.huoli.trip.supplier.web.mapper.TripOrderRefundMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/8/3<br>
 */
@Slf4j
@Service(timeout = 10000,group = "hltrip")
public class UBROrderServiceImpl implements UBROrderService {

    @Autowired
    private HuoliTrace huoliTrace;

    @Autowired
    private IUBRClient iubrClient;

    @Autowired
    TripOrderRefundMapper tripOrderRefundMapper;

    @Autowired
    TripOrderMapper tripOrderMapper;

    @Override
    public BaseResponse createOrder(UBRTicketOrderLocalRequest request){
        tripOrderMapper.updateExtendById(request.getOrderId(), request.getUbrOrderRequest());
        return BaseResponse.withSuccess();
    }

    @Override
    public UBRBaseResponse<UBRTicketOrderResponse> payOrder(BaseOrderRequest request){
        String extend = tripOrderMapper.getExtendById(request.getOrderId());
        if(StringUtils.isBlank(extend)){
            log.error("订单 {} 没有查到btg的扩展参数", request.getOrderId());
            return null;
        }
        UBRTicketOrderRequest orderRequest = JSON.parseObject(extend, UBRTicketOrderRequest.class);
        return iubrClient.order(orderRequest);
    }

    @Override
    public UBRBaseResponse refundCheck(BaseOrderRequest request){
        return iubrClient.refundCheck(request.getSupplierOrderId());
    }

    @Override
    public UBRBaseResponse<UBRTicketOrderResponse> refund(BaseOrderRequest request){
        UBRBaseResponse<UBRTicketOrderResponse> baseResponse = iubrClient.refund(request.getSupplierOrderId());
        if(baseResponse != null && baseResponse.getCode() == 200){
            TripOrderRefund tripOrderRefund = tripOrderRefundMapper.getRefundingOrderByOrderId(request.getOrderId());
            if(tripOrderRefund == null){
                log.error("没有找到退款申请单，订单号={}", request.getOrderId());
            } else {
                TripRefundNotify refundNotify = new TripRefundNotify();
                refundNotify.setRefundId(tripOrderRefund.getId());
                refundNotify.setRefundMoney(tripOrderRefund.getChannelRefundPrice() == null ? 0f : Float.valueOf(tripOrderRefund.getChannelRefundPrice().toPlainString()));
                refundNotify.setRefundTime(DateTimeUtil.formatFullDate(new Date()));
                refundNotify.setRefundStatus(3);
                refundNotify.setOrderId(request.getOrderId());
                refundNotify.setChannel(Constants.SUPPLIER_CODE_BTG_TICKET);
                refundNotify.setCreateTime(DateTimeUtil.formatFullDate(new Date()));
                refundNotify.setStatus(0);
                tripOrderRefundMapper.saveTripRefundNotify(refundNotify);
            }
        }
        return iubrClient.refund(request.getSupplierOrderId());
    }

    @Override
    public UBRBaseResponse<UBROrderDetailResponse> orderDetail(BaseOrderRequest request){
        return iubrClient.orderDetail(request.getSupplierOrderId());
    }

    public void processNotify(){
        List<TripRefundNotify> notifyList = tripOrderRefundMapper.getRefundNotifyByChannel(Constants.SUPPLIER_CODE_BTG_TICKET);
        notifyList.forEach(n -> {
            String outOrderId = tripOrderMapper.getOutOrderIdByOrderId(n.getOrderId());
            BaseOrderRequest request = new BaseOrderRequest();
            request.setSupplierOrderId(outOrderId);
            request.setOrderId(n.getOrderId());
            request.setTraceId(huoliTrace.getTraceInfo().getTraceId());
            UBRBaseResponse<UBROrderDetailResponse> baseResponse = orderDetail(request);
            if(baseResponse != null && baseResponse.getCode() == 200 && baseResponse.getData() != null){
                UBROrderDetailResponse ubrOrderDetailResponse = baseResponse.getData();
                if(StringUtils.equals(ubrOrderDetailResponse.getStatus(), UBRConstants.ORDER_STATUS_REFUND)){
                    // todo 已退款

                } else if(StringUtils.equals(ubrOrderDetailResponse.getStatus(), UBRConstants.ORDER_STATUS_NORMAL)){
                    // todo 退款失败
                }
            }
        });

    }
}
