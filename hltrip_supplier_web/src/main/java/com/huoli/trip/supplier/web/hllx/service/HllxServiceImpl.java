package com.huoli.trip.supplier.web.hllx.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.huoli.trip.common.constant.OrderStatus;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.entity.mpo.groupTour.GroupTourPrice;
import com.huoli.trip.common.entity.mpo.groupTour.GroupTourProductSetMealMPO;
import com.huoli.trip.common.entity.mpo.hotelScenicSpot.HotelScenicSpotPriceStock;
import com.huoli.trip.common.entity.mpo.hotelScenicSpot.HotelScenicSpotProductMPO;
import com.huoli.trip.common.entity.mpo.hotelScenicSpot.HotelScenicSpotProductSetMealMPO;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotProductPriceMPO;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.common.vo.response.order.OrderDetailRep;
import com.huoli.trip.common.vo.v2.GroupTourProductSetMeal;
import com.huoli.trip.supplier.api.HllxService;
import com.huoli.trip.supplier.self.difengyun.vo.DfyBookSaleInfo;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyBaseResult;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyBookCheckResponse;
import com.huoli.trip.supplier.self.hllx.vo.*;
import com.huoli.trip.supplier.web.dao.*;
import com.huoli.trip.supplier.web.mapper.BackChannelMapper;
import com.huoli.trip.supplier.web.mapper.TripOrderMapper;
import com.huoli.trip.supplier.web.mapper.TripOrderOperationLogMapper;
import com.huoli.trip.supplier.web.util.SendMessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


@Service(timeout = 10000, group = "hltrip")
@Slf4j
public class HllxServiceImpl implements HllxService {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    @Autowired
    private PriceDao priceDao;
    @Autowired
    private TripOrderOperationLogMapper tripOrderOperationLogMapper;
    @Autowired
    private TripOrderMapper tripOrderMapper;
    @Autowired
    private BackChannelMapper backChannelMapper;
    @Autowired
    private SendMessageUtil sendMessageUtil;

    @Autowired
    private GroupTourProductSetMealDao groupTourProductSetMealDao;

    @Autowired
    private ScenicSpotProductPriceDao scenicSpotProductPriceDao;

    @Autowired
    private HotelScenicProductSetMealDao hotelScenicProductSetMealDao;

    @Autowired
    private HotelScenicProductDao hotelScenicProductDao;



