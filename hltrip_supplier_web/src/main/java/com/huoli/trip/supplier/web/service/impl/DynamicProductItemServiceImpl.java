package com.huoli.trip.supplier.web.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.huoli.trip.common.entity.PriceInfoPO;
import com.huoli.trip.common.entity.ProductItemPO;
import com.huoli.trip.common.entity.ProductPO;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.supplier.api.DynamicProductItemService;
import com.huoli.trip.supplier.web.dao.ProductDao;
import com.huoli.trip.supplier.web.dao.ProductItemDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/8/27<br>
 */
@Service
@Slf4j
public class DynamicProductItemServiceImpl implements DynamicProductItemService {

    @Autowired
    private ProductDao productDao;

    @Autowired
    private ProductItemDao productItemDao;

    @Override
    @Async
    public void refreshItemByProductCode(String productCode){
        try {
            log.info("开始根据产品码{}刷新item低价产品。。。", productCode);
            ProductPO productPO = productDao.getByCode(productCode);
            if(productPO != null && productPO.getMainItemCode() != null){
                refreshItem(productPO.getMainItemCode());
                log.info("根据产品码{}刷新item低价产品完成。", productCode);
                return;
            }
            log.error("根据产品码{}刷新item低价产品失败，产品或者产品主项目编码为空", productCode);
        } catch (Exception e) {
            log.error("根据产品码{}刷新item低价产品异常！", productCode, e);
        }
    }

    @Override
    @Async
    public void refreshItemByCode(String code){
        try {
            log.info("开始根据item编码{}刷新item低价产品。。。", code);
            refreshItem(code);
            log.info("根据item编码{}刷新item低价产品完成。", code);
        } catch (Exception e) {
            log.error("根据item编码{}刷新item低价产品异常！", code, e);
        }
    }

    private void refreshItem(String code){
        ProductItemPO productItemPO = productItemDao.selectByCode(code);
        if(productItemPO == null){
            log.error("刷新item，没有查到item={}", code);
            return;
        }
        ProductPO productPO =  productDao.getProductListByItemId(code);
        if(productPO == null){
            log.error("刷新item，没有查到item={}符合条件的相关的产品，将此item关联的product置位空。", code);
            productItemDao.updateItemProductByCode(code, null);
            return;
        }
        ProductPO oriPro = productItemPO.getProduct();
        String oriCode = null;
        String oriSalePrice = null;
        String oriSaleDate = null;
        if(oriPro != null){
            PriceInfoPO oriPrice = oriPro.getPriceCalendar().getPriceInfos();
            if(oriPrice != null
                    && StringUtils.isNotBlank(oriPro.getCode())
                    && oriPrice.getSalePrice() != null
                    && oriPrice.getSaleDate() != null){
                oriCode = oriPro.getCode();
                oriSalePrice = oriPrice.getSalePrice().toPlainString();
                oriSaleDate = DateTimeUtil.formatDate(oriPrice.getSaleDate());
                if(StringUtils.equals(oriPro.getCode(), productPO.getCode())
                        && oriPrice.getSalePrice().doubleValue() == productPO.getPriceCalendar().getPriceInfos().getSalePrice().doubleValue()
                        && oriPrice.getSaleDate().getTime() == productPO.getPriceCalendar().getPriceInfos().getSaleDate().getTime()){
                    log.info("item={}最低价产品没有变化不用刷新，productCode={}, salePrice={}, saleDate={}",
                            code, oriCode, oriSalePrice, oriSaleDate);
                    return;
                }
            }
        }
        log.info("item={}最低价产品有变化需要刷新，原productCode={}, 原salePrice={}, 原saleDate={}, 新productCode={}, 新salePrice={}, 新saleDate={}",
                code, oriCode, oriSalePrice, oriSaleDate,
                productPO.getCode(), productPO.getPriceCalendar().getPriceInfos().getSalePrice().toPlainString(),
                DateTimeUtil.formatDate(productPO.getPriceCalendar().getPriceInfos().getSaleDate()));
        productItemDao.updateItemProductByCode(code, productPO);
    }
}
