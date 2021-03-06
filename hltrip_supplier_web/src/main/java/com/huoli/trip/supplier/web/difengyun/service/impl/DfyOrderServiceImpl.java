package com.huoli.trip.supplier.web.difengyun.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huoli.eagle.eye.core.HuoliTrace;
import com.huoli.trip.common.constant.CentralError;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.constant.ConfigConstants;
import com.huoli.trip.common.entity.mpo.groupTour.GroupTourPrice;
import com.huoli.trip.common.entity.mpo.groupTour.GroupTourProductSetMealMPO;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotProductPriceMPO;
import com.huoli.trip.common.util.ConfigGetter;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.common.util.HttpUtil;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.common.vo.request.PushOrderStatusReq;
import com.huoli.trip.common.vo.request.RefundNoticeReq;
import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.common.vo.response.order.OrderDetailRep;
import com.huoli.trip.supplier.api.DfyOrderService;
import com.huoli.trip.supplier.feign.client.difengyun.client.IDiFengYunClient;
import com.huoli.trip.supplier.self.difengyun.vo.*;
import com.huoli.trip.supplier.self.difengyun.vo.request.*;
import com.huoli.trip.supplier.self.difengyun.DfyOrderDetail;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyBaseRequest;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyOrderDetailRequest;
import com.huoli.trip.supplier.self.difengyun.vo.response.*;
import com.huoli.trip.supplier.self.yaochufa.vo.BaseOrderRequest;
import com.huoli.trip.supplier.web.config.TraceConfig;
import com.huoli.trip.supplier.web.dao.GroupTourProductSetMealDao;
import com.huoli.trip.supplier.web.dao.HotelScenicProductSetMealDao;
import com.huoli.trip.supplier.web.dao.PriceDao;
import com.huoli.trip.supplier.web.dao.ScenicSpotProductPriceDao;
import com.huoli.trip.supplier.web.mapper.TripOrderMapper;
import com.huoli.trip.supplier.web.mapper.TripOrderRefundMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author :zhouwenbin
 * @time ?? :2020/12/10
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

    @Autowired
    private HuoliTrace huoliTrace;

    @Autowired
    private GroupTourProductSetMealDao groupTourProductSetMealDao;

    @Autowired
    private ScenicSpotProductPriceDao scenicSpotProductPriceDao;

    public BaseResponse<DfyOrderDetail> orderDetail(BaseOrderRequest request){

        DfyOrderDetailRequest dfyOrderDetailBody=new DfyOrderDetailRequest();
        DfyBaseRequest<DfyOrderDetailRequest> dfyOrderDetailReq = new DfyBaseRequest<>(dfyOrderDetailBody);
        dfyOrderDetailBody.setOrderId(request.getSupplierOrderId());
        try {
            String acctid = ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_DIFENGYUN,"difengyun.api.acctId");
            dfyOrderDetailBody.setAcctId(acctid);
            DfyBaseResult<DfyOrderDetail> baseResult = diFengYunClient.orderDetail(dfyOrderDetailReq);
            log.info("dfy?????????????????????:"+JSONObject.toJSONString(baseResult)+",????????????:"+ JSON.toJSONString(dfyOrderDetailReq));

            DfyOrderDetail detail = baseResult.getData();
            if(detail!=null&&detail.getOrderInfo()!=null){
                detail.setOrderId(detail.getOrderInfo().getOrderId());
                TripOrder tripOrder = tripOrderMapper.getOrderByOutOrderId(detail.getOrderId());
                if(StringUtils.equals(detail.getOrderStatus(),"?????????")){
                    switch (detail.getOrderInfo().getStatusDesc()){
                        case "?????????????????????":
                        case "?????????????????????":
                        case "???????????????":
                        case "???????????????????????????":
                            detail.setOrderStatus("???????????????");
                            break;
                        case "?????????????????????":
                            detail.setOrderStatus("?????????");
                            break;
                        case "?????????":
                        case "?????????":
                        case "?????????":


                            try {
                                TripOrderRefund refundOrder = tripOrderRefundMapper.getRefundingOrderByOrderId(tripOrder.getOrderId());
                                if(refundOrder!=null && refundOrder.getChannelRefundStatus()==0){//???????????????
                                    detail.setOrderStatus("???????????????");
                                    log.info("????????????????????????:"+tripOrder.getOrderId());
                                    TripRefundNotify dbRefundNotify = tripOrderRefundMapper.getRefundNotifyByOrderId(tripOrder.getOrderId());
                                    if(dbRefundNotify!=null){
                                        dbRefundNotify.setStatus(2);
                                        dbRefundNotify.setRefundStatus(-1);
                                        tripOrderRefundMapper.updateRefundNotify(dbRefundNotify);
                                    }

                                    RefundNoticeReq req=new RefundNoticeReq();
                                    req.setPartnerOrderId(tripOrder.getOrderId());
                                    req.setRefundFrom(2);
                                    req.setRefundPrice(new BigDecimal(0));
                                    req.setResponseTime(DateTimeUtil.formatFullDate(new Date()));
                                    req.setSource("dfy");
                                    req.setRefundStatus(-1);
                                    String refundUrl= ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_NAME_COMMON,"hltrip.centtral")+"/recSupplier/refundNotice";
                                    log.info("????????????doRefund???????????????:"+refundUrl+",??????:"+ JSONObject.toJSONString(req)+",orderId:"+tripOrder.getOrderId());
                                    String res2 = HttpUtil.doPostWithTimeout(refundUrl, JSONObject.toJSONString(req), 10000, TraceConfig.traceHeaders(huoliTrace, refundUrl));
                                    log.info("??????refundNotice??????:"+res2);
                                }
                            } catch (Exception e) {
                            	log.error("??????{}",e);
                            }

                            break;

                    	default:
                    		break;
                    }

                }
//                log.info(tripOrder.getBeginDate()+",?????????:"+DateTimeUtil.formatDate(new Date())+","+DateTimeUtil.formatDate(new Date()).compareTo(tripOrder.getBeginDate()));
                //???????????????
                if(tripOrder!=null&&StringUtils.isNotBlank(tripOrder.getBeginDate())&
                        DateTimeUtil.formatDate(new Date()).compareTo(tripOrder.getBeginDate())>=0){
                    DfyBaseResult<DfyVerifyOrderResponse> verifyOrderRes = verifyOrder(dfyOrderDetailReq);
                    if(verifyOrderRes!=null&& verifyOrderRes.isSuccess()){
                        DfyVerifyOrderResponse verifyData = verifyOrderRes.getData();
                        log.info("????????????:"+verifyData.getTotalCount()+","+verifyData.getUsedCount());
                        if(verifyData.getTotalCount()>0 ){
                            if(verifyData.getTotalCount()== verifyData.getUsedCount())
                                   detail.setOrderStatus("?????????");
                            if(verifyData.getTotalCount()> verifyData.getUsedCount())
                                detail.setOrderStatus("?????????");
                        }
                    }
                }
                if(StringUtils.equals(detail.getOrderStatus(),"?????????")){
                    List<TripPayOrder> orderPayList = tripOrderMapper.getOrderPayList(tripOrder.getOrderId());
                    boolean payed=false;
                    for(TripPayOrder payOrder:orderPayList){
                        if(payOrder.getStatus()==1){
                            payed=true;
                            break;
                        }
                    }

                    log.info("??????????????????????????????payed:"+payed);

                    if(payed){
                        detail.setOrderStatus("???????????????");

                        TripRefundNotify dbRefundNotify = tripOrderRefundMapper.getRefundNotifyByOrderId(tripOrder.getOrderId());
                        if(dbRefundNotify!=null){
                            if(dbRefundNotify.getStatus()==1){
                                detail.setOrderStatus("?????????");
                            }else{//??????????????????????????????
                                try {
                                    processNotify(dbRefundNotify);
                                    if(dbRefundNotify.getStatus()==1){
                                        detail.setOrderStatus("?????????");
                                    }
                                    Thread.sleep(100);
                                }  catch (Exception e) {
                                    log.info("?????????????????????????????????id={}", dbRefundNotify.getId(), e);
                                }

                            }

                        }else{
                            TripOrderRefund refundOrder = tripOrderRefundMapper.getRefundingOrderByOrderId(tripOrder.getOrderId());
                            TripRefundNotify notify = new TripRefundNotify();
                            if (refundOrder == null) {
                                log.info("?????????????????????????????????" + tripOrder.getOrderId());
                                notify.setOrderId(tripOrder.getOrderId());
                                notify.setChannel("dfy");
                                notify.setStatus(0);

                            } else {
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
        	log.error("??????{}",e);
            return BaseResponse.fail(CentralError.ERROR_NO_ORDER);
        }


    }


    public BaseResponse<DfyToursOrderDetail> toursOrderDetail(BaseOrderRequest request){

        try {
            DfyOrderDetailRequest dfyOrderDetailBody=new DfyOrderDetailRequest();
            dfyOrderDetailBody.setOrderId(request.getSupplierOrderId());
            String tours_key = ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_DIFENGYUN,"difengyun.api.tours.key");
            String tours_secret = ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_DIFENGYUN,"difengyun.api.tours.secret.key");
            DfyBaseRequest dfyBaseRequest = new DfyBaseRequest();
            dfyBaseRequest.setApiKey(tours_key);
            dfyBaseRequest.setSecretKey(tours_secret);
            dfyBaseRequest.setData(dfyOrderDetailBody);

            String acctid = ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_DIFENGYUN,"difengyun.api.acctId");
            dfyOrderDetailBody.setAcctId(acctid);
            DfyBaseResult<DfyToursOrderDetail> baseResult = diFengYunClient.toursOrderDetail(dfyBaseRequest);
            log.info("dfy??????????????????????????????:"+JSONObject.toJSONString(baseResult)+",????????????:"+ JSON.toJSONString(dfyBaseRequest));

            DfyToursOrderDetail detail = baseResult.getData();
            if(detail!=null&&detail.getOrderInfo()!=null){
                detail.setOrderId(detail.getOrderInfo().getOrderId());

                switch (detail.getOrderInfo().getStatus()) {
                    case "?????????":
                    case "????????????":
                        detail.setOrderStatus(detail.getOrderInfo().getStatus());
                        break;
                }

                if(StringUtils.equals(detail.getOrderStatus(),"?????????") || StringUtils.equals(detail.getOrderStatus(),"?????????") ){
                    switch (detail.getOrderInfo().getStatus()){
                        case "?????????":
                        case "?????????????????????":
                        case "?????????????????????":
                        case "???????????????":
                        case "???????????????????????????":
                            detail.setOrderStatus("???????????????");
                            break;

                        default://??????????????????,?????????????????????
                            try {
//                                TripOrder tripOrder = tripOrderMapper.getOrderByOutOrderId(detail.getOrderId());
//                                TripOrderRefund refundOrder = tripOrderRefundMapper.getRefundingOrderByOrderId(tripOrder.getOrderId());
//                                if(refundOrder!=null && refundOrder.getChannelRefundStatus()==0){//???????????????
//                                    detail.setOrderStatus("???????????????");
//                                    log.info("toursOrderDetail????????????????????????:"+tripOrder.getOrderId());
//                                    TripRefundNotify dbRefundNotify = tripOrderRefundMapper.getRefundNotifyByOrderId(tripOrder.getOrderId());
//                                    if(dbRefundNotify!=null){
//                                        dbRefundNotify.setStatus(2);
//                                        dbRefundNotify.setRefundStatus(-1);
//                                        tripOrderRefundMapper.updateRefundNotify(dbRefundNotify);
//                                    }
//
//                                    RefundNoticeReq req=new RefundNoticeReq();
//                                    req.setPartnerOrderId(tripOrder.getOrderId());
//                                    req.setRefundFrom(2);
//                                    req.setRefundPrice(new BigDecimal(0));
//                                    req.setResponseTime(DateTimeUtil.formatFullDate(new Date()));
//                                    req.setSource("dfy");
//                                    req.setRefundStatus(-1);
//                                    String refundUrl= ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_NAME_COMMON,"hltrip.centtral")+"/recSupplier/refundNotice";
//                                    log.info("????????????doRefund???????????????:"+refundUrl+",??????:"+ JSONObject.toJSONString(req)+",orderId:"+tripOrder.getOrderId());
//                                    String res2 = HttpUtil.doPostWithTimeout(refundUrl, JSONObject.toJSONString(req), 10000, TraceConfig.traceHeaders(huoliTrace, refundUrl));
//                                    log.info("??????refundNotice??????:"+res2);
//                                }
                            } catch (Exception e) {
                                log.error("??????{}",e);
                            }
                            break;
                    }

                }
                if(StringUtils.equals(detail.getOrderStatus(),"?????????")){
                    TripOrder tripOrder = tripOrderMapper.getOrderByOutOrderId(detail.getOrderId());
                    List<TripPayOrder> orderPayList = tripOrderMapper.getOrderPayList(tripOrder.getOrderId());
                    boolean payed=false;
                    for(TripPayOrder payOrder:orderPayList){
                        if(payOrder.getStatus()==1){
                            payed=true;
                            break;
                        }
                    }

                    log.info("???????????????????????????????????????payed:"+payed);

                    if(payed){
                        detail.setOrderStatus("???????????????");

                        TripRefundNotify dbRefundNotify = tripOrderRefundMapper.getRefundNotifyByOrderId(tripOrder.getOrderId());
                        if(dbRefundNotify!=null){
                            if(dbRefundNotify.getStatus()==1){
                                detail.setOrderStatus("?????????");
                            }else{//??????????????????????????????
                                try {
                                    processNotify(dbRefundNotify);
                                    if(dbRefundNotify.getStatus()==1){
                                        detail.setOrderStatus("?????????");
                                    }
                                    Thread.sleep(100);
                                }  catch (Exception e) {
                                    log.info("?????????????????????????????????id={}", dbRefundNotify.getId(), e);
                                }

                            }

                        }else{
                            TripOrderRefund refundOrder = tripOrderRefundMapper.getRefundingOrderByOrderId(tripOrder.getOrderId());
                            TripRefundNotify notify = new TripRefundNotify();
                            if (refundOrder == null) {
                                log.info("?????????????????????????????????" + tripOrder.getOrderId());
                                notify.setOrderId(tripOrder.getOrderId());
                                notify.setChannel("dfy");
                                notify.setStatus(0);

                            } else {
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
            log.error("??????{}",e);
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


            log.info("getVochersdfy?????????????????????:"+JSONObject.toJSONString(baseResult)+",????????????:"+ JSON.toJSONString(dfyOrderDetailReq));

            DfyOrderDetail detail = baseResult.getData();
            if(detail!=null&&detail.getOrderInfo()!=null){
                detail.setOrderId(detail.getOrderInfo().getOrderId());
            }
            return BaseResponse.success(detail);
        } catch (Exception e) {
            log.error("??????{}",e);
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
            log.info("dfyqueryBill?????????:"+JSONObject.toJSONString(dfyBillResponse)+",????????????:"+ JSON.toJSONString(dfyBaseRequest)+","+apipublicKey);

            return dfyBillResponse;
        } catch (Exception e) {
            log.error("??????{}",e);
            return null;
        }
    }

    public DfyBaseResult<DfyBookCheckResponse> getCheckInfos(DfyBookCheckRequest bookCheckReq) {
        log.info("dfy checkinfo req is:{}", JSON.toJSONString(bookCheckReq));
        String category = bookCheckReq.getCategory();
        if(StringUtils.isNotBlank(category)){
            DfyBookCheckResponse dfyBookCheckResponse = null;
            switch (category){
                case "d_ss_ticket":
                    ScenicSpotProductPriceMPO priceMPO = scenicSpotProductPriceDao.getPriceByPackageId(bookCheckReq.getPackageId());
                    if(priceMPO != null && priceMPO.getStock() >= bookCheckReq.getAdtNum()){
                        dfyBookCheckResponse = new DfyBookCheckResponse();
                        dfyBookCheckResponse.setProductId(bookCheckReq.getProductId());
                        dfyBookCheckResponse.setPackageId(bookCheckReq.getPackageId());
                        List<DfyBookSaleInfo> saleInfos = new ArrayList<>();
                        dfyBookCheckResponse.setSaleInfos(saleInfos);
                        DfyBookSaleInfo dfyBookSaleInfo = new DfyBookSaleInfo();
                        dfyBookSaleInfo.setDate(DateTimeUtil.parseDate(priceMPO.getStartDate()));
                        dfyBookSaleInfo.setPrice(priceMPO.getSellPrice());
                        dfyBookSaleInfo.setTotalStock(priceMPO.getStock());
                        saleInfos.add(dfyBookSaleInfo);
                    }
                    break;
                case "group_tour":
                    GroupTourProductSetMealMPO groupTourProductSetMealMPO = groupTourProductSetMealDao.getSetMealByPackageId(bookCheckReq.getPackageId());
                    List<GroupTourPrice> groupTourPrices = groupTourProductSetMealMPO.getGroupTourPrices();
                    if(CollectionUtils.isNotEmpty(groupTourPrices)){
                        groupTourPrices = groupTourPrices.stream().filter(a -> StringUtils.equals(a.getDate(), bookCheckReq.getBeginDate())).collect(Collectors.toList());
                    }
                    if (CollectionUtils.isNotEmpty(groupTourPrices)) {
                        GroupTourPrice groupTourPrice = groupTourPrices.get(0);
                        if(groupTourPrice.getAdtStock() >= bookCheckReq.getAdtNum() && groupTourPrice.getChdStock() >= bookCheckReq.getChdNum()){
                            dfyBookCheckResponse = new DfyBookCheckResponse();
                            dfyBookCheckResponse.setProductId(bookCheckReq.getProductId());
                            dfyBookCheckResponse.setPackageId(bookCheckReq.getPackageId());
                            List<DfyBookSaleInfo> saleInfos = new ArrayList<>();
                            dfyBookCheckResponse.setSaleInfos(saleInfos);
                            DfyBookSaleInfo dfyBookSaleInfo = new DfyBookSaleInfo();
                            dfyBookSaleInfo.setDate(DateTimeUtil.parseDate(groupTourPrice.getDate()));
                            dfyBookSaleInfo.setPrice(groupTourPrice.getAdtPrice());
                            dfyBookSaleInfo.setTotalStock(groupTourPrice.getAdtStock());
                            saleInfos.add(dfyBookSaleInfo);
                        }
                    }
                    break;
            }
            return new DfyBaseResult(true, 200, dfyBookCheckResponse);
        } else {
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
    }

    @Override
    public DfyBaseResult payOrder(DfyPayOrderRequest payOrderRequest) {
        DfyBaseRequest dfyBaseRequest = new DfyBaseRequest();
        DfySubmitOrderRequest request = new DfySubmitOrderRequest();
        request.setOrderId(payOrderRequest.getChannelOrderId());
        request.setPay(payOrderRequest.getPrice());
        dfyBaseRequest.setData(request);
        //?????????????????? ????????????
        request.setPayType("1");
        String acctid = ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_DIFENGYUN,"difengyun.api.acctId");
        request.setAcctId(acctid);
        return diFengYunClient.submitOrder(dfyBaseRequest);
    }

    @Override
    public DfyBaseResult<DfyCreateOrderResponse> createOrder(DfyCreateOrderRequest createOrderReq) {
        DfyBaseRequest dfyBaseRequest = new DfyBaseRequest();
        String acctid = ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_DIFENGYUN,"difengyun.api.acctId");
        String tel = ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_DIFENGYUN,"difengyun.content.phone");
        createOrderReq.setAcctId(acctid);
        createOrderReq.setTraceId(null);
       /* Contact contact = createOrderReq.getContact();
        if(contact != null){
            final String contactTel = contact.getContactTel();
            if(StringUtils.isNotEmpty(contactTel) && StringUtils.isNotEmpty(tel)){
                contact.setContactTel(tel);
            }
        }*/
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
    public void processNotifyTicket() {
        List<TripRefundNotify> pendingNotifys = tripOrderRefundMapper.getRefundNotifyByChannel(Constants.SUPPLIER_CODE_DFY);
        if(ListUtils.isEmpty(pendingNotifys)){
            log.info("????????????????????????????????????????????????");
            return;
        }
        pendingNotifys.forEach(item -> {
            try {
                processNotify(item);
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.error("????????????????????????", e);
            } catch (Exception e) {
                log.error("??????????????????????????????id={}", item.getId(), e);
            }
        });
    }

    @Override
    public void processNotifyTour(){
        List<TripRefundNotify> pendingNotifys = tripOrderRefundMapper.getRefundNotifyByChannel(Constants.SUPPLIER_CODE_DFY_TOURS);
        if(ListUtils.isEmpty(pendingNotifys)){
            log.info("???????????????????????????????????????????????????");
            return;
        }
        pendingNotifys.forEach(item -> {
            try {
                processNotify(item);
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.error("????????????????????????", e);
            } catch (Exception e) {
                log.error("??????????????????????????????id={}", item.getId(), e);
            }
        });
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
        boolean findFlag=false;//???????????????????????????????????????????????????.
        if(dfyBillResponseDfyBaseResult.getData()!=null && CollectionUtils.isNotEmpty(dfyBillResponseDfyBaseResult.getData().getRows())){
            TripOrder tripOrder = tripOrderMapper.getChannelByOrderId(item.getOrderId());
            log.info("processNotify?????????:"+JSONObject.toJSONString(tripOrder));
            log.info("processNotify?????????rows:"+ JSONObject.toJSONString(dfyBillResponseDfyBaseResult.getData().getRows()));
            for(DfyBillResponse.QueryBillsDto bill :dfyBillResponseDfyBaseResult.getData().getRows()){

                if(bill.getBillType()!=4 )
                    break;
                if(!StringUtils.equals(tripOrder.getOutOrderId(),bill.getBizOrderId())) {//????????????????????????{
                    continue;
                }
                findFlag=true;

                String url= ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_NAME_COMMON,"hltrip.centtral")+"/recSupplier/refundNotice";
                RefundNoticeReq req=new RefundNoticeReq();
                req.setPartnerOrderId(item.getOrderId());
                req.setRefundFrom(2);
                req.setRefundPrice(new BigDecimal(bill.getAmount()));
                req.setResponseTime(bill.getTime());
                req.setSource("dfy");
                BigDecimal refundCharge=tripOrder.getOutPayPrice().subtract(req.getRefundPrice());
                req.setRefundCharge(refundCharge);

                switch (bill.getStatus()) {//?????????????????????1????????????-1????????????3??????
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
                        }else{
                            log.info("????????????????????????????????????:"+item);
                        }

                        req.setRefundStatus(1);

                        log.info("doRefund???????????????:"+url+",??????:"+ JSONObject.toJSONString(req)+",refundStatus:"+item.getOrderId());
                        String res = HttpUtil.doPostWithTimeout(url, JSONObject.toJSONString(req), 10000, TraceConfig.traceHeaders(huoliTrace, url));
                        log.info("??????refundNotice??????:"+res);

                        //??????????????????????????????
                        PushOrderStatusReq statusReq =new PushOrderStatusReq();
                        statusReq.setStrStatus("?????????");
                        statusReq.setPartnerOrderId(tripOrder.getOrderId());
                        statusReq.setType(3);

                        log.info("processNotify????????????????????????json:"+req);
                        String statusUrl=ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_NAME_COMMON,"hltrip.centtral")+"/recSupplier/orderStatusNotice";
                        String res3 = HttpUtil.doPostWithTimeout(statusUrl, JSONObject.toJSONString(req), 10000, TraceConfig.traceHeaders(huoliTrace, statusUrl));
                        log.info("processNotify??????orderStatusNotice??????:"+res3);



                        break;

                    case -1:

                        item.setStatus(2);
                        item.setRefundStatus(bill.getStatus());
                        item.setRefundTime(bill.getTime());
                        item.setRefundMoney(bill.getAmount());
                        item.setBillInfo(JSONObject.toJSONString(bill));
                        tripOrderRefundMapper.updateRefundNotify(item);

                        req.setRefundStatus(-1);
                        log.info("????????????doRefund???????????????:"+url+",??????:"+ JSONObject.toJSONString(req)+",orderId:"+item.getOrderId());
                        String res2 = HttpUtil.doPostWithTimeout(url, JSONObject.toJSONString(req), 10000, TraceConfig.traceHeaders(huoliTrace, url));
                        log.info("??????refundNotice??????:"+res2);



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


    @Override
    public DfyBaseResult<DfyCreateOrderResponse> createToursOrder(DfyCreateToursOrderRequest createOrderReq) {
        String acctid = ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_DIFENGYUN,"difengyun.api.acctId");
        String tel = ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_DIFENGYUN,"difengyun.content.phone");
        String email = ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_DIFENGYUN,"difengyun.content.email");
        String name = ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_DIFENGYUN,"difengyun.content.name");
        String tours_key = ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_DIFENGYUN,"difengyun.api.tours.key");
        String tours_secret = ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_DIFENGYUN,"difengyun.api.tours.secret.key");
        createOrderReq.setAcctId(acctid);
        createOrderReq.setContactTel(tel);
        createOrderReq.setContactEmail(email);
        createOrderReq.setContactName(name);
        createOrderReq.setTraceId(null);
        DfyBaseRequest dfyBaseRequest = new DfyBaseRequest();
        dfyBaseRequest.setApiKey(tours_key);
        dfyBaseRequest.setSecretKey(tours_secret);
        dfyBaseRequest.setData(createOrderReq);

        return diFengYunClient.createToursOrder(dfyBaseRequest);
    }

    @Override
    public DfyBaseResult<DfyVerifyOrderResponse> verifyOrder(DfyBaseRequest<DfyOrderDetailRequest> request) {
        try {
            DfyBaseResult<DfyVerifyOrderResponse> dfyVerifyOrderResponseDfyBaseResult = diFengYunClient.verifyOrder(request);
            log.info("???????????????:"+JSONObject.toJSONString(dfyVerifyOrderResponseDfyBaseResult));
            return dfyVerifyOrderResponseDfyBaseResult;
        } catch (Exception e) {
        	log.error("??????{}",e);
            return null;
        }

    }
}
