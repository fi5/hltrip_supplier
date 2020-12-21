package com.huoli.trip.supplier.web.difengyun.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.huoli.trip.common.constant.CentralError;
import com.huoli.trip.common.constant.ConfigConstants;
import com.huoli.trip.common.entity.PriceInfoPO;
import com.huoli.trip.common.entity.PricePO;
import com.huoli.trip.common.util.ConfigGetter;
import com.huoli.trip.common.util.ListUtils;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

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
        diFengYunClient.submitOrder(dfyBaseRequest);
        return new DfyBaseResult("success",true);
    }

    @Override
    public DfyBaseResult<DfyCreateOrderResponse> createOrder(DfyCreateOrderRequest createOrderReq) {
        DfyBaseRequest dfyBaseRequest = new DfyBaseRequest();
        String acctid = ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_DIFENGYUN,"difengyun.api.acctId");
        createOrderReq.setAcctId(acctid);
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
}
