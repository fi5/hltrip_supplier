package com.huoli.trip.supplier.web.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.google.common.collect.Lists;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.supplier.api.DynamicProductItemService;
import com.huoli.trip.supplier.api.ProductService;
import com.huoli.trip.supplier.web.dao.ProductDao;
import com.huoli.trip.supplier.web.dao.ProductItemDao;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/1/8<br>
 */
@Service(timeout = 10000,group = "hltrip")
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductDao productDao;

    @Autowired
    private ProductItemDao productItemDao;

    @Autowired
    private DynamicProductItemService dynamicProductItemService;

    @Override
    public void updateStatusByCode(String code, int status){
        productDao.updateStatusByCode(code, status);
        dynamicProductItemService.refreshItemByProductCode(Lists.newArrayList(code));

    }
    @Override
    public void updateStatusByCodev2(String code, int status,String category){
        productDao.updateStatusByCodev2(code, status,category);
        dynamicProductItemService.refreshItemByProductCode(Lists.newArrayList(code));
    }

    @Override
    public void updateSupplierStatusAndAppFromByCode(String code, Integer supplierStatus, List<String> appFroms){
        if(supplierStatus != null){
            updateSupplierStatusByCode(code, supplierStatus);
        }
        if(ListUtils.isNotEmpty(appFroms)){
            updateAppFromByCode(code, appFroms);
        }
        List<String> codes = productItemDao.selectCodesBySupplierId(code);
        if(ListUtils.isNotEmpty(codes)){
            dynamicProductItemService.refreshItemByCode(codes);
        }
    }

    @Override
    public void updateSupplierStatusByCode(String code, Integer supplierStatus){
        productDao.updateSupplierStatusByCode(code, supplierStatus);
    }

    @Override
    public void updateAppFromByCode(String code, List<String> appFroms){
        if(ListUtils.isNotEmpty(appFroms)) {
            appFroms.forEach(appFrom -> productDao.updateAppFromByCode(code, appFrom));
        }
    }
}
