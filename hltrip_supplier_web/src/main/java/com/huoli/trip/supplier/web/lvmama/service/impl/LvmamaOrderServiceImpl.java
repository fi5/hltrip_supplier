package com.huoli.trip.supplier.web.lvmama.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huoli.eagle.eye.core.HuoliTrace;
import com.huoli.trip.common.constant.CentralError;
import com.huoli.trip.common.constant.ConfigConstants;
import com.huoli.trip.common.entity.TripOrder;
import com.huoli.trip.common.entity.TripOrderRefund;
import com.huoli.trip.common.util.ConfigGetter;
import com.huoli.trip.common.util.HttpUtil;
import com.huoli.trip.common.vo.request.PushOrderStatusReq;
import com.huoli.trip.common.vo.request.RefundNoticeReq;
import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.supplier.api.LvmamaOrderService;
import com.huoli.trip.supplier.feign.client.lvmama.client.ILvmamaClient;
import com.huoli.trip.supplier.self.difengyun.DfyOrderDetail;
import com.huoli.trip.supplier.self.lvmama.vo.LvOrderDetail;
import com.huoli.trip.supplier.self.lvmama.vo.push.LmmOrderPushRequest;
import com.huoli.trip.supplier.self.lvmama.vo.push.LmmRefundPushRequest;
import com.huoli.trip.supplier.self.lvmama.vo.request.*;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmBaseResponse;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmOrderDetailResponse;
import com.huoli.trip.supplier.self.lvmama.vo.response.OrderResponse;
import com.huoli.trip.supplier.self.yaochufa.vo.BaseOrderRequest;
import com.huoli.trip.supplier.web.config.TraceConfig;
import com.huoli.trip.supplier.web.mapper.TripOrderMapper;
import com.huoli.trip.supplier.web.mapper.TripOrderRefundMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2021/3/1517:48
 */
@Slf4j
@Service(timeout = 10000,group = "hltrip")
public class LvmamaOrderServiceImpl implements LvmamaOrderService {
    @Autowired
    private ILvmamaClient iLvmamaClient;
    @Autowired
    private HuoliTrace huoliTrace;

    @Autowired
    TripOrderRefundMapper tripOrderRefundMapper;
    @Autowired
    TripOrderMapper tripOrderMapper;

    @Override
    public BaseResponse<LvOrderDetail> orderDetail(BaseOrderRequest request) {
        try {

            LmmOrderDetailRequest lmmDetailReq=new LmmOrderDetailRequest();
            LmmOrderDetailRequest.LmmOrderReq reqInnerOrder=new LmmOrderDetailRequest.LmmOrderReq();
            reqInnerOrder.setPartnerOrderNos(request.getOrderId());
            lmmDetailReq.setOrder(reqInnerOrder);

            LmmOrderDetailResponse lmmOrderDetailResponse = iLvmamaClient.orderDetail(getRequest(request));
            LvOrderDetail detail=lmmOrderDetailResponse.getOrder();
            String gjStatus="待确认";
            if(StringUtils.equals(detail.getPaymentStatus(),"PAYED")){
                if(StringUtils.equals(detail.getCredenctStatus(),"CREDENCE_SEND"))
                    gjStatus="已发送";
                if(StringUtils.equals(detail.getCredenctStatus(),"CREDENCE_NO_SEND"))
                    gjStatus="未发送";
                if(StringUtils.equals(detail.getStatus(),"CANCEL"))
                    gjStatus="已退款";
            }else{
                if(StringUtils.equals(detail.getStatus(),"NORMAL"))
                    gjStatus="待付款";
                if(StringUtils.equals(detail.getStatus(),"CANCEL"))
                    gjStatus="已取消";
            }

            if(StringUtils.equals(detail.getPerformStatus(),"USED"))
                gjStatus="已消费";
            if(StringUtils.equals(detail.getPerformStatus(),"UNUSE"))
                gjStatus="未使用";

            TripOrder tripOrder = tripOrderMapper.getOrderByOutOrderId(detail.getOrderId());
            TripOrderRefund refundOrder = tripOrderRefundMapper.getRefundingOrderByOrderId(tripOrder.getOrderId());
            if(refundOrder!=null && refundOrder.getChannelRefundStatus()==0) {//写退款失败
                gjStatus="申请退款中";
            }

            detail.setGjStatus(gjStatus);
            return BaseResponse.success(detail);
        } catch (Exception e) {
        	log.error("信息{}",e);
            return BaseResponse.fail(CentralError.ERROR_NO_ORDER);
        }
    }

    @Override
    public LmmBaseResponse orderStatusNotice(LmmOrderPushRequest request) {
        try {
            BaseOrderRequest detailReq=new BaseOrderRequest();
            BaseResponse<LvOrderDetail> lvOrderDetail = orderDetail(detailReq);
            LvOrderDetail detail = lvOrderDetail.getData();
            PushOrderStatusReq req =new PushOrderStatusReq();
            req.setStrStatus(detail.getGjStatus());
            req.setPartnerOrderId(request.getOrder().getPartnerOrderNo());
            req.setVochers(genTicketsVoucher(detail));
            orderStatusNotice(req);
        } catch (Exception e) {
        	log.error("信息{}",e);
        }

        return null;
    }

