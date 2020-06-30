package com.huoli.trip.supplier.web.yaochufa.service.impl;

import com.huoli.trip.supplier.api.YcfProductService;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfProduct;
import com.huoli.trip.supplier.web.dao.PriceDao;
import com.huoli.trip.supplier.web.dao.ProductDao;
import com.huoli.trip.supplier.web.dao.ProductItemDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/30<br>
 */
public class YcfProductServiceImpl implements YcfProductService {

    @Autowired
    private ProductDao productDao;

    @Autowired
    private ProductItemDao productItemDao;

    @Autowired
    private PriceDao priceDao;

    public List<YcfProduct> productList(String city, Integer type, Integer mainPageSize){
        productItemDao.selectByCityAndType(city, type, mainPageSize);

        return null;
    }
}
