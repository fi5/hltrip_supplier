package com.huoli.trip.supplier.web.dao.impl;

import com.huoli.trip.common.constant.MongoConst;
import com.huoli.trip.common.entity.mpo.groupTour.GroupTourProductSetMealBackupMPO;
import com.huoli.trip.common.entity.mpo.hotelScenicSpot.HotelScenicSpotProductBackupMPO;
import com.huoli.trip.supplier.web.dao.HotelScenicProductBackupDao;
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
 * 创建日期：2021/4/22<br>
 */
@Repository
public class HotelScenicProductBackupDaoImpl implements HotelScenicProductBackupDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void saveHotelScenicSpotProductBackup(HotelScenicSpotProductBackupMPO hotelScenicSpotProductBackupMPO){
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(hotelScenicSpotProductBackupMPO.getId()));
        Document document = new Document();
        mongoTemplate.getConverter().write(hotelScenicSpotProductBackupMPO, document);
        Update update = Update.fromDocument(document);
        mongoTemplate.upsert(query, update, MongoConst.COLLECTION_NAME_HOTEL_SCENICSPOT_PRODUCT_BACKUP);
    }

    @Override
    public HotelScenicSpotProductBackupMPO getHotelScenicSpotProductBackupBySetMealId(String setMealId){
        Query query = new Query();
        query.addCriteria(Criteria.where("productMPO._id").is(setMealId));
        return mongoTemplate.findOne(query, HotelScenicSpotProductBackupMPO.class);
    }

    @Override
    public HotelScenicSpotProductBackupMPO getHotelScenicSpotProductBackupByProductId(String productId){
        Query query = new Query();
        query.addCriteria(Criteria.where("setMealMPO._id").is(productId));
        return mongoTemplate.findOne(query, HotelScenicSpotProductBackupMPO.class);
    }
}
