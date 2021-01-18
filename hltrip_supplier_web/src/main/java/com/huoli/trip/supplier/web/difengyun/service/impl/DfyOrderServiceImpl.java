package com.huoli.trip.supplier.web.difengyun.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huoli.trip.common.constant.CentralError;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.constant.ConfigConstants;
import com.huoli.trip.common.util.ConfigGetter;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.common.util.HttpUtil;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.common.vo.request.RefundNoticeReq;
import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.common.vo.response.order.OrderDetailRep;
import com.huoli.trip.supplier.api.DfyOrderService;
import com.huoli.trip.supplier.feign.client.difengyun.client.IDiFengYunClient;
import com.huoli.trip.supplier.self.difengyun.vo.DfyBookSaleInfo;
import com.huoli.trip.supplier.self.difengyun.vo.DfyScenic;
import com.huoli.trip.supplier.self.difengyun.vo.DfyScenicDetail;
import com.huoli.trip.supplier.self.difengyun.vo.request.*;
import com.huoli.trip.supplier.self.difengyun.DfyOrderDetail;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyBaseRequest;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyOrderDetailRequest;
import com.huoli.trip.supplier.self.difengyun.vo.response.*;
import com.huoli.trip.supplier.self.hllx.vo.HllxBaseResult;
import com.huoli.trip.supplier.self.hllx.vo.HllxBookCheckRes;
import com.huoli.trip.supplier.self.hllx.vo.HllxBookSaleInfo;
import com.huoli.trip.supplier.self.yaochufa.vo.BaseOrderRequest;
import com.huoli.trip.supplier.web.dao.PriceDao;
import com.huoli.trip.supplier.web.mapper.TripOrderMapper;
import com.huoli.trip.supplier.web.mapper.TripOrderRefundMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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

    @Autowired
    private PriceDao priceDao;

    @Autowired
    TripOrderRefundMapper tripOrderRefundMapper;
    @Autowired
    TripOrderMapper tripOrderMapper;

    public BaseResponse<DfyOrderDetail> orderDetail(BaseOrderRequest request){

        DfyOrderDetailRequest dfyOrderDetailBody=new DfyOrderDetailRequest();
        DfyBaseRequest<DfyOrderDetailRequest> dfyOrderDetailReq = new DfyBaseRequest<>(dfyOrderDetailBody);
        dfyOrderDetailBody.setOrderId(request.getSupplierOrderId());
        try {
            String acctid = ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_DIFENGYUN,"difengyun.api.acctId");
            dfyOrderDetailBody.setAcctId(acctid);
            DfyBaseResult<DfyOrderDetail> baseResult = diFengYunClient.orderDetail(dfyOrderDetailReq);
            log.info("dfy订单详情的返回:"+JSONObject.toJSONString(baseResult)+",请求参数:"+ JSON.toJSONString(dfyOrderDetailReq));

            DfyOrderDetail detail = baseResult.getData();
            if(detail!=null&&detail.getOrderInfo()!=null){
                detail.setOrderId(detail.getOrderInfo().getOrderId());
                if(StringUtils.equals(detail.getOrderStatus(),"已完成")){
                    switch (detail.getOrderInfo().getStatusDesc()){
                        case "取消订单核损中":
                        case "取消订单确认中":
                        case "核损已反馈":
                        case "取取消订单核损已反馈":
                            detail.setOrderStatus("申请退款中");
                            break;

                    	default:
                    		break;
                    }

                }
                if(StringUtils.equals(detail.getOrderStatus(),"已取消")){
                    TripOrder tripOrder = tripOrderMapper.getOrderByOutOrderId(detail.getOrderId());
                    List<TripPayOrder> orderPayList = tripOrderMapper.getOrderPayList(tripOrder.getOrderId());
                    boolean payed=false;
                    for(TripPayOrder payOrder:orderPayList){
                        if(payOrder.getStatus()==1){
                            payed=true;
                            break;
                        }
                    }

                    log.info("已取消订单详情这里的payed:"+payed);

                    if(payed){
                        detail.setOrderStatus("申请退款中");

                        TripRefundNotify dbRefundNotify = tripOrderRefundMapper.getRefundNotifyByOrderId(tripOrder.getOrderId());
                        if(dbRefundNotify!=null){
                            if(dbRefundNotify.getStatus()==1){
                                detail.setOrderStatus("已退款");
                            }else{//这里去实时查一下账单
                                try {
                                    processNotify(dbRefundNotify);
                                    if(dbRefundNotify.getStatus()==1){
                                        detail.setOrderStatus("已退款");
                                    }
                                    Thread.sleep(100);
                                }  catch (Exception e) {
                                    log.info("裡处理退款通知失败了，id={}", dbRefundNotify.getId(), e);
                                }

                            }

                        }else{
                            TripOrderRefund refundOrder = tripOrderRefundMapper.getRefundingOrderByOrderId(tripOrder.getOrderId());
                            TripRefundNotify notify = new TripRefundNotify();
                            if (refundOrder == null) {
                                log.info("这未找到待处理的退款单" + tripOrder.getOrderId());
                                notify.setOrderId(refundOrder.getOrderId());
                                notify.setChannel("dfy");
                                notify.setStatus(0);

                            } else {
                                TripRefundNotify refundNotify = tripOrderRefundMapper.getRefundNotify(refundOrder.getOrderId(), refundOrder.getId());
                                notify.setOrderId(refundOrder.getOrderId());
                                notify.setRefundId(refundOrder.getId());
                                notify.setChannel("dfy");
                                notify.setStatus(0);
                            }
                            tripOrderRefundMapper.saveTripRefundNotify(notify);
                        }

                    }


                }
            }else{
                if(!baseResult.isSuccess()){
                    return BaseResponse.fail(CentralError.ERROR_NO_ORDER);
                }
            }
            return BaseResponse.success(detail);
        } catch (Exception e) {
        	log.error("信息{}",e);
            return BaseResponse.fail(CentralError.ERROR_NO_ORDER);
        }


    }

    @Override
    public BaseResponse<OrderDetailRep> getVochers(BaseOrderRequest request) {
        DfyOrderDetailRequest dfyOrderDetailBody=new DfyOrderDetailRequest();
        DfyBaseRequest<DfyOrderDetailRequest> dfyOrderDetailReq = new DfyBaseRequest<>(dfyOrderDetailBody);
        dfyOrderDetailBody.setOrderId(request.getSupplierOrderId());
        try {
            DfyBaseResult<DfyOrderDetail> baseResult = diFengYunClient.orderDetail(dfyOrderDetailReq);


            log.info("getVochersdfy订单详情的返回:"+JSONObject.toJSONString(baseResult)+",请求参数:"+ JSON.toJSONString(dfyOrderDetailReq));

            DfyOrderDetail detail = baseResult.getData();
            if(detail!=null&&detail.getOrderInfo()!=null){
                detail.setOrderId(detail.getOrderInfo().getOrderId());
            }
            return BaseResponse.success(detail);
        } catch (Exception e) {
            log.error("信息{}",e);
            return BaseResponse.fail(CentralError.ERROR_UNKNOWN);
        }
    }

    @Override
    public DfyBaseResult<DfyBillResponse> queryBill(DfyBillQueryDataReq billQueryDataReq) {
        try {
            DfyBaseRequest dfyBaseRequest = new DfyBaseRequest();
            String acctid = ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_DIFENGYUN,"difengyun.api.acctId");
            billQueryDataReq.setAcctId(acctid);
            dfyBaseRequest.setData(billQueryDataReq);
            String apipublicKey = ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_DIFENGYUN,"difengyun.api.public.key");
            String apipublicSecretKey = ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_DIFENGYUN,"difengyun.api.public.secret.key");
            dfyBaseRequest.setApiKey(apipublicKey);
            dfyBaseRequest.setSecretKey(apipublicSecretKey);
            DfyBaseResult<DfyBillResponse> dfyBillResponse = diFengYunClient.queryBill(dfyBaseRequest);
            log.info("dfyqueryBill的返回:"+JSONObject.toJSONString(dfyBillResponse)+",请求参数:"+ JSON.toJSONString(dfyBaseRequest)+","+apipublicKey);

            return dfyBillResponse;
        } catch (Exception e) {
            log.error("信息{}",e);
            return null;
        }
    }

    public DfyBaseResult<DfyBookCheckResponse> getCheckInfos(DfyBookCheckRequest bookCheckReq) {

        PricePO pricePO = priceDao.getByProductCode(bookCheckReq.getProductId());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        if (pricePO != null) {
            List<PriceInfoPO> priceInfos = pricePO.getPriceInfos();
            if (ListUtils.isNotEmpty(priceInfos)) {
                Optional<PriceInfoPO> optionalT = priceInfos.stream().filter(priceInfoPO -> {
                    Date saleDate = priceInfoPO.getSaleDate();
                    String saleDates = formatter.format(saleDate);
                    return StringUtils.equals(bookCheckReq.getBeginDate(), saleDates);
                }).findFirst();
                if (optionalT.isPresent()) {
                    PriceInfoPO priceInfoPO = optionalT.get();
                    log.info("DFY checkinfo PricePO is:{}", JSON.toJSONString(priceInfoPO));
                    Integer stock = priceInfoPO.getStock();
                    if (stock != null && stock > 0) {
                        DfyBookCheckResponse dfyBookCheckResponse = new DfyBookCheckResponse();
                        dfyBookCheckResponse.setProductId(bookCheckReq.getProductId());
                        List<DfyBookSaleInfo> saleInfos = new ArrayList<>();
                        dfyBookCheckResponse.setSaleInfos(saleInfos);
                        DfyBookSaleInfo dfyBookSaleInfo = new DfyBookSaleInfo();
                        dfyBookSaleInfo.setDate(priceInfoPO.getSaleDate());
                        dfyBookSaleInfo.setPrice(priceInfoPO.getSalePrice());
                        //llxBookSaleInfo.setPriceType(priceInfoPO.getPriceType());
                        dfyBookSaleInfo.setTotalStock(priceInfoPO.getStock());
                        saleInfos.add(dfyBookSaleInfo);
                        return new DfyBaseResult(true, 200, dfyBookCheckResponse);
                    }
                }
            }
        }
        return new DfyBaseResult(true, 200, null);
    }

    @Override
    public DfyBaseResult payOrder(DfyPayOrderRequest payOrderRequest) {
        DfyBaseRequest dfyBaseRequest = new DfyBaseRequest();
        DfySubmitOrderRequest request = new DfySubmitOrderRequest();
        request.setOrderId(payOrderRequest.getChannelOrderId());
        request.setPay(payOrderRequest.getPrice());
        dfyBaseRequest.setData(request);
        //需要支付方式 支付金额
        request.setPayType("1");
        String acctid = ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_DIFENGYUN,"difengyun.api.acctId");
        request.setAcctId(acctid);
        return diFengYunClient.submitOrder(dfyBaseRequest);
    }

    @Override
    public DfyBaseResult<DfyCreateOrderResponse> createOrder(DfyCreateOrderRequest createOrderReq) {
        DfyBaseRequest dfyBaseRequest = new DfyBaseRequest();
        String acctid = ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_DIFENGYUN,"difengyun.api.acctId");
        createOrderReq.setAcctId(acctid);
        createOrderReq.setTraceId(null);
        dfyBaseRequest.setData(createOrderReq);
        return diFengYunClient.createOrder(dfyBaseRequest);
    }

    @Override
    public DfyBaseResult cancelOrder(DfyCancelOrderRequest cancelOrderReq) {
        DfyBaseRequest dfyBaseRequest = new DfyBaseRequest();
        cancelOrderReq.setTraceId(null);
        dfyBaseRequest.setData(cancelOrderReq);
        return diFengYunClient.cancelOrder(dfyBaseRequest);
    }

    @Override
    public DfyBaseResult<DfyRefundTicketResponse> rufundTicket(DfyRefundTicketRequest request) {
        DfyBaseRequest dfyBaseRequest = new DfyBaseRequest();
        request.setTraceId(null);
        dfyBaseRequest.setData(request);
        return diFengYunClient.refundTicket(dfyBaseRequest);
    }

    @Override
    public void processNotify(TripRefundNotify item) {

        DfyBillQueryDataReq billQueryDataReq=new DfyBillQueryDataReq();
        billQueryDataReq.setAccType(1);
        billQueryDataReq.setBillType(4);
        billQueryDataReq.setStart(0);
        billQueryDataReq.setLimit(50);
        billQueryDataReq.setStatus(1);
        Date createDate = DateTimeUtil.parse(item.getCreateTime(), DateTimeUtil.YYYYMMDDHHmmss);

        billQueryDataReq.setBeginTime(DateTimeUtil.format(DateTimeUtil.addDay(createDate,-1),DateTimeUtil.YYYYMMDDHHmmss));
        billQueryDataReq.setEndTime(DateTimeUtil.format(DateTimeUtil.addDay(createDate,10),DateTimeUtil.YYYYMMDDHHmmss));

        DfyBaseResult<DfyBillResponse> dfyBillResponseDfyBaseResult = queryBill(billQueryDataReq);
        if(dfyBillResponseDfyBaseResult.getData()!=null && CollectionUtils.isNotEmpty(dfyBillResponseDfyBaseResult.getData().getRows())){
            TripOrder tripOrder = tripOrderMapper.getChannelByOrderId(item.getOrderId());
            log.info("processNotify这里的rows:"+ JSONObject.toJSONString(dfyBillResponseDfyBaseResult.getData().getRows()));
            for(DfyBillResponse.QueryBillsDto bill :dfyBillResponseDfyBaseResult.getData().getRows()){

                if(bill.getBillType()!=4 )
                    break;
                if(!StringUtils.equals(tripOrder.getOutOrderId(),bill.getBizOrderId()))//单号不一样则跳过
                    continue;

                String url= ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_NAME_COMMON,"hltrip.centtral")+"/recSupplier/refundNotice";
                RefundNoticeReq req=new RefundNoticeReq();
                req.setPartnerOrderId(item.getOrderId());
                req.setRefundFrom(2);
                req.setRefundPrice(new BigDecimal(bill.getAmount()));
                req.setResponseTime(bill.getTime());
                req.setSource("dfy");
                BigDecimal refundCharge=tripOrder.getOutPayPrice().subtract(req.getRefundPrice());
                req.setRefundCharge(refundCharge);

                switch (bill.getStatus()) {//账单处理结果，1处理完成-1处理失败3处理
                    case 1:
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
                            req.setRefundCharge(refundOrder.getRefundCharge());
                        }else{
                            log.info("无退款单子但是渠道退款了:"+item);
                        }

                        req.setRefundStatus(1);

                        log.info("doRefund请求的地址:"+url+",参数:"+ JSONObject.toJSONString(req)+",refundStatus:"+item.getOrderId());
                        String res = HttpUtil.doPostWithTimeout(url, JSONObject.toJSONString(req), 10000, null);
                        log.info("中台refundNotice返回:"+res);


                        break;

                    case -1:

                        item.setStatus(2);
                        item.setRefundStatus(bill.getStatus());
                        item.setRefundTime(bill.getTime());
                        item.setRefundMoney(bill.getAmount());
                        item.setBillInfo(JSONObject.toJSONString(bill));
                        tripOrderRefundMapper.updateRefundNotify(item);

                        req.setRefundStatus(-1);
                        log.info("doRefund请求的地址:"+url+",参数:"+ JSONObject.toJSONString(req)+",orderId:"+item.getOrderId());
                        String res2 = HttpUtil.doPostWithTimeout(url, JSONObject.toJSONString(req), 10000, null);
                        log.info("中台refundNotice返回:"+res2);
                        break;
                    case 3:

                        break;
                    default:

                        break;
                }


            }
        }

    }

    @Override
    public DfyBaseResult<DfyOrderStatusResponse> orderStatus(DfyOrderStatusRequest request) {
        String acctid = ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_DIFENGYUN,"difengyun.api.acctId");
        DfyBaseRequest dfyBaseRequest = new DfyBaseRequest();
        dfyBaseRequest.setData(request);
        request.setAcctId(acctid);
        return diFengYunClient.orderStatus(dfyBaseRequest);
    }
}