    @Override
    public HllxBaseResult<HllxBookCheckRes> getCheckInfos(HllxBookCheckReq req) {
        log.info("hllx checkinfo req is:{}", JSON.toJSONString(req));
        String category = req.getCategory();
        if(StringUtils.isNotBlank(category)){
            HllxBookCheckRes hllxBookCheckRes = null;
            switch (category){
                case "d_ss_ticket":
                    ScenicSpotProductPriceMPO priceMPO = scenicSpotProductPriceDao.getPriceByPackageId(req.getPackageId());
                    if(priceMPO != null && priceMPO.getStock() >= req.getAdtNum()){
                        hllxBookCheckRes = new HllxBookCheckRes();
                        hllxBookCheckRes.setProductId(req.getProductId());
                        hllxBookCheckRes.setPackageId(req.getPackageId());
                        List<HllxBookSaleInfo> saleInfos = new ArrayList<>();
                        hllxBookCheckRes.setSaleInfos(saleInfos);
                        HllxBookSaleInfo hllxBookSaleInfo = new HllxBookSaleInfo();
                        hllxBookSaleInfo.setDate(DateTimeUtil.parseDate(req.getBeginDate()));
                        hllxBookSaleInfo.setPrice(priceMPO.getSellPrice());
                        hllxBookSaleInfo.setTotalStock(priceMPO.getStock());
                        saleInfos.add(hllxBookSaleInfo);
                    }
                    break;
                case "group_tour":
                    GroupTourProductSetMealMPO groupTourProductSetMealMPO = groupTourProductSetMealDao.getSetMealByPackageId(req.getPackageId());
                    List<GroupTourPrice> groupTourPrices = groupTourProductSetMealMPO.getGroupTourPrices();
                    if(CollectionUtils.isNotEmpty(groupTourPrices)){
                        groupTourPrices = groupTourPrices.stream().filter(a -> StringUtils.equals(a.getDate(), req.getBeginDate())).collect(Collectors.toList());
                    }
                    if (CollectionUtils.isNotEmpty(groupTourPrices)) {
                        GroupTourPrice groupTourPrice = groupTourPrices.get(0);
                        if(groupTourPrice.getAdtStock() >= req.getAdtNum() && groupTourPrice.getChdStock() >= req.getChdNum()){
                            hllxBookCheckRes = new HllxBookCheckRes();
                            hllxBookCheckRes.setProductId(req.getProductId());
                            hllxBookCheckRes.setPackageId(req.getPackageId());
                            List<HllxBookSaleInfo> saleInfos = new ArrayList<>();
                            hllxBookCheckRes.setSaleInfos(saleInfos);
                            HllxBookSaleInfo hllxBookSaleInfo = new HllxBookSaleInfo();
                            hllxBookSaleInfo.setDate(DateTimeUtil.parseDate(groupTourPrice.getDate()));
                            hllxBookSaleInfo.setPrice(groupTourPrice.getAdtSellPrice());
                            hllxBookSaleInfo.setTotalStock(groupTourPrice.getAdtStock());
                            saleInfos.add(hllxBookSaleInfo);
                        }
                    }
                    break;
                case "hotel_scenicSpot":
                    HotelScenicSpotProductMPO productMPO = hotelScenicProductDao.getByProductId(req.getProductId());
                    HotelScenicSpotProductSetMealMPO hotelScenicSpotProductSetMealMPO = hotelScenicProductSetMealDao.getSetMealByPackageId(req.getPackageId());
                    List<HotelScenicSpotPriceStock> priceStocks = hotelScenicSpotProductSetMealMPO.getPriceStocks();
                    if (CollectionUtils.isNotEmpty(priceStocks) && productMPO.getPayInfo().getSellType() == 1) {
                        priceStocks = priceStocks.stream().filter(a -> StringUtils.equals(a.getDate(), req.getBeginDate())).collect(Collectors.toList());
                    }
                    if (CollectionUtils.isNotEmpty(priceStocks)) {
                        HotelScenicSpotPriceStock hotelScenicSpotPriceStock = priceStocks.get(0);
                        if(hotelScenicSpotPriceStock.getAdtStock() >= req.getAdtNum() && hotelScenicSpotPriceStock.getChdStock() >= req.getChdNum()){
                            hllxBookCheckRes = new HllxBookCheckRes();
                            hllxBookCheckRes.setProductId(req.getProductId());
                            hllxBookCheckRes.setPackageId(req.getPackageId());
                            List<HllxBookSaleInfo> saleInfos = new ArrayList<>();
                            hllxBookCheckRes.setSaleInfos(saleInfos);
                            HllxBookSaleInfo hllxBookSaleInfo = new HllxBookSaleInfo();
                            hllxBookSaleInfo.setDate(DateTimeUtil.parseDate(hotelScenicSpotPriceStock.getDate()));
                            hllxBookSaleInfo.setPrice(hotelScenicSpotPriceStock.getAdtSellPrice());
                            hllxBookSaleInfo.setTotalStock(hotelScenicSpotPriceStock.getAdtStock());
                            saleInfos.add(hllxBookSaleInfo);
                        }
                    }
                    break;
            }
            return new HllxBaseResult(true, 200, hllxBookCheckRes);
        }
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
                    log.info("hllx checkinfo PricePO is:{}", JSON.toJSONString(priceInfoPO));
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
                        log.info("hllx checkinfo resp is :{}", JSON.toJSONString(hllxBookCheckRes));
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
        log.info("创建订单请求为：{}", JSON.toJSONString(req));
        String category = req.getCategory();
        if (StringUtils.isNotBlank(category)) {
            switch (category){
                case "d_ss_ticket":
                    ScenicSpotProductPriceMPO priceMPO = scenicSpotProductPriceDao.getPriceByPackageId(req.getPackageId());
                    if(priceMPO == null){
                        return new HllxBaseResult<>(false, 200, "无价格库存信息");
                    }
                    if(priceMPO.getStock() <=req.getQunatity()){
                        return new HllxBaseResult<>(false, 200, "库存不足");
                    }
                    priceMPO.setStock(priceMPO.getStock() - req.getQunatity());
                    scenicSpotProductPriceDao.updatePriceStock(priceMPO);
                    break;
                case "group_tour":
                    GroupTourProductSetMealMPO groupTourProductSetMealMPO = groupTourProductSetMealDao.getSetMealByPackageId(req.getPackageId());
                    List<GroupTourPrice> groupTourPrices = groupTourProductSetMealMPO.getGroupTourPrices();
                    Map<String, GroupTourPrice> groupTourPriceMap = Maps.uniqueIndex(groupTourPrices, item -> item.getDate());
                    GroupTourPrice groupTourPrice = groupTourPriceMap.get(req.getDate());
                    if(groupTourPrice == null || (groupTourPrice.getAdtStock() < req.getAdtQuantity() || groupTourPrice.getChdStock() < req.getChildQuantity())){
                        log.error("库存不足:adtStock : {},{}; chdStock:{}, {}", groupTourPrice.getAdtPrice(), req.getAdtQuantity(), groupTourPrice.getChdPrice(), req.getChildQuantity());
                        return new HllxBaseResult<>(false, 200, "库存不足");
                    }
                    groupTourPrice.setAdtStock(groupTourPrice.getAdtStock() - req.getAdtQuantity());
                    groupTourPrice.setChdStock(groupTourPrice.getChdStock() - req.getChildQuantity());
                    groupTourProductSetMealDao.updatePriceStock(groupTourProductSetMealMPO, groupTourPrice);
                    break;
                case "hotel_scenicSpot":
                    HotelScenicSpotProductMPO productMPO = hotelScenicProductDao.getByProductId(req.getProductId());
                    HotelScenicSpotProductSetMealMPO hotelScenicSpotProductSetMealMPO = hotelScenicProductSetMealDao.getSetMealByPackageId(req.getPackageId());
                    List<HotelScenicSpotPriceStock> priceStocks = hotelScenicSpotProductSetMealMPO.getPriceStocks();
                    HotelScenicSpotPriceStock hotelScenicSpotPriceStock = null;
                    if(productMPO.getPayInfo().getSellType() == 0){
                        hotelScenicSpotPriceStock = priceStocks.get(0);
                        if(hotelScenicSpotPriceStock.getAdtStock() < req.getAdtQuantity() || hotelScenicSpotPriceStock.getChdStock() < req.getChildQuantity()){
                            log.error("库存不足：adtStock:{},{}; chdStock:{},{}", hotelScenicSpotPriceStock.getAdtStock(), req.getAdtQuantity(), hotelScenicSpotPriceStock.getChdStock(), req.getChildQuantity());
                            return new HllxBaseResult<>(false, 200, "库存不足");
                        }
                        hotelScenicSpotPriceStock.setAdtStock(hotelScenicSpotPriceStock.getAdtStock() - req.getAdtQuantity());
                        hotelScenicSpotPriceStock.setChdStock(hotelScenicSpotPriceStock.getChdStock() - req.getChildQuantity());
                    }else{
                        Map<String, HotelScenicSpotPriceStock> priceStockMap = Maps.uniqueIndex(priceStocks, item -> item.getDate());
                        hotelScenicSpotPriceStock = priceStockMap.get(req.getDate());
                        if(hotelScenicSpotPriceStock == null || hotelScenicSpotPriceStock.getAdtStock() < req.getAdtQuantity() || hotelScenicSpotPriceStock.getChdStock() < req.getChildQuantity()){
                            log.error("库存不足：adtStock:{},{}; chdStock:{},{}", hotelScenicSpotPriceStock.getAdtStock(), req.getAdtQuantity(), hotelScenicSpotPriceStock.getChdStock(), req.getChildQuantity());
                            return new HllxBaseResult<>(false, 200, "库存不足");
                        }
                        hotelScenicSpotPriceStock.setAdtStock(hotelScenicSpotPriceStock.getAdtStock() - req.getAdtQuantity());
                        hotelScenicSpotPriceStock.setChdStock(hotelScenicSpotPriceStock.getChdStock() - req.getChildQuantity());
                    }
                    hotelScenicProductSetMealDao.updatePriceStock(hotelScenicSpotProductSetMealMPO, hotelScenicSpotPriceStock);
                    break;
            }
        }
        /*PricePO pricePO = priceDao.getByProductCode(req.getProductId());
        log.info("创建订单查询到的原始库存数据为：{}", JSON.toJSONString(pricePO));
        if (pricePO == null) {
            return new HllxBaseResult(false, 200, "无价格库存信息");
        }

        List<PriceInfoPO> priceInfos = pricePO.getPriceInfos();
        if (ListUtils.isNotEmpty(priceInfos)) {
            priceInfos.forEach(priceInfoPO -> {
                Date saleDate = priceInfoPO.getSaleDate();
                String saleDates = formatter.format(saleDate);
                if (StringUtils.equals(req.getDate(), saleDates)) {
                    log.info("创建订单匹配到的原始库存数据为：{}", JSON.toJSONString(priceInfoPO));
                    priceInfoPO.setStock(priceInfoPO.getStock() - req.getQunatity());
                    if (priceInfoPO.getSales() != null) {
                        priceInfoPO.setSales(priceInfoPO.getSales() + req.getQunatity());
                    } else {
                        priceInfoPO.setSales(req.getQunatity());

                    }
                }
            });
        }
        pricePO.setPriceInfos(priceInfos);
        log.info("创建订单更新后的库存数据为：{}", JSON.toJSONString(pricePO));
        priceDao.updateByProductCode(pricePO);*/
        HllxCreateOrderRes hllxCreateOrderRes = new HllxCreateOrderRes();
        hllxCreateOrderRes.setOrderStatus(OrderStatus.TO_BE_PAID.getCode());
        return new HllxBaseResult(true, 200, hllxCreateOrderRes);
    }

