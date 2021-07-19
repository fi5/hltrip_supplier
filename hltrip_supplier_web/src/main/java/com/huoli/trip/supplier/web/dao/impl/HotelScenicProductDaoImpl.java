package com.huoli.trip.supplier.web.dao.impl;

import com.huoli.trip.common.constant.MongoConst;
import com.huoli.trip.common.entity.mpo.hotelScenicSpot.HotelScenicSpotProductMPO;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotProductMPO;
import com.huoli.trip.supplier.web.dao.HotelScenicProductDao;
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
 * 创建日期：2021/4/21<br>
 */
@Repository
public class HotelScenicProductDaoImpl implements HotelScenicProductDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public HotelScenicSpotProductMPO getBySupplierProductId(String supplierProductId, String channel){
        return mongoTemplate.findOne(new Query(Criteria.where("supplierProductId")
                .is(supplierProductId).and("channel").is(channel)), HotelScenicSpotProductMPO.class);
    }

    @Override
    public void saveProduct(HotelScenicSpotProductMPO productMPO){
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(productMPO.getId()));
        Document document = new Document();
        mongoTemplate.getConverter().write(productMPO, document);
        Update update = Update.fromDocument(document);
        mongoTemplate.upsert(query, update, MongoConst.COLLECTION_NAME_HOTEL_SCENICSPOT_PRODUCT);
    }

    @Override
    public HotelScenicSpotProductMPO getByProductId(String productId) {
        return mongoTemplate.findOne(new Query(Criteria.where("_id").is(productId)), HotelScenicSpotProductMPO.class);
    }
}
