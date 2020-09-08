package com.huoli.trip.supplier.web.hllx.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.huoli.trip.common.constant.OrderStatus;
import com.huoli.trip.common.entity.PriceInfoPO;
import com.huoli.trip.common.entity.PricePO;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.supplier.api.HllxService;
import com.huoli.trip.supplier.self.hllx.vo.*;
import com.huoli.trip.supplier.web.dao.PriceDao;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Service(timeout = 10000,group = "hltrip")
public class HllxServiceImpl implements HllxService {
    @Autowired
    private PriceDao priceDao;


    @Override
    public HllxBaseResult<HllxBookCheckRes> getCheckInfos(HllxBookCheckReq req) {
        PricePO pricePO = priceDao.getByProductCode(req.getProductId());
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
                        hllxBookSaleInfo.setPriceType(priceInfoPO.getPriceType());
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
        return new HllxBaseResult(true, 200,null);
    }

}