    /**
     * 支付
     *
     * @param req
     * @return
     */
    @Override
    public HllxBaseResult<HllxPayOrderRes> payOrder(HllxPayOrderReq req) {
        HllxPayOrderRes hllxPayOrderRes = new HllxPayOrderRes(OrderStatus.TO_BE_CONFIRMED.getCode());
        BackChannelEntry channelInfoByChannelCode = backChannelMapper.getChannelInfoByChannelCode(req.getChannelCode());
        if(channelInfoByChannelCode != null){
            String payNoticePhone = channelInfoByChannelCode.getPayNoticePhone();
            if(StringUtils.isNotEmpty(payNoticePhone)){
                String[] phone = payNoticePhone.split(",");
                for(String s: phone){
                    sendMessageUtil.sendMSG(s,req.getChannelOrderId(),1);
                }
            }
        }
        return new HllxBaseResult(true, 200, hllxPayOrderRes);
    }

    /**
     * 取消订单
     *
     * @param req
     * @return
     */
    @Override
    public HllxBaseResult<HllxCancelOrderRes> cancelOrder(HllxCancelOrderReq req) {
        log.info("客户调用取消订单：{}", JSON.toJSONString(req));
        HllxCancelOrderRes hllxCancelOrderRes = new HllxCancelOrderRes(OrderStatus.CANCELLED.getCode());
        TripOrder tripOrder = tripOrderMapper.getOrderStatusByOrderId(req.getPartnerOrderId());
        log.info("取消订单查到的订单数据为：{}", JSON.toJSONString(tripOrder));
        if (tripOrder != null) {
            try {
                int total = tripOrder.getQuantity() + tripOrder.getChildQuantity();
                PricePO pricePO = priceDao.getByProductCode(tripOrder.getProductId());
                if (pricePO != null) {
                    List<PriceInfoPO> priceInfos = pricePO.getPriceInfos();
                    if (ListUtils.isNotEmpty(priceInfos)) {
                        priceInfos.forEach(priceInfoPO -> {
                            Date saleDate = priceInfoPO.getSaleDate();
                            String saleDates = formatter.format(saleDate);
                            if (StringUtils.equals(tripOrder.getBeginDate(), saleDates)) {
                                log.info("取消订单匹配到的原始库存数据为：{}", JSON.toJSONString(priceInfoPO));
                                if (priceInfoPO.getStock() != null) {
                                    priceInfoPO.setStock(priceInfoPO.getStock() + total);
                                } else {
                                    priceInfoPO.setStock(total);
                                }
                                if (priceInfoPO.getSales() != null) {
                                    priceInfoPO.setSales(priceInfoPO.getSales() - total);
                                } else {
                                    priceInfoPO.setSales(0);
                                }
                            }
                        });
                    }
                    pricePO.setPriceInfos(priceInfos);
                }
                priceDao.updateByProductCode(pricePO);
            } catch (Exception ex) {
                log.error("取消订单修改库存出现异常,订单号为：{}", req.getPartnerOrderId(), ex);

            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            TripOrderOperationLog tripOrderOperationLog = new TripOrderOperationLog();
            tripOrderOperationLog.setOrderId(req.getPartnerOrderId());
            tripOrderOperationLog.setOperator("订单客户");
            tripOrderOperationLog.setNewStatus(OrderStatus.CANCELLED.getCode());
            tripOrderOperationLog.setUpdateTime(dateFormat.format(new Date()));
            tripOrderOperationLog.setRemark("客户发起请求取消订单");
            try {
                tripOrderOperationLogMapper.insertOperationLog(tripOrderOperationLog);
            } catch (Exception ex) {
                log.error("取消订单写入日志出现异常,订单号为：{}", req.getPartnerOrderId(), ex);
            }

        }

        return new HllxBaseResult(true, 200, hllxCancelOrderRes);
    }

    /**
     * 查询订单
     *
     * @param orderId
     * @return
     */
    @Override
    public HllxBaseResult<HllxOrderStatusResult> getOrder(String orderId) {
        TripOrder tripOrder = tripOrderMapper.getOrderStatusByOrderId(orderId);
        if (tripOrder != null) {
            HllxOrderStatusResult hllxOrderStatusResult = new HllxOrderStatusResult();
            hllxOrderStatusResult.setOrderId(tripOrder.getOrderId());
            hllxOrderStatusResult.setOrderStatus(tripOrder.getChannelStatus());
            return new HllxBaseResult(true, 200, hllxOrderStatusResult);
        } else {
            return new HllxBaseResult(false, 500, "未查询到订单信息");

        }
    }

    @Override
    public HllxBaseResult<HllxOrderVoucherResult> getVochers(String orderId) {
        final List<TripOrderVoucher> voucherInfos = tripOrderMapper.getVoucherInfoByOrderId(orderId);

        HllxOrderVoucherResult result = new HllxOrderVoucherResult();
        List<OrderDetailRep.Voucher> vochers = new ArrayList<>();
        result.setOrderId(orderId);

        for (TripOrderVoucher entry : voucherInfos) {
            OrderDetailRep.Voucher oneVoucher = new OrderDetailRep.Voucher();
            oneVoucher.setType(entry.getType());
            if (entry.getType() == 1) {
                oneVoucher.setVocherNo(entry.getVoucherInfo());
            }
            if (entry.getType() == 2) {
                oneVoucher.setVocherUrl(entry.getVoucherInfo());
            }
            vochers.add(oneVoucher);
        }
        result.setVochers(vochers);
        return new HllxBaseResult(true, 200, result);
    }

    /**
     * 申请退款
     *
     * @param orderId
     * @return
     */
    @Override
    public HllxBaseResult<HllxOrderStatusResult> drawback(String orderId) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        TripOrder tripChannel = tripOrderMapper.getChannelByOrderId(orderId);
        if(tripChannel != null) {
            String channel = tripChannel.getChannel();
            BackChannelEntry channelInfoByChannelCode = backChannelMapper.getChannelInfoByChannelCode(channel);
            if (channelInfoByChannelCode != null) {
                String refundNoticePhone = channelInfoByChannelCode.getRefundNoticePhone();
                if (StringUtils.isNotEmpty(refundNoticePhone)) {
                    String[] phone = refundNoticePhone.split(",");
                    for (String s : phone) {
                        sendMessageUtil.sendMSG(s,orderId,2);
                    }
                }
            }
        }

        TripOrderOperationLog tripOrderOperationLog = new TripOrderOperationLog();
        tripOrderOperationLog.setOrderId(orderId);
        tripOrderOperationLog.setOperator("系统机器人");
        tripOrderOperationLog.setNewStatus(OrderStatus.APPLYING_FOR_REFUND.getCode());
        tripOrderOperationLog.setUpdateTime(dateFormat.format(new Date()));
        tripOrderOperationLog.setRemark("发起退款申请,订单需要更新状态为:申请退款中");
        try {
            tripOrderOperationLogMapper.insertOperationLog(tripOrderOperationLog);
        }catch (Exception ex){
            log.error("写入退款申请操作记录失败，请求信息为：{}",JSON.toJSONString(tripOrderOperationLog),ex);
        }
        return new HllxBaseResult(true, 200, null);
    }


}
