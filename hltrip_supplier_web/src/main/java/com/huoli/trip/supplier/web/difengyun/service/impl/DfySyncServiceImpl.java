package com.huoli.trip.supplier.web.difengyun.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.util.CommonUtils;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.common.util.MongoDateUtils;
import com.huoli.trip.supplier.feign.client.difengyun.client.IDiFengYunClient;
import com.huoli.trip.supplier.self.difengyun.constant.DfyConstants;
import com.huoli.trip.supplier.self.difengyun.vo.*;
import com.huoli.trip.supplier.self.difengyun.vo.request.*;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyBaseResult;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyScenicListResponse;
import com.huoli.trip.supplier.web.dao.PriceDao;
import com.huoli.trip.supplier.web.dao.ProductDao;
import com.huoli.trip.supplier.web.dao.ProductItemDao;
import com.huoli.trip.supplier.web.difengyun.convert.DfyConverter;
import com.huoli.trip.supplier.web.difengyun.service.DfySyncService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/12/9<br>
 */
@Service
@Slf4j
public class DfySyncServiceImpl implements DfySyncService {

    @Autowired
    private IDiFengYunClient diFengYunClient;

    @Autowired
    private ProductItemDao productItemDao;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private PriceDao priceDao;

    @Override
    public boolean syncScenicList(DfyScenicListRequest request){
        try {
            DfyBaseRequest<DfyScenicListRequest> listRequest = new DfyBaseRequest<>(request);
            DfyBaseResult<DfyScenicListResponse> baseResult = diFengYunClient.getScenicList(listRequest);
            DfyScenicListResponse response = baseResult.getData();
            if(response != null && ListUtils.isNotEmpty(response.getRows())){
                List<DfyScenic> scenics= response.getRows();
                scenics.forEach(s -> syncScenicDetail(s.getScenicId()));
                return true;
            } else {
                log.error("笛风云门票列表返回空，request = {}", JSON.toJSONString(listRequest));
                return false;
            }
        } catch (Exception e) {
            log.error("笛风云同步景点、产品异常", e);
            return false;
        }
    }

    @Override
    public void syncScenicDetail(String scenicId){
        DfyScenicDetailRequest detailRequest = new DfyScenicDetailRequest();
        detailRequest.setScenicId(scenicId);
        DfyBaseRequest detailBaseRequest = new DfyBaseRequest<>(detailRequest);
        DfyBaseResult<DfyScenicDetail> detailBaseResult = diFengYunClient.getScenicDetail(detailBaseRequest);
        if(detailBaseResult != null && detailBaseResult.getData() != null){
            DfyScenicDetail scenicDetail = detailBaseResult.getData();
            ProductItemPO productItem = DfyConverter.convertToProductItemPO(scenicDetail);
            ProductItemPO productItemPO = productItemDao.selectByCode(productItem.getCode());
            if(productItemPO == null){
                productItem.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
            }
            productItem.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
            productItem.setOperator(Constants.SUPPLIER_CODE_DFY);
            productItem.setOperatorName(Constants.SUPPLIER_NAME_DFY);
            productItemDao.updateByCode(productItem);
            productItemPO = productItemDao.selectByCode(productItem.getCode());
            List<DfyTicket> allTickets = Lists.newArrayList();
            if(ListUtils.isNotEmpty(scenicDetail.getTicketList())){
                allTickets.addAll(scenicDetail.getTicketList());
            }
            if(ListUtils.isNotEmpty(scenicDetail.getDisTickets())){
                scenicDetail.getDisTickets().forEach(t -> t.setExclusive(1));
                allTickets.addAll(scenicDetail.getDisTickets());
            }
            if(ListUtils.isNotEmpty(allTickets)){
                for (DfyTicket dfyTicket : allTickets) {
                    syncProduct(dfyTicket.getProductId(), productItemPO);
                }
            }
        } else {
            log.error("笛风云门票详情返回空，request = {}", JSON.toJSONString(detailBaseRequest));
        }
    }

