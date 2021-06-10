package com.huoli.trip.supplier.web.dao.impl;

import com.huoli.trip.common.constant.MongoConst;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotBackupMPO;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotProductBackupMPO;
import com.huoli.trip.supplier.web.dao.ScenicSpotBackupDao;
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
public class ScenicSpotProductBackupDaoImpl implements ScenicSpotProductBackupDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void saveScenicSpotProductBackup(ScenicSpotProductBackupMPO scenicSpotProductBackupMPO){
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(scenicSpotProductBackupMPO.getId()));
        Document document = new Document();
        mongoTemplate.getConverter().write(scenicSpotProductBackupMPO, document);
        Update update = Update.fromDocument(document);
        mongoTemplate.upsert(query, update, MongoConst.COLLECTION_NAME_SCENICSPOT_PRODUCT_BACKUP);
    }

    @Override
    public ScenicSpotProductBackupMPO getScenicSpotProductBackupByProductId(String productId){
        Query query = new Query();
        query.addCriteria(Criteria.where("scenicSpotProduct._id").is(productId));
        return mongoTemplate.findOne(query, ScenicSpotProductBackupMPO.class);
    }
}
