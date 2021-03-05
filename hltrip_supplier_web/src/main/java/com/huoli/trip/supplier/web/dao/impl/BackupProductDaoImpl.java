package com.huoli.trip.supplier.web.dao.impl;

import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.entity.BackupProductItemPO;
import com.huoli.trip.common.entity.BackupProductPO;
import com.huoli.trip.supplier.web.dao.BackupProductDao;
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
 * 创建日期：2021/3/3<br>
 */
@Repository
public class BackupProductDaoImpl implements BackupProductDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public BackupProductPO getBackupProductByCode(String code){
        return mongoTemplate.findOne(new Query(Criteria.where("code").is(code)), BackupProductPO.class);
    }

    @Override
    public BackupProductItemPO getBackupProductItemByCode(String code){
        return mongoTemplate.findOne(new Query(Criteria.where("code").is(code)), BackupProductItemPO.class);
    }

    @Override
    public void updateBackupProductByCode(BackupProductPO backupProductPO){
        Query query = new Query();
        query.addCriteria(Criteria.where("code").is(backupProductPO.getCode()));
        Document document = new Document();
        mongoTemplate.getConverter().write(backupProductPO, document);
        Update update = Update.fromDocument(document);
        mongoTemplate.upsert(query, update, Constants.COLLECTION_NAME_TRIP_BACKUP_PRODUCT);
    }

    @Override
    public void updateBackupProductItemByCode(BackupProductItemPO backupProductItemPO){
        Query query = new Query();
        query.addCriteria(Criteria.where("code").is(backupProductItemPO.getCode()));
        Document document = new Document();
        mongoTemplate.getConverter().write(backupProductItemPO, document);
        Update update = Update.fromDocument(document);
        mongoTemplate.upsert(query, update, Constants.COLLECTION_NAME_TRIP_BACKUP_PRODUCT_ITEM);
    }
}
