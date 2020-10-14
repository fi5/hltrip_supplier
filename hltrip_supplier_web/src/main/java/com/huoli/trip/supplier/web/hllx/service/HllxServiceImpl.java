package com.huoli.trip.supplier.web.hllx.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.huoli.trip.common.constant.OrderStatus;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.common.vo.response.order.OrderDetailRep;
import com.huoli.trip.supplier.api.HllxService;
import com.huoli.trip.supplier.self.hllx.vo.*;
import com.huoli.trip.supplier.web.dao.PriceDao;
import com.huoli.trip.supplier.web.mapper.TripOrderMapper;
import com.huoli.trip.supplier.web.mapper.TripOrderOperationLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Service(timeout = 10000,group = "hltrip")
@Slf4j
public class HllxServiceImpl implements HllxService {
    @Autowired
    private PriceDao priceDao;
    @Autowired
    TripOrderOperationLogMapper tripOrderOperationLogMapper;
    @Autowired
    TripOrderMapper tripOrderMapper;


    @Override
    public HllxBaseResult<HllxBookCheckRes> getCheckInfos(HllxBookCheckReq req) {
        log.info("hllx checkinfo req is:{}", JSON.toJSONString(req));
        PricePO pricePO = priceDao.getByProductCode(req.getProductId());
        log.info("hllx checkinfo PricePO is:{}", JSON.toJSONString(pricePO));

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        if (pricePO != null) {
            List<PriceInfoPO> priceInfos = pricePO.getPriceInfos();
            if (ListUtils.isNotEmpty(priceInfos)) {
                Optional<PriceInfoPO> optionalT = priceInfos.stream().filter(priceInfoPO -> {
                    Date saleDate = priceInfoPO.getSaleDate();
                    String saleDates = formatter.format(saleDate);
                    return StringUtils.equals(req.getBeginDate(), saleDates);
                }).findFirst();
                if (optionalT.isPresent()) {
                    PriceInfoPO priceInfoPO = optionalT.get();
                    Integer stock = priceInfoPO.getStock();
                    if (stock != null && stock > 0) {
                        HllxBookCheckRes hllxBookCheckRes = new HllxBookCheckRes();
                        hllxBookCheckRes.setProductId(req.getProductId());
                        List<HllxBookSaleInfo> saleInfos = new ArrayList<>();
                        hllxBookCheckRes.setSaleInfos(saleInfos);
                        HllxBookSaleInfo hllxBookSaleInfo = new HllxBookSaleInfo();
                        hllxBookSaleInfo.setDate(priceInfoPO.getSaleDate());
                        hllxBookSaleInfo.setPrice(priceInfoPO.getSalePrice());
                        //llxBookSaleInfo.setPriceType(priceInfoPO.getPriceType());
                        hllxBookSaleInfo.setTotalStock(priceInfoPO.getStock());
                        saleInfos.add(hllxBookSaleInfo);
                        return new HllxBaseResult(true, 200, hllxBookCheckRes);
                    }
                }
            }
        }
        return new HllxBaseResult(true, 200, null);
    }

    /**
     * 创建订单，主要是减少库存
     *
     * @param req
     * @return
     */
    @Override
    public HllxBaseResult<HllxCreateOrderRes> createOrder(HllxCreateOrderReq req) {
        PricePO pricePO = priceDao.getByProductCode(req.getProductId());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        if (pricePO == null) {
            return new HllxBaseResult(false, 200, "无价格库存信息");
        }

        List<PriceInfoPO> priceInfos = pricePO.getPriceInfos();
        if (ListUtils.isNotEmpty(priceInfos)) {
            priceInfos.stream().filter(priceInfoPO -> {
                Date saleDate = priceInfoPO.getSaleDate();
                String saleDates = formatter.format(saleDate);
                if (StringUtils.equals(req.getDate(), saleDates)) {
                    priceInfoPO.setStock(priceInfoPO.getStock() - 1);
                }
                return false;
            });
        }
        pricePO.setPriceInfos(priceInfos);
        priceDao.updateByProductCode(pricePO);
        HllxCreateOrderRes hllxCreateOrderRes = new HllxCreateOrderRes();
        hllxCreateOrderRes.setOrderStatus(OrderStatus.TO_BE_PAID.getCode());
        return new HllxBaseResult(true, 200, hllxCreateOrderRes);
    }

    /**
     * 支付
     * @param req
     * @return
     */
    @Override
    public HllxBaseResult<HllxPayOrderRes> payOrder(HllxPayOrderReq req) {
        HllxPayOrderRes hllxPayOrderRes = new HllxPayOrderRes(OrderStatus.TO_BE_CONFIRMED.getCode());
        return new HllxBaseResult(true, 200, hllxPayOrderRes);
    }

    /**
     * 取消订单
     * @param req
     * @return
     */
    @Override
    public HllxBaseResult<HllxCancelOrderRes> cancelOrder(HllxCancelOrderReq req) {
        HllxCancelOrderRes hllxCancelOrderRes = new HllxCancelOrderRes(OrderStatus.CANCELLED.getCode());
        return new HllxBaseResult(true, 200, hllxCancelOrderRes);
    }

    /**
     * 查询订单
     * @param orderId
     * @return
     */
    @Override
    public HllxBaseResult<HllxOrderStatusResult> getOrder(String orderId) {
        TripOrder tripOrder = tripOrderMapper.getOrderStatusByOrderId(orderId);
        if(tripOrder != null) {
            HllxOrderStatusResult hllxOrderStatusResult = new HllxOrderStatusResult();
            hllxOrderStatusResult.setOrderId(tripOrder.getOrderId());
            hllxOrderStatusResult.setOrderStatus(tripOrder.getChannelStatus());
        }
        return new HllxBaseResult(true, 200,tripOrder);
    }

    @Override
    public HllxBaseResult<HllxOrderVoucherResult> getVochers(String orderId) {
        final List<TripOrderVoucher> voucherInfos = tripOrderMapper.getVoucherInfoByOrderId(orderId);

        HllxOrderVoucherResult result= new HllxOrderVoucherResult();
        List<OrderDetailRep.Voucher> vochers=new ArrayList<>();
        result.setOrderId(orderId);

        for(TripOrderVoucher entry : voucherInfos){
            OrderDetailRep.Voucher oneVoucher=new OrderDetailRep.Voucher();
            oneVoucher.setType(entry.getType());
            if(entry.getType()==1){
                oneVoucher.setVocherNo(entry.getVoucherInfo());
            }
            if(entry.getType()==2){
                oneVoucher.setVocherUrl(entry.getVoucherInfo());
            }
            vochers.add(oneVoucher);
        }
        result.setVochers(vochers);
        return new HllxBaseResult(true, 200,result);
    }

    /**
     * 申请退款
     * @param orderId
     * @return
     */
    @Override
    public HllxBaseResult<HllxOrderStatusResult> drawback(String orderId) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        TripOrderOperationLog tripOrderOperationLog = new TripOrderOperationLog();
        tripOrderOperationLog.setOrderId(orderId);
        tripOrderOperationLog.setOperator("系统机器人");
        tripOrderOperationLog.setNewStatus(OrderStatus.APPLYING_FOR_REFUND.getCode());
        tripOrderOperationLog.setUpdateTime(dateFormat.format(new Date()));
        tripOrderOperationLog.setRemark("发起退款申请,订单需要更新状态为:申请退款中");
        tripOrderOperationLogMapper.insertOperationLog(tripOrderOperationLog);
        return new HllxBaseResult(true, 200,null);
    }


}
