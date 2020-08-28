package com.huoli.trip.supplier.web.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.huoli.trip.common.entity.PriceInfoPO;
import com.huoli.trip.common.entity.ProductItemPO;
import com.huoli.trip.common.entity.ProductPO;
import com.huoli.trip.supplier.api.DynamicProductItemService;
import com.huoli.trip.supplier.web.dao.ProductDao;
import com.huoli.trip.supplier.web.dao.ProductItemDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

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
    public void refreshItemByProductCode(String productCode){
        log.info("开始根据产品码{}刷新item低价产品。。。", productCode);
        ProductPO productPO = productDao.getByCode(productCode);
        if(productPO != null && productPO.getMainItemCode() != null){
            refreshItem(productPO.getMainItemCode());
            log.info("根据产品码{}刷新item低价产品完成。", productCode);
            return;
        }
        log.error("根据产品码{}刷新item低价产品失败，产品或者产品主项目编码为空", productCode);
    }

    @Override
    public void refreshItemByCode(String code){
        log.info("开始根据item编码{}刷新item低价产品。。。", code);
        refreshItem(code);
        log.info("根据item编码{}刷新item低价产品完成。", code);
    }

    private void refreshItem(String code){
        ProductPO productPO =  productDao.getProductListByItemId(code);
        if(productPO == null){
            log.error("刷新item，没有查到item={}相关的产品", code);
            return;
        }
        ProductItemPO productItemPO = productItemDao.selectByCode(code);
        if(productItemPO == null){
            log.error("刷新item，没有查到item={}", code);
            return;
        }
        ProductPO oriPro = productItemPO.getProduct();
        if(oriPro != null){
            PriceInfoPO oriPrice = productItemPO.getProduct().getPriceCalendar().getPriceInfos();
            if(oriPrice != null
                    && StringUtils.isNotBlank(oriPro.getCode()) && oriPrice.getSalePrice() != null
                    && StringUtils.equals(oriPro.getCode(), productPO.getCode())
                    && oriPrice.getSalePrice().doubleValue() == productPO.getPriceCalendar().getPriceInfos().getSalePrice().doubleValue()){
                log.info("item={}最低价产品没有变化不用刷新，productCode={}, salePrice={}", code, oriPro.getCode(), oriPrice.getSalePrice().toString());
                return;
            }
        }
        log.info("item={}最低价产品有变化需要刷新，原productCode={}, 原salePrice={}，新productCode={}, 新salePrice={}",
                code, oriPro.getCode(), oriPro.getPriceCalendar().getPriceInfos().getSalePrice().toString(),
                productPO.getCode(), productPO.getPriceCalendar().getPriceInfos().getSalePrice().toString());
        try {
            productItemDao.updateProductAndPriceByCode(code, productPO);
        } catch (Exception e) {
            log.error("刷新item失败", e);
            return;
        }
    }
}
