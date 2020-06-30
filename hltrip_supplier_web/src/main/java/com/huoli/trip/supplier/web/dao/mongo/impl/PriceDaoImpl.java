package com.huoli.trip.supplier.web.dao.mongo.impl;

import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.entity.PricePO;
import com.huoli.trip.supplier.web.dao.mongo.PriceDao;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/28<br>
 */
@Repository
public class PriceDaoImpl implements PriceDao {

//    @Autowired
//    private MongoTemplate mongoTemplate;

    @Override
    public void updateBySupplierProductId(PricePO pricePO){
        Query query = new Query();
        query.addCriteria(Criteria.where("code").is(pricePO.getCode()));
        Document document = new Document();
//        mongoTemplate.getConverter().write(pricePO, document);
//        Update update = Update.fromDocument(document);
//        mongoTemplate.upsert(query, update, Constants.COLLECTION_NAME_TRIP_PRICE_CALENDAR);
    }
}
