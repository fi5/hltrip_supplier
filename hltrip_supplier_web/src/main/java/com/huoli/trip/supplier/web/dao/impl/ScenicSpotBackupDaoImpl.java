package com.huoli.trip.supplier.web.dao.impl;

import com.huoli.trip.common.constant.MongoConst;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotBackupMPO;
import com.huoli.trip.supplier.web.dao.ScenicSpotBackupDao;
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
public class ScenicSpotBackupDaoImpl implements ScenicSpotBackupDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void saveScenicSpotBackup(ScenicSpotBackupMPO scenicSpotBackupMPO){
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(scenicSpotBackupMPO.getId()));
        Document document = new Document();
        mongoTemplate.getConverter().write(scenicSpotBackupMPO, document);
        Update update = Update.fromDocument(document);
        mongoTemplate.upsert(query, update, MongoConst.COLLECTION_NAME_SCENICSPOT_BACKUP);
    }

    @Override
    public ScenicSpotBackupMPO getScenicSpotBySupplierScenicIdAndSupplierId(String supplierScenicId, String supplierId){
        Query query = new Query();
        query.addCriteria(Criteria.where("supplierScenicId").is(supplierScenicId)
                .and("supplierId").is(supplierId));
        return mongoTemplate.findOne(query, ScenicSpotBackupMPO.class);
    }

    @Override
    public ScenicSpotBackupMPO getScenicSpotByScenicSpotId(String scenicSpotId){
        Query query = new Query();
        query.addCriteria(Criteria.where("scenicSpotMPO._id").is(scenicSpotId));
        return mongoTemplate.findOne(query, ScenicSpotBackupMPO.class);
    }
}
