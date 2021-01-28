package com.huoli.trip.supplier.web.difengyun.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.huoli.eagle.eye.core.HuoliTrace;
import com.huoli.trip.common.constant.CentralError;
import com.huoli.trip.common.constant.ConfigConstants;
import com.huoli.trip.common.entity.TripOrder;
import com.huoli.trip.common.entity.TripOrderRefund;
import com.huoli.trip.common.entity.TripPayOrder;
import com.huoli.trip.common.entity.TripRefundNotify;
import com.huoli.trip.common.util.ConfigGetter;
import com.huoli.trip.common.util.HttpUtil;
import com.huoli.trip.common.vo.request.PushOrderStatusReq;
import com.huoli.trip.common.vo.request.central.OrderStatusKafka;
import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.common.vo.response.order.OrderDetailRep;
import com.huoli.trip.supplier.api.DfyOrderService;
import com.huoli.trip.supplier.self.difengyun.DfyOrderDetail;
import com.huoli.trip.supplier.self.difengyun.vo.DfyToursOrderDetail;
import com.huoli.trip.supplier.self.difengyun.vo.push.DfyOrderPushRequest;
import com.huoli.trip.supplier.self.difengyun.vo.push.DfyOrderPushResponse;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyBaseResult;
import com.huoli.trip.supplier.self.yaochufa.vo.BaseOrderRequest;
import com.huoli.trip.supplier.web.config.TraceConfig;
import com.huoli.trip.supplier.web.mapper.TripOrderMapper;
import com.huoli.trip.supplier.web.mapper.TripOrderRefundMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    @Autowired
    private HuoliTrace huoliTrace;

    public DfyBaseResult orderStatusNotice(DfyOrderPushRequest request) {
        String url= ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_NAME_COMMON,"hltrip.centtral")+"/recSupplier/orderStatusNotice";
        try {
            String orderId = request.getData().getOrderId();
            BaseOrderRequest orderDetailReq=new BaseOrderRequest();
            orderDetailReq.setSupplierOrderId(orderId);

            TripOrder tripOrder = tripOrderMapper.getOrderByOutOrderId(orderId);
            if(null==tripOrder)
                return new DfyBaseResult("601","未查询到订单",false);
            List<TripPayOrder> orderPayList = tripOrderMapper.getOrderPayList(tripOrder.getOrderId());
            boolean payed=false;
            for(TripPayOrder payOrder:orderPayList){
                if(payOrder.getStatus()==1){
                    payed=true;
                    break;
                }
            }
            log.info("orderStatusNotice这的payed:"+payed);

            switch (tripOrder.getProductType()){//产品类型:1-酒+,2-门票,3-跟团,4-餐饮,5-门票+
            	case 2:
                    return  handleTicket(tripOrder,orderDetailReq,payed,url);

                case 3:
                    return  handleTours(tripOrder,orderDetailReq,payed,url);

            	default:
                    return new DfyBaseResult("601","未找到对应的订单类型",false);
            }

        } catch (Exception e) {
            log.info("",e);
            return new DfyBaseResult("500","内部通信异常",false);
        }
    }

    private DfyBaseResult handleTours(TripOrder tripOrder, BaseOrderRequest orderDetailReq, boolean payed, String url) {

        try {

            BaseResponse<DfyToursOrderDetail> dfyToursOrderDetail = dfyOrderService.toursOrderDetail(orderDetailReq);
            DfyToursOrderDetail orderDetail = dfyToursOrderDetail.getData();
            if(null==orderDetail)
                return new DfyBaseResult("601","未查询到订单",false);
            //只有当有支付订单并支付成功后 ,订单详情有取消的,才去考虑退款单
            if(payed&&orderDetail.getOrderStatus().equals("已取消")){

            }else{
                PushOrderStatusReq req =new PushOrderStatusReq();
                req.setStrStatus(orderDetail.getOrderStatus());
                req.setPartnerOrderId(tripOrder.getOrderId());
                req.setVochers(genToursVoucher(orderDetail));
                req.setType(4);
                log.info("handleTours中台订单推送传参json:"+req);
                String res = HttpUtil.doPostWithTimeout(url, JSONObject.toJSONString(req), 10000, TraceConfig.traceHeaders(huoliTrace, url));
                log.info("handleTours中台orderStatusNotice返回:"+res);
            }

            return DfyBaseResult.success(null);
        } catch (Exception e) {
            log.info("",e);
            return new DfyBaseResult("500","内部通信异常",false);
        }
    }


    private DfyBaseResult handleTicket(TripOrder tripOrder,BaseOrderRequest orderDetailReq, boolean payed, String url) {

        try {

            BaseResponse<DfyOrderDetail> dfyOrderDetail = dfyOrderService.orderDetail(orderDetailReq);
            DfyOrderDetail orderDetail = dfyOrderDetail.getData();
            if(null==orderDetail)
                return new DfyBaseResult("601","未查询到订单",false);
            //只有当有支付订单并支付成功后 ,订单详情有取消的,才去考虑退款单
            if(payed&&orderDetail.getOrderStatus().equals("已取消")){

            }else{
                PushOrderStatusReq req =new PushOrderStatusReq();
                req.setStrStatus(orderDetail.getOrderStatus());
                req.setPartnerOrderId(tripOrder.getOrderId());
                req.setVochers(genTicketsVoucher(orderDetail));
                req.setType(3);
                log.info("中台订单推送传参json:"+req);
                String res = HttpUtil.doPostWithTimeout(url, JSONObject.toJSONString(req), 10000, TraceConfig.traceHeaders(huoliTrace, url));
                log.info("中台orderStatusNotice返回:"+res);
            }


            return DfyBaseResult.success(null);
        } catch (Exception e) {
            log.info("",e);
            return new DfyBaseResult("500","内部通信异常",false);
        }
    }

    private List<PushOrderStatusReq.Voucher> genTicketsVoucher(DfyOrderDetail orderDetail) {

        if(orderDetail.getOrderStatus().equals("已完成")){
            List<PushOrderStatusReq.Voucher> vochers = new ArrayList<>();
            try {
                if(null!=orderDetail.getOrderInfo().getEnterCertificate()&& CollectionUtils.isNotEmpty(orderDetail.getOrderInfo().getEnterCertificate().getEnterCertificateTypeInfo())){
                    for(DfyOrderDetail.EnterCertificateTypeInfo typeInfo:orderDetail.getOrderInfo().getEnterCertificate().getEnterCertificateTypeInfo()){
                        for(DfyOrderDetail.TicketCertInfo oneInfo:typeInfo.getTicketCertInfos()){

                            switch (oneInfo.getCertType()){//凭证类型   1.纯文本  2.二维码 3.PDF
                                case 1:
                                    for(String entry:oneInfo.getFileUrls()){
                                        PushOrderStatusReq.Voucher oneVoucher=new PushOrderStatusReq.Voucher();
                                        oneVoucher.setVocherNo(entry);
                                        oneVoucher.setType(1);
                                        vochers.add(oneVoucher);
                                    }
                                    break;

                                case 2:
                                    for(String entry:oneInfo.getFileUrls()){
                                        PushOrderStatusReq.Voucher oneVoucher=new PushOrderStatusReq.Voucher();
                                        oneVoucher.setVocherUrl(entry);
                                        oneVoucher.setType(2);
                                        vochers.add(oneVoucher);
                                    }
                                    break;
                                case 3:
                                    for(String entry:oneInfo.getFileUrls()){
                                        PushOrderStatusReq.Voucher oneVoucher=new PushOrderStatusReq.Voucher();
                                        oneVoucher.setVocherUrl(entry);
                                        oneVoucher.setType(3);
                                        vochers.add(oneVoucher);
                                    }
                                    break;
                            }
                        }
                    }
                    return  vochers;
                }
            } catch (Exception e) {
                log.error("genVouchers错",e);
            }
        }
        return  null;
    }


    //看代码里就两类：行程（仅供参考）  和   出团通知
    private List<PushOrderStatusReq.Voucher> genToursVoucher(DfyToursOrderDetail orderDetail) {

        if(orderDetail.getOrderStatus().equals("已完成")){
            List<PushOrderStatusReq.Voucher> vochers = new ArrayList<>();
            try {
                if(CollectionUtils.isNotEmpty(orderDetail.getAttachments())){
                    for(DfyToursOrderDetail.OrderAttachment oneInfo:orderDetail.getAttachments()){
                            //凭证类型   1.纯文本  2.二维码 3.PDF
                        PushOrderStatusReq.Voucher oneVoucher=new PushOrderStatusReq.Voucher();
                        oneVoucher.setVocherUrl(oneInfo.getUrl());
                        oneVoucher.setType(3);
                        vochers.add(oneVoucher);
                    }
                    return  vochers;
                }
            } catch (Exception e) {
                log.error("genToursVouchers错",e);
            }
        }
        return  null;
    }
}
