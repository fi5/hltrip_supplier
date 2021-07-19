package com.huoli.trip.supplier.web.dao.impl;

import com.huoli.trip.common.constant.MongoConst;
import com.huoli.trip.common.entity.mpo.groupTour.GroupTourProductSetMealBackupMPO;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotProductBackupMPO;
import com.huoli.trip.supplier.web.dao.GroupProductBackupDao;
import com.huoli.trip.supplier.web.dao.ScenicSpotProductBackupDao;
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
 * 创建日期：2021/3/23<br>
 */
@Repository
public class GroupProductBackupDaoImpl implements GroupProductBackupDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void saveGroupProductBackup(GroupTourProductSetMealBackupMPO groupTourProductSetMealBackupMPO){
        Query query = new Query();
        query.addCriteria(Criteria.where("groupTourProductMPO._id").is(groupTourProductSetMealBackupMPO.getId()));
        Document document = new Document();
        mongoTemplate.getConverter().write(groupTourProductSetMealBackupMPO, document);
        Update update = Update.fromDocument(document);
        mongoTemplate.upsert(query, update, MongoConst.COLLECTION_NAME_GROUPTOUR_PRODUCT_SET_MEAL_BACKUP);
    }

    @Override
    public GroupTourProductSetMealBackupMPO getGroupProductBackupBySetMealId(String setMealId){
        Query query = new Query();
        query.addCriteria(Criteria.where("groupTourProductSetMealMPO._id").is(setMealId));
        return mongoTemplate.findOne(query, GroupTourProductSetMealBackupMPO.class);
    }

    @Override
    public GroupTourProductSetMealBackupMPO getGroupProductBackupByProductId(String productId){
        Query query = new Query();
        query.addCriteria(Criteria.where("groupTourProductMPO._id").is(productId));
        return mongoTemplate.findOne(query, GroupTourProductSetMealBackupMPO.class);
    }
}
