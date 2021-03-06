package com.huoli.trip.supplier.web.universal.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huoli.eagle.eye.core.HuoliTrace;
import com.huoli.hlwx.tool.method.BigDecimalUtil;
import com.huoli.trip.common.constant.ConfigConstants;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.constant.OrderStatus;
import com.huoli.trip.common.entity.TripOrder;
import com.huoli.trip.common.entity.TripOrderRefund;
import com.huoli.trip.common.entity.TripRefundNotify;
import com.huoli.trip.common.util.ConfigGetter;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.common.util.HttpUtil;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.common.vo.request.PushOrderStatusReq;
import com.huoli.trip.common.vo.request.RefundNoticeReq;
import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.supplier.api.UBROrderService;
import com.huoli.trip.supplier.feign.client.universal.client.IUBRClient;
import com.huoli.trip.supplier.self.common.ConstConfig;
import com.huoli.trip.supplier.self.universal.constant.UBRConstants;
import com.huoli.trip.supplier.self.universal.vo.UBROrderDetailResponse;
import com.huoli.trip.supplier.self.universal.vo.reqeust.UBRTicketOrderRequest;
import com.huoli.trip.supplier.self.universal.vo.response.UBRBaseResponse;
import com.huoli.trip.supplier.self.universal.vo.response.UBRRefundCheckResponse;
import com.huoli.trip.supplier.self.universal.vo.response.UBRRefundCheckResponseCustom;
import com.huoli.trip.supplier.self.universal.vo.response.UBRTicketOrderResponse;
import com.huoli.trip.supplier.self.yaochufa.vo.BaseOrderRequest;
import com.huoli.trip.supplier.web.config.TraceConfig;
import com.huoli.trip.supplier.web.mapper.TripOrderMapper;
import com.huoli.trip.supplier.web.mapper.TripOrderRefundMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * ?????????<br/>
 * ?????????Copyright (c) 2011-2020<br>
 * ?????????????????????<br>
 * ??????????????????<br>
 * ?????????1.0<br>
 * ???????????????2021/8/3<br>
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
    public BaseResponse createOrder(){
        return BaseResponse.withSuccess();
    }

    @Override
    public UBRBaseResponse<UBRTicketOrderResponse> payOrder(BaseOrderRequest request){
        TripOrder order = tripOrderMapper.getOrderByOrderId(request.getOrderId());
        if(StringUtils.isBlank(order.getExtend())){
            log.error("?????? {} ????????????btg???????????????", request.getOrderId());
            return null;
        }
        UBRTicketOrderRequest orderRequest = JSON.parseObject(order.getExtend(), UBRTicketOrderRequest.class);
        UBRBaseResponse<UBRTicketOrderResponse> baseResponse = iubrClient.order(orderRequest);
        if(baseResponse != null && baseResponse.getCode() == 200 && baseResponse.getData() != null){
            String status = baseResponse.getData().getStatus();
            int channelStatus = order.getChannelStatus();
            if(StringUtils.equals(UBRConstants.ORDER_STATUS_PROCESS, status)){
                channelStatus = OrderStatus.TO_BE_CONFIRMED.getCode();
            } else if(StringUtils.equals(UBRConstants.ORDER_STATUS_NORMAL, status) ){
                channelStatus = OrderStatus.WAITING_TO_TRAVEL.getCode();
            } else if(StringUtils.equals(UBRConstants.ORDER_STATUS_BUY_FILED, status)){
                channelStatus = OrderStatus.REFUNDED.getCode();
            }
            tripOrderMapper.updateOutOrderIdById(request.getOrderId(), channelStatus, baseResponse.getData().getOrderId());
        }
        return baseResponse;
    }

    @Override
    public UBRBaseResponse<UBRRefundCheckResponseCustom> refundCheck(BaseOrderRequest request){
        // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        TripOrder tripOrder = tripOrderMapper.getOrderByOrderId(request.getOrderId());
        if(StringUtils.isBlank(tripOrder.getOutOrderId())){
            UBRRefundCheckResponseCustom ubrRefundCheckResponse = new UBRRefundCheckResponseCustom();
            ubrRefundCheckResponse.setRefundFee(new BigDecimal(0));
            ubrRefundCheckResponse.setRefundAllow(true);
            ubrRefundCheckResponse.setRefundPrice(BigDecimal.valueOf(BigDecimalUtil.sub(tripOrder.getOutPayPrice().doubleValue(),
                    ubrRefundCheckResponse.getRefundFee().doubleValue())));
            UBRBaseResponse ubrBaseResponse = new UBRBaseResponse();
            ubrBaseResponse.setCode(200);
            ubrBaseResponse.setData(ubrRefundCheckResponse);
            return ubrBaseResponse;
        }
        UBRBaseResponse<UBRRefundCheckResponse> ubrBaseResponse = iubrClient.refundCheck(tripOrder.getOutOrderId());
        if(ubrBaseResponse == null){
            return null;
        }
        UBRRefundCheckResponseCustom custom;
        if(ubrBaseResponse.getData() != null){
            custom = new UBRRefundCheckResponseCustom();
            BeanUtils.copyProperties(ubrBaseResponse.getData(), custom);
            custom.setRefundPrice(BigDecimal.valueOf(BigDecimalUtil.sub(tripOrder.getOutPayPrice().doubleValue(),
                    ubrBaseResponse.getData().getRefundFee().doubleValue())));
            ubrBaseResponse.setData(custom);
        }
        UBRBaseResponse<UBRRefundCheckResponseCustom> ubrBaseResponse1 = new UBRBaseResponse<>();
        BeanUtils.copyProperties(ubrBaseResponse, ubrBaseResponse1);
        return ubrBaseResponse1;
    }

    @Override
    public UBRBaseResponse<UBRTicketOrderResponse> refund(BaseOrderRequest request){
        // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        TripOrder tripOrder = tripOrderMapper.getOrderByOrderId(request.getOrderId());
        if(StringUtils.isBlank(tripOrder.getOutOrderId())){
            UBRBaseResponse ubrBaseResponse = new UBRBaseResponse();
            ubrBaseResponse.setCode(200);
            return ubrBaseResponse;
        }
        UBRBaseResponse<UBRTicketOrderResponse> baseResponse = iubrClient.refund(request.getSupplierOrderId());
        if(baseResponse != null && baseResponse.getCode() == 200){
            TripOrderRefund tripOrderRefund = tripOrderRefundMapper.getRefundingOrderByOrderId(request.getOrderId());
            if(tripOrderRefund == null){
                log.error("???????????????????????????????????????={}", request.getOrderId());
            } else {
                TripRefundNotify refundNotify = new TripRefundNotify();
                refundNotify.setRefundId(tripOrderRefund.getId());
                refundNotify.setRefundMoney(tripOrder.getOutPayPrice().floatValue());
                refundNotify.setRefundTime(DateTimeUtil.formatFullDate(new Date()));
                refundNotify.setRefundStatus(3);
                refundNotify.setOrderId(request.getOrderId());
                refundNotify.setChannel(Constants.SUPPLIER_CODE_BTG_TICKET);
                refundNotify.setCreateTime(DateTimeUtil.formatFullDate(new Date()));
                refundNotify.setStatus(0);
                tripOrderRefundMapper.saveTripRefundNotify(refundNotify);
            }
        }
        return baseResponse;
    }

    @Override
    public UBRBaseResponse<UBROrderDetailResponse> orderDetail(BaseOrderRequest request){
        if(StringUtils.isBlank(request.getSupplierOrderId())){
            return null;
        }
        UBRBaseResponse<UBROrderDetailResponse> baseResponse = iubrClient.orderDetail(request.getSupplierOrderId());
        if(baseResponse != null){
            UBROrderDetailResponse detailResponse = baseResponse.getData();
            if(StringUtils.equals(detailResponse.getStatus(), UBRConstants.ORDER_STATUS_CANCEL)
                    || StringUtils.equals(detailResponse.getStatus(), UBRConstants.ORDER_STATUS_BUY_FILED) ){
                orderFailed(request.getOrderId());
            }
            // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            else if(StringUtils.equals(detailResponse.getStatus(), UBRConstants.ORDER_STATUS_REFUND)){
                TripRefundNotify tripRefundNotify = tripOrderRefundMapper.getRefundNotifyByOrderId(request.getOrderId());
                if(tripRefundNotify == null){
                    TripOrder order = tripOrderMapper.getOrderByOrderId(request.getOrderId());
                    TripRefundNotify notify = new TripRefundNotify();
                    notify.setStatus(0); // ???????????????
                    notify.setChannel(Constants.SUPPLIER_CODE_BTG_TICKET);
                    notify.setOrderId(request.getOrderId());
                    notify.setRefundStatus(1);
                    notify.setRefundTime(DateTimeUtil.formatFullDate(new Date()));
                    notify.setRefundMoney(order.getOutPayPrice().floatValue());
                    notify.setCreateTime(DateTimeUtil.formatFullDate(new Date()));
                    notify.setRemark("?????????????????????????????????");
                    tripOrderRefundMapper.saveTripRefundNotify(notify);
                }
            }
        }
        return baseResponse;
    }

    private void orderFailed(String orderId){
        TripRefundNotify tripRefundNotify = tripOrderRefundMapper.getRefundNotifyByOrderId(orderId);
        if(tripRefundNotify == null){
            TripOrder order = tripOrderMapper.getOrderByOrderId(orderId);
            TripOrderRefund refund = tripOrderRefundMapper.getRefundingOrderByOrderId(orderId);
            BigDecimal refundFee = new BigDecimal(0);
            if(refund != null){
                refundFee = refund.getChannelRefundCharge();
            }
            refunded(order.getOrderId(), order.getOutPayPrice(), refundFee, 0, 1);
            try {
                // ?????????????????????????????????????????????????????????????????????????????????????????????
                TripRefundNotify notify = new TripRefundNotify();
                notify.setStatus(1);
                notify.setChannel(Constants.SUPPLIER_CODE_BTG_TICKET);
                notify.setOrderId(orderId);
                notify.setRefundStatus(1);
                notify.setRefundTime(DateTimeUtil.formatFullDate(new Date()));
                notify.setRefundMoney(order.getOutPayPrice().floatValue());
                notify.setCreateTime(DateTimeUtil.formatFullDate(new Date()));
                notify.setRemark("?????????????????????");
                tripOrderRefundMapper.saveTripRefundNotify(notify);
            } catch (Exception e) {
                log.error("btg??????????????????????????????????????????", e);
            }
        }
    }

    @Override
    public void processNotify(){
        List<TripRefundNotify> notifyList = tripOrderRefundMapper.getRefundNotifyByChannel(
                Constants.SUPPLIER_CODE_BTG_TICKET);
        if(ListUtils.isEmpty(notifyList)){
            log.info("btg?????????????????????????????????");
            return;
        }
        notifyList.forEach(n -> {
            try {
                TripOrder tripOrder = tripOrderMapper.getOrderByOrderId(n.getOrderId());
                // consumer????????????kafka?????????????????????
                TripOrderRefund tripOrderRefund = tripOrderRefundMapper.getRefundingOrderByOrderId(n.getOrderId());
                BigDecimal refundFee = new BigDecimal(0);
                if(tripOrderRefund != null){
                    refundFee = tripOrderRefund.getChannelRefundCharge();
                }
                BaseOrderRequest request = new BaseOrderRequest();
                request.setSupplierOrderId(tripOrder.getOutOrderId());
                request.setOrderId(n.getOrderId());
                request.setTraceId(huoliTrace.getTraceInfo().getTraceId());
                UBRBaseResponse<UBROrderDetailResponse> baseResponse = orderDetail(request);
                if(baseResponse != null && baseResponse.getCode() == 200 && baseResponse.getData() != null) {
                    UBROrderDetailResponse ubrOrderDetailResponse = baseResponse.getData();
                    refundNotify(n, ubrOrderDetailResponse.getStatus(), tripOrder.getOrderId(), refundFee);
                }
            } catch (Exception e) {
                log.error("btg???????????????????????????id={}???orderId={}, {refundId}",
                        n.getId(), n.getOrderId(), n.getRefundId(), e);
            }
        });
    }

    private void refundNotify(TripRefundNotify n, String ubrStatus, String orderId, BigDecimal refundFee){
        if(StringUtils.equals(ubrStatus, UBRConstants.ORDER_STATUS_REFUND)){
            n.setStatus(1);
            tripOrderRefundMapper.updateRefundNotify(n);
            refunded(orderId, BigDecimal.valueOf(n.getRefundMoney()), refundFee, n.getRefundId(), 1);
            //??????????????????????????????
            statusChanged(orderId, UBRConstants.ORDER_STATUS_REFUND);
        } else if(StringUtils.equals(ubrStatus, UBRConstants.ORDER_STATUS_NORMAL)){  // ?????????????????????????????????????????????????????????????????????????????????????????????
            n.setStatus(2);
            tripOrderRefundMapper.updateRefundNotify(n);
            refunded(orderId, BigDecimal.valueOf(n.getRefundMoney()), refundFee, n.getRefundId(), -1);
        }
    }

    private void refunded(String orderId, BigDecimal refundMoney, BigDecimal refundFee, int refundId, int refundStatus){
        String centralUrl = ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_NAME_COMMON, ConstConfig.CONFIG_CENTRAL_URL);
        String refundUrl =  String.format("%s%s", centralUrl, ConstConfig.CONFIG_CENTRAL_REFUND_NOTICE);
        RefundNoticeReq req = new RefundNoticeReq();
        req.setPartnerOrderId(orderId);
        req.setRefundFrom(2);
        if(refundFee != null && refundMoney != null){
            refundMoney = BigDecimal.valueOf(BigDecimalUtil.sub(refundMoney.doubleValue(), refundFee.doubleValue()));
        }
        req.setRefundPrice(refundMoney);
        req.setResponseTime(DateTimeUtil.formatFullDate(new Date()));
        req.setSource("btg");
        if(refundId > 0){
            req.setRefundId(refundId);
        }
        req.setRefundStatus(refundStatus);
        req.setRefundCharge(refundFee);
        log.info("btg??????????????????????????????url = {} , ?????? = {}", refundUrl, JSON.toJSONString(req));
        String res = HttpUtil.doPostWithTimeout(refundUrl, JSONObject.toJSONString(req), 10000, TraceConfig.traceHeaders(huoliTrace, refundUrl));
        log.info("btg???????????????????????????{}", res);
    }

    private void statusChanged(String orderId, String ubrStatus){
        String centralUrl = ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_NAME_COMMON, ConstConfig.CONFIG_CENTRAL_URL);
        PushOrderStatusReq statusReq =new PushOrderStatusReq();
        statusReq.setStrStatus(ubrStatus);
        statusReq.setPartnerOrderId(orderId);
        statusReq.setType(6);

        String statusUrl =  String.format("%s%s", centralUrl, ConstConfig.CONFIG_CENTRAL_ORDER_STATUS_NOTICE);
        log.info("btg??????????????????????????????url = {} , ?????? = {}", statusUrl, JSON.toJSONString(statusReq));
        String res3 = HttpUtil.doPostWithTimeout(statusUrl, JSONObject.toJSONString(statusReq), 10000, TraceConfig.traceHeaders(huoliTrace, statusUrl));
        log.info("btg????????????????????????:"+res3);
    }
}
