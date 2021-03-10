package com.huoli.trip.supplier.web.task;

import com.google.common.collect.Lists;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.entity.PricePO;
import com.huoli.trip.common.entity.ProductItemPO;
import com.huoli.trip.common.entity.ProductPO;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.supplier.api.DynamicProductItemService;
import com.huoli.trip.supplier.web.dao.PriceDao;
import com.huoli.trip.supplier.web.dao.ProductDao;
import com.huoli.trip.supplier.web.dao.ProductItemDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/1/11<br>
 */
@Slf4j
@Component
public class RefreshProductTask {

    @Value("${schedule.executor}")
    private String schedule;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private PriceDao priceDao;

    @Autowired
    private DynamicProductItemService dynamicProductItemService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void refreshItemProduct(){
        if(schedule == null || !StringUtils.equalsIgnoreCase("yes", schedule)){
            return;
        }
        long s = System.currentTimeMillis();
        log.info("开始执行刷新产品状态任务。。。");
        List<ProductPO> products = productDao.getProductsByStatus(Constants.PRODUCT_STATUS_VALID);
        Date date = new Date();
        products.forEach(productPO -> {
            checkProduct(productPO, date);
        });
        long t = System.currentTimeMillis() - s;
        log.info("刷新产品状态任务执行完毕。用时{}", DateTimeUtil.format(DateTimeUtil.toGreenWichTime(new Date(t)), "HH:mm:ss"));
    }

    private void checkProduct(ProductPO productPO, Date date){
        try {
            if(productPO.getValidTime() != null && date.getTime() < productPO.getValidTime().getTime()){
                log.error("还没到销售日期。。。code = {}, validDate = {}",
                        productPO.getCode(), DateTimeUtil.formatDate(productPO.getValidTime()));
                productDao.updateStatusByCode(productPO.getCode(), Constants.PRODUCT_STATUS_INVALID_SALE_DATE);
                return;
            }
            if(productPO.getInvalidTime() != null && date.getTime() > productPO.getInvalidTime().getTime()){
                log.error("已经过了销售日期。。。code = {}, invalidDate = {}",
                        productPO.getCode(), DateTimeUtil.formatDate(productPO.getInvalidTime()));
                productDao.updateStatusByCode(productPO.getCode(), Constants.PRODUCT_STATUS_INVALID_SALE_DATE);
                return;
            }
            PricePO pricePO = priceDao.getByProductCode(productPO.getCode());
            if(pricePO == null || ListUtils.isEmpty(pricePO.getPriceInfos())){
                log.error("没有价格信息，code = {}", productPO.getCode());
                productDao.updateStatusByCode(productPO.getCode(), Constants.PRODUCT_STATUS_INVALID_PRICE_STOCK);
                return;
            }
            if(!pricePO.getPriceInfos().stream().anyMatch(p -> checkDate(p.getSaleDate(), date)
                    && checkPrice(p.getSalePrice()) && checkStock(p.getStock()))){
                log.error("没有有效的价格和库存信息，code = {}", productPO.getCode());
                productDao.updateStatusByCode(productPO.getCode(), Constants.PRODUCT_STATUS_INVALID_PRICE_STOCK);
                return;
            }
            if(!pricePO.getPriceInfos().stream().anyMatch(p -> checkDate(p.getSaleDate(), date)
                    && checkPrice(p.getSalePrice()))){
                log.error("没有有效的价格信息，code = {}", productPO.getCode());
                productDao.updateStatusByCode(productPO.getCode(), Constants.PRODUCT_STATUS_INVALID_PRICE);
                return;
            }
            if(!pricePO.getPriceInfos().stream().anyMatch(p -> checkDate(p.getSaleDate(), date)
                    && checkStock(p.getStock()))){
                log.error("没有有效的库存信息，code = {}", productPO.getCode());
                productDao.updateStatusByCode(productPO.getCode(), Constants.PRODUCT_STATUS_INVALID_STOCK);
                return;
            }
            // 自动上线，这个要放在最后判断，如果放在前面的话价格状态有问题会被再次修改，状态可能会有短暂不准确
            if(productPO.getValidTime() != null && productPO.getValidTime() != null
                    && date.getTime() >= productPO.getValidTime().getTime()
                    && date.getTime() <= productPO.getInvalidTime().getTime()
                    && Constants.PRODUCT_STATUS_INVALID_SALE_DATE == productPO.getStatus() ){
                log.error("已进入销售日期范围，并且状态是日期异常，改成上线。。。code = {}, validDate = {}",
                        productPO.getCode(), DateTimeUtil.formatDate(productPO.getValidTime()));
                productDao.updateStatusByCode(productPO.getCode(), Constants.PRODUCT_STATUS_INVALID_SALE_DATE);
                return;
            }
        } catch (Exception e) {
            log.error("刷新产品状态异常，productCode={}", productPO.getCode(), e);
        }
    }


    private boolean checkPrice(BigDecimal price){
        return price != null && price.compareTo(BigDecimal.valueOf(0)) == 1;
    }

    private boolean checkStock(Integer stock){
        return stock != null && stock > 0;
    }

    private boolean checkDate(Date cDate, Date nDate){
        if(cDate == null){
            return false;
        }
        long cTime = DateTimeUtil.trancateToDate(cDate).getTime();
        long nTime = DateTimeUtil.trancateToDate(nDate).getTime();
        return cTime >= nTime;
    }
}