    @Override
    public void syncProduct(String productId, ProductItemPO productItemPO){
        DfyTicketDetailRequest ticketDetailRequest = new DfyTicketDetailRequest();
        ticketDetailRequest.setProductId(Integer.valueOf(productId));
        DfyBaseRequest ticketDetailBaseRequest = new DfyBaseRequest<>(ticketDetailRequest);
        DfyBaseResult<DfyTicketDetail> ticketDetailDfyBaseResult = diFengYunClient.getTicketDetail(ticketDetailBaseRequest);

        if(ticketDetailDfyBaseResult != null && ticketDetailDfyBaseResult.getData() != null){
            DfyTicketDetail dfyTicketDetail = ticketDetailDfyBaseResult.getData();
            // 当通知更新的时候这个item是空的
            if(productItemPO == null){
                String scenicId = dfyTicketDetail.getScenicId();
                // 如果没有景点id更新不了产品，因为产品是绑在景点下的
                if(StringUtils.isBlank(scenicId)){
                    log.error("门票productId={}，没有景点id", productId);
                    return;
                }
                // 如果景点不存在就去更新景点，更新景点的同时会更新门票，所以下面就不用走了。
                productItemPO = productItemDao.selectByCode(CommonUtils.genCodeBySupplier(Constants.SUPPLIER_CODE_DFY, scenicId));
                if(productItemPO == null){
                    syncScenicDetail(scenicId);
                    return;
                }
            }
            ProductPO product = DfyConverter.convertToProductPO(dfyTicketDetail);
            product.setMainItemCode(productItemPO.getCode());
            product.setMainItem(productItemPO);
            product.setCity(productItemPO.getCity());
            product.setDesCity(productItemPO.getDesCity());
            product.setOriCity(productItemPO.getOriCity());
            if(product.getTicket() != null && ListUtils.isNotEmpty(product.getTicket().getTickets())){
                for (TicketInfoPO ticket : product.getTicket().getTickets()) {
                    ticket.setItemId(productItemPO.getCode());
                    ticket.setProductItem(productItemPO);
                }
            }
            ProductPO productPO = productDao.getByCode(product.getCode());
            if(productPO == null){
                product.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
            }
            product.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
            product.setOperator(Constants.SUPPLIER_CODE_DFY);
            product.setOperatorName(Constants.SUPPLIER_NAME_DFY);
            product.setValidTime(DateTimeUtil.trancateToDate(MongoDateUtils.handleTimezoneInput(new Date())));
            log.info("准备更新价格。。。");
            if(ListUtils.isNotEmpty(ticketDetailDfyBaseResult.getData().getPriceCalendar())){
                log.info("有价格信息。。。{}", JSON.toJSONString(ticketDetailDfyBaseResult.getData().getPriceCalendar()));
                PricePO pricePO = syncPrice(product.getCode(), ticketDetailDfyBaseResult.getData().getPriceCalendar());
                if(pricePO != null && ListUtils.isNotEmpty(pricePO.getPriceInfos())){
                    // 笛风云没有上下架时间，就把最远的销售日期作为下架时间
                    PriceInfoPO priceInfoPO = pricePO.getPriceInfos().stream().max(Comparator.comparing(PriceInfoPO::getSaleDate)).get();
                    product.setInvalidTime(priceInfoPO.getSaleDate());
                }
            } else {
                product.setInvalidTime(product.getValidTime());
                log.error("没有价格信息。。。。");
            }
            productDao.updateByCode(product);
        } else {
            log.error("笛风云产品详情返回空，request = {}", JSON.toJSONString(ticketDetailBaseRequest));
        }
    }

    /**
     * 设置价格日历
     * @param productCode
     * @param priceCalendar
     */
    private PricePO syncPrice(String productCode, List<DfyPriceCalendar> priceCalendar){
        log.info("查询价格。。");
        PricePO pricePO = priceDao.getByProductCode(productCode);
        PricePO price = DfyConverter.convertToPricePO(priceCalendar);
        price.setProductCode(productCode);
        if(pricePO == null){
            log.info("没有查询到价格，准备新建。。");
            price.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
        }
        price.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
        price.setOperator(Constants.SUPPLIER_CODE_DFY);
        price.setOperatorName(Constants.SUPPLIER_NAME_DFY);
        priceDao.updateByProductCode(price);
        log.info("价格已更新。。。{}", JSON.toJSONString(price));
        return price;
    }

    @Override
    @Async
    public void productUpdate(DfyProductNoticeRequest request){
        try {
            List<DfyProductNotice> productNotices = request.getProductNotices();
            if(ListUtils.isEmpty(productNotices)){
                log.error("笛风云通知更新产品列表为空");
                return;
            }
            productNotices.forEach(p -> {
                if(p.getClassBrandParentId() == DfyConstants.BRAND_GROUP){
                    log.error("笛风云更新产品 {} 是跟团，跳过。", p.getProductId());
                    return;
                }
                // 如果只是更新状态直接在这里改就行
                if(p.getNoticeType() == DfyConstants.NOTICE_TYPE_INVALID || p.getNoticeType() == DfyConstants.NOTICE_TYPE_VALID){
                    ProductPO productPO = productDao.getByCode(CommonUtils.genCodeBySupplier(Constants.SUPPLIER_CODE_DFY, p.getProductId().toString()));
                    // 如果本地有就直接更新
                    if(productPO != null){
                        productPO.setStatus(p.getNoticeType() == DfyConstants.NOTICE_TYPE_INVALID ? Constants.PRODUCT_STATUS_INVALID : Constants.PRODUCT_STATUS_VALID);
                        productPO.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                        productDao.updateByCode(productPO);
                        return;
                    }
                }
                // 如果本地没有就走同步产品流程
                syncProduct(p.getProductId().toString(), null);
            });
        } catch (Exception e) {
            log.error("笛风云接收通知更新产品异常，", e);
        }
    }
}
