package com.huoli.trip.supplier.web.universal.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huoli.eagle.eye.core.HuoliTrace;
import com.huoli.trip.common.constant.ConfigConstants;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.entity.TripOrder;
import com.huoli.trip.common.entity.TripOrderRefund;
import com.huoli.trip.common.entity.TripRefundNotify;
import com.huoli.trip.common.util.ConfigGetter;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.common.util.HttpUtil;
import com.huoli.trip.common.vo.request.PushOrderStatusReq;
import com.huoli.trip.common.vo.request.RefundNoticeReq;
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
import com.huoli.trip.supplier.web.config.TraceConfig;
import com.huoli.trip.supplier.web.mapper.TripOrderMapper;
import com.huoli.trip.supplier.web.mapper.TripOrderRefundMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
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
            TripOrder tripOrder = tripOrderMapper.getOrderByOrderId(n.getOrderId());
            BaseOrderRequest request = new BaseOrderRequest();
            request.setSupplierOrderId(tripOrder.getOutOrderId());
            request.setOrderId(n.getOrderId());
            request.setTraceId(huoliTrace.getTraceInfo().getTraceId());
            UBRBaseResponse<UBROrderDetailResponse> baseResponse = orderDetail(request);
            if(baseResponse != null && baseResponse.getCode() == 200 && baseResponse.getData() != null){
                UBROrderDetailResponse ubrOrderDetailResponse = baseResponse.getData();
                if(StringUtils.equals(ubrOrderDetailResponse.getStatus(), UBRConstants.ORDER_STATUS_REFUND)){
                    // todo 已退款
                    item.setStatus(1);
                    item.setRefundStatus(bill.getStatus());
                    item.setRefundTime(bill.getTime());
                    item.setRefundMoney(bill.getAmount());
                    item.setBillInfo(JSONObject.toJSONString(bill));
                    tripOrderRefundMapper.updateRefundNotify(item);

                    req.setRefundTime(bill.getTime());

                    if(item.getRefundId()>0){
                        TripOrderRefund refundOrder = tripOrderRefundMapper.getRefundOrderById(item.getRefundId());
                        req.setRefundId(refundOrder.getId());
                    }else{
                        log.info("无退款单子但是渠道退款了:"+item);
                    }

                    req.setRefundStatus(1);

                    log.info("doRefund请求的地址:"+url+",参数:"+ JSONObject.toJSONString(req)+",refundStatus:"+item.getOrderId());
                    String res = HttpUtil.doPostWithTimeout(url, JSONObject.toJSONString(req), 10000, TraceConfig.traceHeaders(huoliTrace, url));
                    log.info("中台refundNotice返回:"+res);

                    //退款成功后再发个通知
                    PushOrderStatusReq statusReq =new PushOrderStatusReq();
                    statusReq.setStrStatus("已退款");
                    statusReq.setPartnerOrderId(tripOrder.getOrderId());
                    statusReq.setType(3);

                    log.info("processNotify中台订单推送传参json:"+req);
                    String statusUrl=ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_NAME_COMMON,"hltrip.centtral")+"/recSupplier/orderStatusNotice";
                    String res3 = HttpUtil.doPostWithTimeout(statusUrl, JSONObject.toJSONString(req), 10000, TraceConfig.traceHeaders(huoliTrace, statusUrl));
                    log.info("processNotify中台orderStatusNotice返回:"+res3);


                } else if(StringUtils.equals(ubrOrderDetailResponse.getStatus(), UBRConstants.ORDER_STATUS_NORMAL)){  // 供应商退款失败会变成正常状态，如果我们有退款任务说明是退款失败
                    n.setStatus(2);
                    tripOrderRefundMapper.updateRefundNotify(n);

                    String url= ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_NAME_COMMON,"hltrip.centtral")+"/recSupplier/refundNotice";
                    RefundNoticeReq req=new RefundNoticeReq();
                    req.setPartnerOrderId(n.getOrderId());
                    req.setRefundFrom(2);
                    req.setRefundPrice(BigDecimal.valueOf(n.getRefundMoney()));
                    req.setResponseTime(DateTimeUtil.formatFullDate(new Date()));
                    req.setSource("btg");
                    BigDecimal refundCharge=tripOrder.getOutPayPrice().subtract(req.getRefundPrice());
                    req.setRefundCharge(refundCharge);
                    req.setRefundStatus(-1);
                    log.info("btg退款失败doRefund请求的地址:"+url+",参数:" + JSONObject.toJSONString(req) + ",orderId:" + tripOrder.getOrderId());
                    String res2 = HttpUtil.doPostWithTimeout(url, JSONObject.toJSONString(req), 10000, TraceConfig.traceHeaders(huoliTrace, url));
                    log.info("btg中台refundNotice返回:"+res2);

                }
            }
        });

    }
}
