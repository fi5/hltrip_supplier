package com.huoli.trip.supplier.web.dao.impl;

import com.huoli.trip.common.constant.MongoConst;
import com.huoli.trip.common.entity.mpo.hotelScenicSpot.HotelScenicSpotProductMPO;
import com.huoli.trip.common.entity.mpo.hotelScenicSpot.HotelScenicSpotProductSetMealMPO;
import com.huoli.trip.supplier.web.dao.HotelScenicProductSetMealDao;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/4/21<br>
 */
@Repository
public class HotelScenicProductSetMealDaoImpl implements HotelScenicProductSetMealDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<HotelScenicSpotProductSetMealMPO> getByProductId(String productId){
        return mongoTemplate.find(new Query(Criteria.where("hotelScenicSpotProductId").is(productId)), HotelScenicSpotProductSetMealMPO.class);
    }

    @Override
    public void saveProduct(HotelScenicSpotProductSetMealMPO setMealMPO){
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(setMealMPO.getId()));
        Document document = new Document();
        mongoTemplate.getConverter().write(setMealMPO, document);
        Update update = Update.fromDocument(document);
        mongoTemplate.upsert(query, update, MongoConst.COLLECTION_NAME_HOTEL_SCENICSPOT_PRODUCT_SETMEAL);
    }
}