    private List<PushOrderStatusReq.Voucher> genTicketsVoucher(LvOrderDetail detail) {
        if (CollectionUtils.isNotEmpty(detail.getCredentials())) {
            List<PushOrderStatusReq.Voucher> vochers = new ArrayList<>();
            try {
                for (LvOrderDetail.Credential oneInfo : detail.getCredentials()) {

                    if (StringUtils.isNotBlank(oneInfo.getQRcode())) {
                        PushOrderStatusReq.Voucher oneVoucher = new PushOrderStatusReq.Voucher();
                        oneVoucher.setVocherUrl(oneInfo.getQRcode());
                        oneVoucher.setType(2);
                        vochers.add(oneVoucher);
                    }
                    if (StringUtils.isNotBlank(oneInfo.getVoucherUrl())) {
                        PushOrderStatusReq.Voucher oneVoucher = new PushOrderStatusReq.Voucher();
                        oneVoucher.setVocherUrl(oneInfo.getVoucherUrl());
                        oneVoucher.setType(2);
                        vochers.add(oneVoucher);
                    }
                    if (StringUtils.isNotBlank(oneInfo.getSerialCode())) {
                        PushOrderStatusReq.Voucher oneVoucher = new PushOrderStatusReq.Voucher();
                        oneVoucher.setVocherNo(oneInfo.getSerialCode());
                        oneVoucher.setType(1);
                        vochers.add(oneVoucher);
                    }
                    if (StringUtils.isNotBlank(oneInfo.getAdditional())) {
                        PushOrderStatusReq.Voucher oneVoucher = new PushOrderStatusReq.Voucher();
                        oneVoucher.setVocherNo(oneInfo.getAdditional());
                        oneVoucher.setType(1);
                        vochers.add(oneVoucher);
                    }

                }
                return vochers;

            } catch (Exception e) {
                log.error("genVouchers错", e);
            }
        }
        return null;
    }

    @Override
    public LmmBaseResponse pushOrderRefund(LmmRefundPushRequest request) {

        LmmRefundPushRequest.LmmRefundPushBody refundBody = request.getOrder();
        RefundNoticeReq req=new RefundNoticeReq();
        req.setPartnerOrderId(refundBody.getPartnerOrderID());
        req.setRefundFrom(2);
        req.setRefundPrice(new BigDecimal(refundBody.getRefundAmount()*100));
//        req.setResponseTime();
        req.setSource("lvmama");
        BigDecimal refundCharge=new BigDecimal(refundBody.getFactorage()*100);
        req.setRefundCharge(refundCharge);
        req.setRefundStatus(1);


        String refundNotiUrl= ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_NAME_COMMON,"hltrip.centtral")+"/recSupplier/refundNotice";
        log.info("doRefund请求的地址:"+refundNotiUrl+",参数:"+ JSONObject.toJSONString(req));
        String res = HttpUtil.doPostWithTimeout(refundNotiUrl, JSONObject.toJSONString(req), 10000, TraceConfig.traceHeaders(huoliTrace, refundNotiUrl));
        log.info("中台refundNotice返回:"+res);


        PushOrderStatusReq statusReq =new PushOrderStatusReq();
        statusReq.setStrStatus("退款成功");
        statusReq.setPartnerOrderId(refundBody.getPartnerOrderID());
        orderStatusNotice(statusReq);

        return null;
    }

    public void orderStatusNotice(PushOrderStatusReq req) {
        req.setType(5);
        try {
            log.info("中台订单推送传参json:" +  JSONObject.toJSONString(req));
            String statusUrl=ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_NAME_COMMON,"hltrip.centtral")+"/recSupplier/orderStatusNotice";
            String res = HttpUtil.doPostWithTimeout(statusUrl, JSONObject.toJSONString(req), 10000, TraceConfig.traceHeaders(huoliTrace, statusUrl));
            log.info("驴妈妈orderStatusNotice推给中台orderStatusNotice返回:" + res);
        } catch (Exception e) {
            log.info("", e);
        }
    }

    @Override
    public LmmBaseResponse getCheckInfos(ValidateOrderRequest request) {
        return iLvmamaClient.getCheckInfos(getRequest(request));
    }

    @Override
    public OrderResponse payOrder(OrderPaymentRequest request) {
        return iLvmamaClient.payOrder(getRequest(request));
    }

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        return iLvmamaClient.createOrder(getRequest(request));
    }

    @Override
    public OrderResponse cancelOrder(OrderUnpaidCancelRequest request) {
        return iLvmamaClient.cancelOrder(request.getPartnerOrderNo(), request.getOrderId());
    }

    @Override
    public LmmBaseResponse refundTicket(OrderCancelRequest request) {
        return iLvmamaClient.refundTicket(request.getPartnerOrderNo(), request.getOrderId());
    }

    private <T> String getRequest(T obj){
        LmmBodyRequest<T> bodyRequest = new LmmBodyRequest<>();
        bodyRequest.setRequest(obj);
        return JSON.toJSONString(bodyRequest);
    }
}
